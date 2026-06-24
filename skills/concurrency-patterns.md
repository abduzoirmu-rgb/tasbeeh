# Concurrency Patterns

Правила и паттерны для безопасной работы с параллельным выполнением в Java/Spring + PostgreSQL.

## Чек-лист «красных флагов» — ОБЯЗАТЕЛЬНО при review

При любом из этих маркеров в коде нужны конкретные защиты. Если хоть один не закрыт — это потенциальный баг гонки.

| Маркер | Обязательная защита |
|---|---|
| `@Async` метод с `void` | Настроен `AsyncUncaughtExceptionHandler` (иначе exception потеряется) |
| `@Async` + `@Scheduled` в одном классе | Distributed lock (ShedLock или PG advisory lock) на пересекающемся ресурсе |
| `@Scheduled` в multi-node deployment | ShedLock с `lockAtMostFor` > worst-case execution time |
| `entity.setX(entity.getX() + n)` | `@Version` на entity ИЛИ atomic `UPDATE` через JPQL |
| `if (existsBy...) save();` | UNIQUE-constraint в БД на бизнес-ключе + `catch DataIntegrityViolationException` |
| Доступ к shared mutable state из нескольких потоков | `Atomic*`, `Concurrent*`, или immutable объект |
| Длительная транзакционная операция (>5 сек) | Разбить на batch'и, commit между ними |
| Очередь задач в БД | `SELECT FOR UPDATE SKIP LOCKED` или вынести в очередь (RabbitMQ/SQS) |
| Read-modify-write на счётчике | Atomic UPDATE через `@Modifying @Query` |

## Пирамида защиты — выбирай слабейшее, что решает задачу

```
                    Immutable                ← идеал, защита не нужна
                  Atomic / Concurrent        ← java.util.concurrent
                Synchronized / Lock          ← in-process критические секции
              Optimistic lock (@Version)     ← редкие конфликты, default JPA
            Pessimistic lock (FOR UPDATE)    ← частые конфликты, hot spots
          Distributed lock (ShedLock/advisory)  ← кластер, multi-node
        DB constraints (UNIQUE/CHECK)        ← всегда, последний рубеж
```

**Правило**: применяй слабейший уровень, который решает проблему.
**И при этом**: низ пирамиды (DB constraints) применяй ВСЕГДА — это бесплатный страховочный слой, независимо от того, что выше.

## Decision tree — какой механизм блокировки выбрать

```
Где shared state?
├── Внутри одной JVM (single-thread shared)
│   ├── Счётчик/флаг            → AtomicInteger / AtomicReference
│   ├── Коллекция               → ConcurrentHashMap / CopyOnWriteArrayList
│   ├── Критическая секция      → ReentrantLock или synchronized
│   └── Можно сделать immutable → СДЕЛАЙ immutable, лучшая защита
│
├── В JPA-сущности (одна нода, БД)
│   ├── Конфликты редкие         → @Version (optimistic), default
│   ├── Простой инкремент        → atomic UPDATE через @Modifying @Query
│   │                              (лучше @Version — не имеет lost update в принципе)
│   └── Конфликты частые         → @Lock(PESSIMISTIC_WRITE) или SELECT FOR UPDATE
│
├── В кластере (multi-node)
│   ├── @Scheduled задача        → ShedLock с @SchedulerLock
│   ├── Произвольная операция    → PG advisory lock (pg_try_advisory_lock)
│   ├── Очередь задач            → SELECT FOR UPDATE SKIP LOCKED
│   └── Критичная транзакция     → Redisson / Redis lock
│
└── Идемпотентность бизнес-операции
    └── ВСЕГДА UNIQUE-constraint в БД на бизнес-ключе + catch DataIntegrityViolationException
```

## Конкретные паттерны

### 1. Atomic UPDATE для счётчиков (вместо @Version + retry)

```java
// ❌ Плохо — read-modify-write, lost update под нагрузкой
campaign.setProcessedCount(campaign.getProcessedCount() + n);
campaignRepository.save(campaign);

// ✅ Хорошо — атомарный UPDATE в БД
@Modifying
@Query("UPDATE NotificationCampaign c SET c.processedCount = c.processedCount + :n WHERE c.id = :id")
int incrementProcessedCount(@Param("id") Long id, @Param("n") int n);
```

