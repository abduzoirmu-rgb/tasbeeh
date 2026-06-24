# Database Conventions

## Entity — базовый паттерн
```java
@Entity
@Table(name = "table_name")
@Where(clause = "deleted = false")           // Soft-delete фильтр
@SQLDelete(sql = "UPDATE table_name SET deleted = true WHERE id = ?")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EntityName extends AbstractEntity {
    // поля
}
```

### AbstractEntity (базовый класс)
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    @Column(nullable = false)
    private boolean deleted = false;
}
```

## Soft Delete
Все основные entities используют soft delete:
- `deleted` boolean column (default false)
- `@Where(clause = "deleted = false")` — автоматический фильтр на все SELECT
- `@SQLDelete(sql = "UPDATE ... SET deleted = true WHERE id = ?")` — DELETE → UPDATE
- Каскадный soft delete НЕ используется — ручная обработка в сервисах

## Enum маппинг
```java
@Enumerated(EnumType.STRING)  // Хранить как строку, НЕ как ordinal
private Role role;             // OWNER, ADMIN, ADMIN_EVENT, EMPLOYEE, GUARD

@Enumerated(EnumType.STRING)
private InvitationStatus invitationStatus;  // PENDING, ACCEPTED, DECLINED, EXPIRED
```

## JSON поля (PostgreSQL JSONB)
```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(columnDefinition = "jsonb")
private Map<String, Object> filterCriteria;

// Или с кастомным конвертером:
@Convert(converter = EventFieldConverter.class)
private List<EventField> fields;
```

## Связи между сущностями
```java
// ManyToOne (ленивая загрузка по умолчанию):
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "event_id")
private Event event;

// OneToMany (ленивая, cascade ALL):
@OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Location> locations;

// ManyToMany — НЕ используется в проекте (вместо неё связующая entity)
// UserEvent — связь User ↔ Event с дополнительными полями (role, invitationStatus)
```

## Composite Key
```java
@Embeddable
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GuestLocationId implements Serializable {
    private Long guestId;
    private Long locationId;
}

@Entity
@Table(name = "guest_location")
public class GuestLocation {
    @EmbeddedId
    private GuestLocationId id;
}
```

## Repository паттерн
```java
public interface EntityRepository extends JpaRepository<Entity, Long>,
        JpaSpecificationExecutor<Entity> {  // для Specification фильтрации

    // Named query methods:
    Optional<Entity> findByEmail(String email);
    List<Entity> findByEventIdAndDeletedFalse(Long eventId);
    boolean existsByEmailAndEventId(String email, Long eventId);

    // @Query для сложных запросов:
    @Query("SELECT e FROM Entity e WHERE e.event.id = :eventId AND e.status = :status")
    List<Entity> findByEventAndStatus(@Param("eventId") Long eventId,
                                       @Param("status") Status status);
}
```

## JPA Projections (интерфейсы)
```java
// Для оптимизации SELECT (не грузить всю entity):
public interface CommonProjection {
    Long getId();
    String getName();
}

public interface GuestInfoProjection extends CommonProjection {
    String getBarcode();
    String getEmail();
}

