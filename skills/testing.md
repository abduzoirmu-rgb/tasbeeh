# Testing

## Backend — JUnit 5 + Mockito

### Структура тестов
```
src/test/java/tj/abad/meet/
├── controller/          — @WebMvcTest (HTTP слой)
├── service/impl/        — @ExtendWith(MockitoExtension.class) (бизнес-логика)
├── service/scheduler/   — тесты для @Scheduled
├── utils/               — тесты утилит (чистые unit)
└── security/            — тесты OAuth2/JWT
```

### Service тесты (основной паттерн)
```java
@ExtendWith(MockitoExtension.class)
class ServiceImplTest {
    @Mock private Repository repository;
    @Mock private Mapper mapper;
    @InjectMocks private ServiceImpl service;

    @Test
    void methodName_scenario_expectedResult() {
        // Given
        var entity = new Entity();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(new Dto());

        // When
        var result = service.getById(1L);

        // Then
        assertNotNull(result);
        verify(repository).findById(1L);
        verify(mapper).toDto(entity);
    }
}
```

### Controller тесты
```java
@WebMvcTest(ResourceController.class)
class ResourceControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private ResourceService service;

    @Test
    void getAll_returnsOk() throws Exception {
        when(service.getAll(any())).thenReturn(List.of());

        mockMvc.perform(get("/events/1/resources")
                .header("Authorization", "Bearer test-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
```

### Мокирование SessionUtils (static)
```java
try (MockedStatic<SessionUtils> mocked = mockStatic(SessionUtils.class)) {
    var userDetails = mock(UserDetailsAdapter.class);
    var user = new User();
    user.setId(1L);
    when(userDetails.getUser()).thenReturn(user);
    when(userDetails.getEventId()).thenReturn(100L);
    mocked.when(SessionUtils::currentUser).thenReturn(userDetails);

    // Test code using SessionUtils.currentUser()
}
```

### ArgumentCaptor для проверки внутренних объектов
```java
ArgumentCaptor<UserEvent> captor = ArgumentCaptor.forClass(UserEvent.class);
verify(repository).save(captor.capture());
UserEvent saved = captor.getValue();
assertEquals(Role.ADMIN_EVENT, saved.getRole());
assertEquals(InvitationStatus.ACCEPTED, saved.getInvitationStatus());
```

### Параметризованные тесты
```java
@ParameterizedTest
@ValueSource(strings = {"invalid1", "invalid2", ""})
void validate_invalidInput_returnsFalse(String input) {
    assertFalse(validator.validate(input).isValid());
}

@ParameterizedTest
@NullAndEmptySource
@ValueSource(strings = {"  ", "\t", "\n"})
void process_blankInput_returnsEmpty(String input) {
    assertEquals("", processor.process(input));
}
```

### Группировка тестов
```java
@Nested
@DisplayName("publishWhatsapp")
class PublishWhatsapp {
    @Test
    void happyPath_convertsAndPublishes() { ... }

    @Test
    void templateNotFound_throwsException() { ... }
}
```

### ReflectionTestUtils для @Value полей
```java
ReflectionTestUtils.setField(service, "uiUrl", "http://localhost:4200");
ReflectionTestUtils.setField(handler, "redirectUrl", "/auth/oauth2/redirect");
```

### Assertions — два стиля
```java
// JUnit 5 (используется в проекте):
assertEquals(expected, actual);
assertNotNull(value);
assertThrows(EventNotFoundException.class, () -> service.delete(1L));
assertTrue(result.isValid());

// AssertJ (также используется):
assertThat(result.isValid()).isTrue();
assertThat(errors).isEmpty();
assertThat(errors).hasSize(3);
assertThat(result.getError()).contains("не может быть пустым");
```

### Mockito Verification
```java
verify(mock, times(1)).method(arg);
verify(mock, never()).method();
verifyNoInteractions(mock);
verifyNoMoreInteractions(mock);
```

## Frontend — Karma + Jasmine

### Component тесты
```typescript
describe('ComponentName', () => {
  let component: ComponentName;
  let fixture: ComponentFixture<ComponentName>;
  let mockService: jasmine.SpyObj<ServiceName>;

  beforeEach(async () => {
    mockService = jasmine.createSpyObj('ServiceName', ['method1', 'method2']);
    mockService.method1.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [ComponentName, NoopAnimationsModule],
      providers: [
        { provide: ServiceName, useValue: mockService },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({}) } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ComponentName);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
```

### Async тестирование
```typescript
it('should handle async operation', fakeAsync(() => {
    component.onSubmit();
    tick();  // flush microtasks
    expect(mockService.method).toHaveBeenCalledWith(expectedArgs);
}));
```