**Когда применять:** простой инкремент/декремент, прибавление к множеству, не требующий чтения значения для последующей логики.
**Преимущество перед `@Version`:** не нужен retry на `OptimisticLockingFailureException`, нет дополнительного поля.

### 2. @Version для сложных update'ов

```java
@Entity
public class Campaign extends AbstractEntity {
    @Version
    private Long version;
    // ...
}

// в сервисе:
@Retryable(value = OptimisticLockingFailureException.class,
           maxAttempts = 3,
           backoff = @Backoff(delay = 100, multiplier = 2))
@Transactional
public void updateCampaignState(Long id, ...) {
    var campaign = repository.findById(id).orElseThrow();
    // сложная логика с несколькими полями
    campaign.setStatus(...);
    campaign.setProgress(...);
    repository.save(campaign);
}
```

**Когда применять:** обновление нескольких полей с зависимостями, которые нельзя выразить одним `UPDATE`.

### 3. ShedLock для @Scheduled в кластере

```java
// pom.xml
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-spring</artifactId>
</dependency>
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-provider-jdbc-template</artifactId>
</dependency>

// конфигурация
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
public class SchedulerConfig {
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
    }
}

// использование
@Scheduled(fixedDelay = 60000)
@SchedulerLock(name = "processCampaigns",
               lockAtMostFor = "PT30M",
               lockAtLeastFor = "PT1M")
public void processCampaigns() { ... }
```

**Правило `lockAtMostFor`:** всегда больше worst-case execution time. Это страховка от deadlock'а если нода упала.
**Правило `lockAtLeastFor`:** меньше cron-интервала. Защищает от слишком частых запусков, если cron триггерит в момент отпускания lock.

### 4. PG advisory lock — для произвольных операций

Когда нужен distributed lock, но не на `@Scheduled`-задаче, а на произвольной операции (например, на `processUntilComplete(campaignId)`):

```java
@Component
@RequiredArgsConstructor
public class CampaignProcessingLock {
    private final JdbcTemplate jdbc;
    private static final long NAMESPACE = 0xCAFEL;

    public boolean tryAcquire(Long campaignId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
            "SELECT pg_try_advisory_lock(?, ?)",
            Boolean.class, NAMESPACE, campaignId
        ));
    }

    public void release(Long campaignId) {
        jdbc.queryForObject(
            "SELECT pg_advisory_unlock(?, ?)",
            Boolean.class, NAMESPACE, campaignId
        );
    }
}

// использование
public void processUntilComplete(Long campaignId) {
    if (!lock.tryAcquire(campaignId)) {
        log.info("Campaign {} is already being processed, skipping", campaignId);
        return;
    }
    try {
        // работа
    } finally {
        lock.release(campaignId);
    }
}
```

**Особенности:**
- Lock привязан к коннекту (session-level). При отключении — auto-release. Это защита от зависания при падении JVM.
- Используй фиксированный namespace, чтобы локи разных ресурсов не пересекались.
- Не освобождается автоматически в transaction-rollback (для transaction-level используй `pg_try_advisory_xact_lock`).

### 5. SELECT FOR UPDATE SKIP LOCKED — для очередей задач

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0"))
@Query(value = "SELECT * FROM jobs WHERE status = 'PENDING' " +
               "ORDER BY created_at LIMIT :n FOR UPDATE SKIP LOCKED",
       nativeQuery = true)
List<Job> claimNextJobs(@Param("n") int batchSize);
```

**Когда применять:** N воркеров разбирают очередь задач, каждый берёт свой батч, не конкурируют.
**Альтернатива:** вынести в RabbitMQ/SQS — лучше для прода с большой нагрузкой.

### 6. UNIQUE constraint + catch — soft idempotency

```java
// БД (миграция или ручной DDL)
CREATE UNIQUE INDEX uq_notification_logs_campaign_guest
    ON notification_logs (campaign_id, guest_id)
    WHERE deleted = false AND campaign_id IS NOT NULL;