// Использование:
List<GuestInfoProjection> findByEventId(Long eventId);
```

## Specification Pattern
```java
public class GuestSpecification {
    public static Specification<Guest> filter(Map<String, Object> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.containsKey("eventId")) {
                predicates.add(cb.equal(root.get("event").get("id"),
                    params.get("eventId")));
            }
            if (params.containsKey("search")) {
                String search = "%" + params.get("search") + "%";
                predicates.add(cb.like(cb.lower(root.get("fullName")),
                    search.toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

## Flyway миграции (временно отключены)
```
Текущее состояние: Flyway закомментирован в pom.xml
Формат: V{номер}__{описание}.sql
Текущий номер: V13 (следующая миграция: V14)
Расположение: backend/src/main/resources/db/migration/

Правила:
- ЗАПРЕЩЕНО менять уже созданные миграции
- ЗАПРЕЩЕНО создавать два файла с одним номером (уже был конфликт с V10)
- Каждая миграция — один логический шаг
- Имена в snake_case: V14__add_payment_refund_table.sql
```

## Конвертеры (AttributeConverter)
```java
@Converter
public class EventFieldConverter implements AttributeConverter<List<EventField>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<EventField> attribute) {
        return objectMapper.writeValueAsString(attribute);
    }

    @Override
    public List<EventField> convertToEntityAttribute(String dbData) {
        return objectMapper.readValue(dbData, new TypeReference<>() {});
    }
}
```

## MapStruct маппинг
```java
@Mapper(componentModel = "spring")
public abstract class EntityMapper {
    // Простой маппинг:
    @Mapping(target = "userId", source = "user.id")
    public abstract EntityDto toDto(Entity entity);

    // Игнорирование поля:
    @Mapping(target = "fieldName", ignore = true)

    // Константа:
    @Mapping(target = "memberType", constant = "REGISTERED")

    // Java expression:
    @Mapping(target = "url",
        expression = "java(baseUrl + \"/event/\" + entity.getUuid())")

    // @Value инъекция в mapper bean:
    @Value("${app.registration-url}")
    String registrationUrl;
}
```

## UNIQUE-constraints для бизнес-инвариантов

Это **последний рубеж** защиты от дублей. Применяется **независимо** от soft-checks в коде (`if (existsBy...) ...`) — soft-check проигрывает гонку, БД нет.

### Когда обязательно UNIQUE

При любом из этих сценариев — UNIQUE-constraint в БД ОБЯЗАТЕЛЕН:

| Бизнес-инвариант | Пример |
|---|---|
| «не более одной записи на пару (X, Y)» | `notification_logs(campaign_id, guest_id)` — одна попытка отправки на пару |
| «email уникален в системе» | `users(email)` |
| «связь many-to-many без soft-delete» | `user_event(user_id, event_id)` |
| Идемпотентная операция по бизнес-ключу | `payments(idempotency_key)` |
| Связь one-to-one | `user_profile(user_id)` |

### Партиальный UNIQUE для soft-delete и nullable

Если у таблицы есть `deleted` или nullable бизнес-поле, обычный UNIQUE сломает повторную вставку после soft-delete. Используй **partial index**:

```sql
-- ✅ Партиальный UNIQUE — учитывает soft-delete и nullable
CREATE UNIQUE INDEX uq_notification_logs_campaign_guest
    ON notification_logs (campaign_id, guest_id)
    WHERE deleted = false AND campaign_id IS NOT NULL;

-- ✅ Email уникален среди не-удалённых
CREATE UNIQUE INDEX uq_users_email
    ON users (email)
    WHERE deleted = false;

-- ❌ Плохо — обычный UNIQUE сломает повторную регистрацию после soft-delete
CREATE UNIQUE INDEX uq_users_email ON users (email);
```

### Обработка нарушения UNIQUE в коде

Spring оборачивает PostgreSQL `unique_violation` (SQLSTATE 23505) в `DataIntegrityViolationException`. Обрабатывать как **штатную ситуацию** в идемпотентных кейсах:

```java
try {
    var log = new NotificationLog(campaign, guest, SENT);
    logRepository.save(log);
} catch (DataIntegrityViolationException e) {
    // Другой воркер уже записал — это нормально, не ошибка.
    log.info("Duplicate notification log skipped for campaign={} guest={}",
             campaign.getId(), guest.getId());
}
```

В случае HTTP-ответа: `GlobalExceptionHandler` уже ловит `DuplicateKeyException` → 409 Conflict.

### Чек-лист при создании новой Entity

1. Какой бизнес-ключ должен быть уникален? → UNIQUE-constraint в DDL.
2. Используется ли soft-delete? → partial index `WHERE deleted = false`.
3. Есть ли nullable поля в бизнес-ключе? → partial index `WHERE field IS NOT NULL`.
4. Есть ли counter-поле, которое будут инкрементировать (`processedCount`, `viewCount`)? → atomic UPDATE через `@Modifying @Query` (см. `concurrency-patterns.md`).
5. Под конкурентной нагрузкой? → `@Version` или pessimistic lock (см. `concurrency-patterns.md`).

## Жёсткие правила
- ЗАПРЕЩЕНО использовать `@Enumerated(EnumType.ORDINAL)` — только STRING
- ЗАПРЕЩЕНО удалять данные физически (DELETE) — только soft delete
- ЗАПРЕЩЕНО создавать Entity без наследования от AbstractEntity
- ЗАПРЕЩЕНО использовать `FetchType.EAGER` на коллекциях (N+1 problem)
- ЗАПРЕЩЕНО менять существующие миграции — только новые файлы
- ЗАПРЕЩЕНО полагаться только на soft-check `if (existsBy...) return` для уникальности — обязателен UNIQUE-constraint в БД
- ЗАПРЕЩЕНО `entity.setCounter(entity.getCounter() + n)` без `@Version` или atomic UPDATE — lost update гарантирован под нагрузкой
- ОБЯЗАТЕЛЬНО UNIQUE-constraint на каждом бизнес-инварианте «не более одной записи на (X, Y)»
- ОБЯЗАТЕЛЬНО partial index для UNIQUE на таблицах с soft-delete и nullable полями
- Каждая Entity ОБЯЗАНА иметь @Where и @SQLDelete для soft delete