### SessionStorage в тестах
```typescript
afterEach(() => {
    sessionStorage.removeItem('registration_channel');
});

it('should save channel to sessionStorage', fakeAsync(() => {
    component.onFormSubmit({ formData, channel: 'TELEGRAM' });
    tick();
    expect(sessionStorage.getItem('registration_channel')).toBe('TELEGRAM');
}));
```

### Dialog тестирование
```typescript
const dialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);

it('should close dialog on success', () => {
    mockService.create.and.returnValue(of(mockResponse));
    component.onSubmit();
    expect(dialogRef.close).toHaveBeenCalledWith(true);
});
```

## E2E — Playwright

### Структура
```
ui-ng/e2e/
├── specs/              — тесты (<feature>.spec.ts)
├── pages/              — Page Objects (<page>.page.ts)
└── helpers/
    └── mailpit.helper.ts  — работа с email
```

### Page Object
```typescript
import { type Page, type Locator } from '@playwright/test';

export class FeaturePage {
  readonly page: Page;
  readonly submitButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.submitButton = page.locator('button[type="submit"]');
  }

  async goto() {
    await this.page.goto('/feature');
  }
}
```

### E2E тест (паттерн)
```typescript
import { test, expect } from '@playwright/test';
import { FeaturePage } from '../pages/feature.page';
import { MailpitHelper } from '../helpers/mailpit.helper';

const mailpit = new MailpitHelper();

test.describe('Feature Name', () => {

  test('happy path — user completes main scenario', async ({ page }) => {
    // Clear stale auth state
    await page.goto('/auth/login');
    await page.evaluate(() => localStorage.clear());
    await page.reload();

    // Login with seed user
    // ... login steps ...

    // Verify dashboard loaded (ОБЯЗАТЕЛЬНО после логина)
    await expect(page.locator('.logo', { hasText: 'Eventy' })).toBeVisible();

    // Feature-specific actions and assertions
    // ...
  });
});
```

### Email verification через Mailpit
```typescript
const mailpit = new MailpitHelper();

// Очистить inbox перед тестом
await mailpit.deleteAll();

// Выполнить действие, которое триггерит email
await registerPage.register('User', email, phone, password);

// Дождаться email (async, backend отправляет с @Async)
const emailMsg = await mailpit.waitForEmail(email, 15_000);
expect(emailMsg.Subject).toContain('Верификация');

// Извлечь токен
const token = mailpit.extractActivationToken(emailMsg);
// или: mailpit.extractResetPasswordToken(emailMsg)

// Перейти по ссылке верификации
await page.goto(`/auth/verify-email-confirm?token=${token}`);
```

### Snackbar (Angular Material MDC)
```typescript
// НЕПРАВИЛЬНО — множественные элементы, strict mode violation:
page.locator('simple-snack-bar, mat-snack-bar-container, .mat-mdc-snack-bar-container');

// ПРАВИЛЬНО — один конкретный элемент, фильтр от "Сессия истекла":
const snackbar = page.locator('simple-snack-bar', { hasNotText: 'Сессия истекла' });
const dialog = page.locator('app-login-error-dialog');
await expect(snackbar.or(dialog).first()).toBeVisible({ timeout: 5_000 });
```

### E2E инфраструктура
```bash
# Docker (PostgreSQL :5433, Mailpit SMTP :1025, UI :8025)
docker compose -f docker-compose-e2e.yml up -d

# Backend с e2e профилем
./mvnw spring-boot:run -pl backend -Dspring-boot.run.profiles=e2e

# Тесты
cd ui-ng && npx playwright test           # headless
cd ui-ng && npx playwright test --headed  # видно браузер
cd ui-ng && npx playwright test --ui      # интерактивный UI

# Всё одной командой
./run-e2e.sh [--headed | --ui | --up | --down]
```

## Именование тестов
```
Backend:  methodName_scenario_expectedResult
          invite_existingUser_createsUserEvent
          invite_unknownEmail_createsPendingInvitation

Frontend: should + описание поведения
          should create
          should save channel to sessionStorage on form submit
          should close dialog on success

E2E:      описательное имя сценария
          'register → receive email → verify → login → dashboard'
          'unverified user cannot login'
          'login with seed user redirects to dashboard'
```

## Concurrency Integration-тесты

Mockito-only тесты НЕ ловят race conditions, lost updates, идемпотентность. Это **другой класс багов**, требующий другого инструмента.

### Когда ОБЯЗАТЕЛЬНО писать concurrency-тест

Любой из этих маркеров в коде = concurrency integration-test обязателен (см. `.claude/skills/concurrency-patterns.md`):