// сервис — обрабатывать DataIntegrityViolationException как штатную ситуацию
public void sendNotification(Campaign c, Guest g) {
    try {
        var log = new NotificationLog(c, g, SENT);
        logRepository.save(log);
    } catch (DataIntegrityViolationException e) {
        // другой воркер уже записал — это нормально, не ошибка
        log.info("Notification for campaign={} guest={} already sent", c.getId(), g.getId());
    }
}
```

**Партиальный UNIQUE:** используй `WHERE` в индексе для soft-delete и nullable полей.
**Никогда не глотай молча:** хотя бы info-лог должен быть.

## Spring @Async — отдельный класс проблем

`@Async`-метод **отвязан** от вызывающего. Любое исключение в нём не дойдёт до caller'а.

### Правила

1. **Возвращай `CompletableFuture<T>`, не `void`** — caller может `.exceptionally(...)` или `.handle(...)`.
2. **Если возврат `void` — настрой `AsyncUncaughtExceptionHandler`** глобально:
   ```java
   @Configuration
   @EnableAsync
   public class AsyncConfig implements AsyncConfigurer {
       @Override
       public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
           return (ex, method, params) ->
               log.error("Async error in {}: {}", method, ex.getMessage(), ex);
       }
   }
   ```
3. **`@Transactional` НЕ распространяется через `@Async`** — async-метод запускается в новом потоке без транзакционного контекста. Если нужно — открывай транзакцию **внутри** async-метода.
4. **Предусматривай timeout** — async-задача может зависнуть навсегда. Circuit breaker или явный `Thread.interrupt()` после deadline.

### Async + Scheduled — частая ошибка

```java
// ❌ Плохо — два независимых воркера могут пересечься
@Component
public class CampaignScheduler {

    @Async
    public void triggerImmediate(Long id) { processUntilComplete(id); }

    @Scheduled(fixedDelay = 120000)
    public void processCampaigns() {
        repository.findFirstPendingOrProcessing()
            .ifPresent(c -> processUntilComplete(c.getId()));
    }

    private void processUntilComplete(Long id) { /* долгая работа */ }
}

// ✅ Хорошо — distributed lock на ресурсе
@Component
public class CampaignScheduler {

    @Async
    public void triggerImmediate(Long id) {
        if (!lock.tryAcquire(id)) return;
        try { processUntilComplete(id); } finally { lock.release(id); }
    }

    @Scheduled(fixedDelay = 60000)
    @SchedulerLock(name = "processCampaigns", lockAtMostFor = "PT30M")
    public void processCampaigns() {
        repository.findAllPendingOrProcessing()
            .forEach(c -> {
                if (lock.tryAcquire(c.getId())) {
                    try { processUntilComplete(c.getId()); }
                    finally { lock.release(c.getId()); }
                }
            });
    }
}

// ✅ ЕЩЁ ЛУЧШЕ — убрать @Async совсем, оставить только @Scheduled со ShedLock
@Scheduled(fixedDelay = 30000)
@SchedulerLock(name = "processCampaigns", lockAtMostFor = "PT30M")
public void processCampaigns() { /* единственная точка входа */ }
```

## Транзакции и propagation

### REQUIRES_NEW для идемпотентного блока

```java
// Каждая отправка — отдельная транзакция, не зависит от родительского контекста
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void sendNotification(...) { ... }
```

**Когда применять:** каждый item в batch'е должен committit'ся независимо. Если один упадёт — остальные не откатятся.
**Подвох:** REQUIRES_NEW не защищает от race conditions сам по себе. Soft-check `if (exists...)` всё равно может проиграть гонку (TOCTOU). Нужен UNIQUE-constraint в БД.

### Self-injection для @Transactional через прокси

```java
@Component
public class Service {
    @Lazy private final Service self;  // прокси Spring

    public void outer() {
        self.inner();  // ✅ через прокси, @Transactional работает
        // this.inner();  ← ❌ напрямую, @Transactional НЕ работает
    }