- `@Async` метод
- `@Scheduled` задача
- `@Async` + `@Scheduled` в одном классе
- `entity.setX(entity.getX() + n)` (read-modify-write)
- `if (existsBy...) save();` (soft-check идемпотентности)
- Atomic UPDATE через `@Modifying @Query`
- UNIQUE-constraint на бизнес-инварианте
- `@Version` на entity
- Distributed lock (ShedLock, advisory lock)

### Шаблон concurrency-теста

```java
@SpringBootTest
@Testcontainers  // обязательно реальная PostgreSQL, не H2/моки
class CampaignSchedulerConcurrencyIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired private CampaignScheduler scheduler;
    @Autowired private NotificationLogRepository logRepository;

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

        // When — все потоки стартуют одновременно
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

        // Then — главный инвариант: ровно N логов, ни больше ни меньше
        assertThat(logRepository.countByCampaignId(campaign.getId()))
            .as("Должно быть ровно 50 логов")
            .isEqualTo(50);
        assertThat(errors.get()).as("Без ошибок").isZero();

        executor.shutdown();
    }
}
```

### Шаблон теста на UNIQUE-constraint

```java
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationLogRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired private NotificationLogRepository repository;

    @Test
    void duplicateLog_throwsDataIntegrityViolation() {
        // Given
        var log1 = new NotificationLog(campaign, guest, SENT);
        repository.saveAndFlush(log1);

        // When & Then
        var log2 = new NotificationLog(campaign, guest, SENT);
        assertThatThrownBy(() -> repository.saveAndFlush(log2))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
```

### Шаблон теста на @Version (lost update)

```java
@Test
void concurrentUpdates_throwsOptimisticLockException() {
    // Given — два EntityManager'а с одним id
    var c1 = em1.find(Campaign.class, id);
    var c2 = em2.find(Campaign.class, id);

    // When — оба меняют и сохраняют
    c1.setProcessedCount(c1.getProcessedCount() + 5);
    em1.merge(c1);
    em1.flush();  // первый коммит — успех

    c2.setProcessedCount(c2.getProcessedCount() + 5);

    // Then — второй коммит должен упасть
    assertThatThrownBy(() -> em2.merge(c2))
        .isInstanceOf(OptimisticLockingFailureException.class);
}
```

### Шаблон теста на atomic UPDATE

```java
@Test
void incrementProcessedCount_isAtomic_underConcurrency() throws Exception {
    var campaign = createCampaign();
    int threads = 20;
    CountDownLatch latch = new CountDownLatch(threads);

    var executor = Executors.newFixedThreadPool(threads);
    for (int i = 0; i < threads; i++) {
        executor.submit(() -> {
            try {
                repository.incrementProcessedCount(campaign.getId(), 1);
            } finally {
                latch.countDown();
            }
        });
    }
    latch.await(10, TimeUnit.SECONDS);

    // Инвариант: каждый инкремент учтён, lost update нет
    var fresh = repository.findById(campaign.getId()).orElseThrow();
    assertThat(fresh.getProcessedCount()).isEqualTo(threads);
}
```

### Жёсткие правила concurrency-тестов

- ОБЯЗАТЕЛЬНО реальная PostgreSQL через Testcontainers (не H2, не in-memory моки)
- ОБЯЗАТЕЛЬНО `CountDownLatch start` чтобы все потоки стартовали одновременно
- ОБЯЗАТЕЛЬНО проверять **главный инвариант данных** (count = N), а не только отсутствие exception'ов
- Минимум 10 потоков для воспроизведения race condition
- timeout `done.await(30, SECONDS)` — чтобы тест не висел
- `executor.shutdown()` в конце

## Жёсткие правила
- ЗАПРЕЩЕНО использовать @SpringBootTest для unit-тестов сервисов (слишком тяжёлый)
- ЗАПРЕЩЕНО мокать то, что тестируется — мокаются только зависимости
- ЗАПРЕЩЕНО оставлять hardcoded URL/config в тестах — использовать ReflectionTestUtils
- ЗАПРЕЩЕНО Mockito-only unit-тестом покрывать race conditions / lost updates / идемпотентность — нужен concurrency integration-test с реальной БД
- ЗАПРЕЩЕНО использовать H2/in-memory вместо PostgreSQL Testcontainer для тестов на UNIQUE-constraints или advisory locks (поведение отличается)
- Given-When-Then структура обязательна для backend
- afterEach cleanup обязателен для localStorage/sessionStorage на frontend
- Регрессионные тесты для известных багов приветствуются
- **ОБЯЗАТЕЛЬНО**: каждая фича с UI должна иметь E2E тест (Playwright)
- **ОБЯЗАТЕЛЬНО**: после логина проверять UI дашборда, а не только URL
- **ОБЯЗАТЕЛЬНО**: concurrency integration-test при наличии любого красного флага из `.claude/skills/concurrency-patterns.md`