    @Transactional
    public void inner() { ... }
}
```

## Тестирование concurrency

### Шаблон concurrency-теста

```java
@Test
@DisplayName("two parallel workers must not produce duplicate logs")
void noDuplicates_underConcurrentProcessing() throws Exception {
    // Given
    var campaign = createCampaignWithNGuests(50);
    int threads = 10;
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threads);
    AtomicInteger errors = new AtomicInteger();

    var executor = Executors.newFixedThreadPool(threads);

    // When
    for (int i = 0; i < threads; i++) {
        executor.submit(() -> {
            try {
                start.await();
                scheduler.processUntilComplete(campaign.getId());
            } catch (Exception e) {
                errors.incrementAndGet();
            } finally {
                done.countDown();
            }
        });
    }
    start.countDown();
    done.await(30, TimeUnit.SECONDS);

    // Then — главный инвариант: сколько отправили = сколько гостей
    assertThat(notificationLogRepository.countByCampaignId(campaign.getId()))
        .as("Должно быть ровно 50 логов, не больше")
        .isEqualTo(50);
    assertThat(errors.get()).as("Не должно быть ошибок").isZero();

    executor.shutdown();
}
```

**Запускать с реальной PostgreSQL** (Testcontainers), не с H2/моками.

### Когда писать concurrency-тест

ОБЯЗАТЕЛЬНО при наличии любого красного флага из чек-листа в начале файла. Это контракт на безопасность кода в многопоточной среде, а не «опционально».

## Антипаттерны

| Антипаттерн | Почему плохо | Как правильно |
|---|---|---|
| `if (exists) save();` без UNIQUE | TOCTOU — гонка между check и use | + UNIQUE-constraint, catch `DataIntegrityViolationException` |
| `entity.set(entity.get() + n); save();` без `@Version` | Lost update | atomic UPDATE через `@Modifying @Query` |
| `@Async` + `@Scheduled` без lock | Два воркера на одной сущности | ShedLock + advisory lock |
| `@Scheduled` без ShedLock в кластере | N запусков на N нодах | `@SchedulerLock` с правильными `lockAtMostFor`/`lockAtLeastFor` |
| Изменение shared `HashMap`/`ArrayList` из нескольких потоков | `ConcurrentModificationException`, потеря данных | `ConcurrentHashMap`/`CopyOnWriteArrayList` |
| `synchronized` на `this` или `String` constant | Внешний код может lock'нуть на том же объекте | private final lock object |
| Удержание lock'а во время IO (HTTP/DB call) | Deadlock, медленные операции | Открой lock → IO → закрой lock |
| Mockito-only тест на параллельный код | Race не воспроизводится | concurrency integration-test с реальной БД |
| Чтение из БД вне транзакции после async-вызова | Stale read, race с auto-commit | `@Transactional(REQUIRES_NEW)` на async-методе или явный fetch |

## Жёсткие правила

- ЗАПРЕЩЕНО иметь `@Async` + `@Scheduled` в одном классе без distributed lock на пересекающемся ресурсе
- ЗАПРЕЩЕНО иметь `entity.setX(entity.getX() + n)` без `@Version` или atomic UPDATE
- ЗАПРЕЩЕНО полагаться только на `if (exists...)` для идемпотентности — обязателен UNIQUE-constraint в БД
- ЗАПРЕЩЕНО `synchronized` на `this`, `String` или классах, доступных извне — только private final lock object
- ЗАПРЕЩЕНО держать DB-lock или synchronized дольше, чем нужно для модификации памяти
- ОБЯЗАТЕЛЬНО `@SchedulerLock` на каждом `@Scheduled` в multi-node deployment
- ОБЯЗАТЕЛЬНО `AsyncUncaughtExceptionHandler` настроен в проекте, если есть хоть один `@Async void`
- ОБЯЗАТЕЛЬНО concurrency integration-test (с реальной БД) при наличии любого красного флага из чек-листа

## Применение в проекте meet

- БД: PostgreSQL — есть встроенные advisory locks, не нужны Redis/ZooKeeper
- Стек single-node сейчас, но архитектура должна быть готова к multi-node
- `@Async` используется в: notification sender, email отправка, OAuth handlers
- `@Scheduled` используется в: notification campaign processor, telegram polling
- ShedLock пока не подключён — добавить при первом ушедшем в кластер сценарии
- При выборе между `@Version` + retry и atomic UPDATE — по умолчанию atomic UPDATE (проще, эффективнее)
