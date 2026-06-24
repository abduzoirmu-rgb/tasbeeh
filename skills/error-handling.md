# Error Handling

## Backend — Exception Hierarchy
```
EventManagerException (базовый для бизнес-ошибок)
├── EventNotFoundException          → 400
├── ConflictToDeleteException       → 409
├── UnzipException                  → 400
├── UserExistException              → 400
├── UserActivationException         → 400
├── QuotaNotFoundException          → 400
├── QuotaNumberFinishedException    → 400
└── PaymentGatewayException         → 400

RuntimeException
├── AccessDeniedException           → 400
└── PayoutDetailsRequiredException  → 400

Spring exceptions (обрабатываются GlobalExceptionHandler):
├── BadCredentialsException         → 401
├── DuplicateKeyException           → 409
├── EntityNotFoundException         → 404
├── IllegalArgumentException        → 400
└── MethodArgumentNotValidException → 422
```

## GlobalExceptionHandler (@RestControllerAdvice)
```java
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(EventManagerException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(EventManagerException ex) {
        return ResponseEntity.badRequest().body(
            new ErrorResponse("Business Error", 400, ex.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.status(422).body(
            new ErrorResponse("Validation Error", 422, message)
        );
    }
}
```

## ErrorResponse (формат ответа)
```java
@Data @AllArgsConstructor
public class ErrorResponse {
    private String title;
    private int status;
    private String message;
    // stackTrace — только в dev профиле
}
```

## Создание нового Exception
```java
// 1. Наследовать от EventManagerException:
public class ResourceNotFoundException extends EventManagerException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

// 2. Добавить обработчик в GlobalExceptionHandler (если нужен особый статус):
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.status(404).body(
        new ErrorResponse("Not Found", 404, ex.getMessage())
    );
}

// 3. Бросать в сервисе:
public Entity getById(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Resource " + id + " not found"));
}
```

## Security Error Handling
```
JwtAuthenticationEntryPoint (401):
  - Нет JWT или невалидный токен
  - Response: { title: "Unauthorised", status: 401, message: "Unauthorised request" }

RestAccessDeniedHandler (403):
  - Пользователь аутентифицирован, но нет прав
  - Response: { title: "Unauthorised", status: 403, message: "Access denied request" }

JwtRequestFilter (403):
  - eventId указан, но нет UserEvent связи
  - response.sendError(SC_FORBIDDEN) — до контроллера не доходит
```

## Frontend — Error Handling Flow
```
HTTP Response Error
    │
    ▼
authInterceptor (функциональный)
    ├── 401 → removeToken + redirect /auth/login
    ├── 403 → "Доступ запрещён"
    ├── 500 → "Ошибка сервера"
    └── остальное → передаётся дальше (throwError)
    │
    ▼
Component/Service .subscribe({ error: ... })
    ├── ErrorHandlingService.handleError(err)
    │   └── Читает err.error?.message
    │       └── NotificationService.error(message)
    └── или кастомная обработка в компоненте
```

## NotificationService (MatSnackBar)
```typescript
@Injectable({ providedIn: 'root' })
export class NotificationService {
  success(message: string): void   // panelClass: success-snackbar
  error(message: string): void     // panelClass: error-snackbar
  warn(message: string): void      // panelClass: warning-snackbar
  info(message: string): void      // panelClass: info-snackbar
  // duration: 5000ms, position: top-right
}
```

## Паттерн обработки ошибок в компоненте
```typescript
this.service.create(data)
  .pipe(finalize(() => this.isLoading = false))
  .subscribe({
    next: (result) => {
      this.notificationService.success('Создано успешно');
      this.dialogRef.close(true);
    },
    error: (err) => {
      if (err.status === 409) {
        this.notificationService.warn('Ресурс уже существует');
      } else {
        this.notificationService.error(err?.error?.message || 'Произошла ошибка');
      }
    }
  });
```

## Graceful Degradation
```java
// Для invite tokens — НЕ бросать exception, а возвращать valid=false:
public InviteTokenInfo getInviteTokenInfo(String token) {
    var pending = repository.findByInviteToken(token);
    if (pending == null || pending.isUsed() || pending.isExpired()) {
        return InviteTokenInfo.builder().valid(false).build();
    }
    return InviteTokenInfo.builder()
        .eventId(pending.getEvent().getId())
        .email(pending.getEmail())
        .valid(true)
        .build();
}
```

## Try-Catch Best Practices (по Joshua Bloch + Spring)

### Правила

1. **Используй exceptions только для исключительных ситуаций**, не для control flow.
   `try { while(true) arr[i++] } catch (ArrayIndexOutOfBoundsException)` — антипаттерн.

2. **Лови узко** — конкретные классы, которые знаешь как обработать.
   - `catch (Exception e)` или `catch (Throwable t)` запрещены, кроме корня async/scheduled задачи.
   - Если ловишь несколько — используй multi-catch: `catch (IOException | SQLException e)`.

3. **Не глотай молча** — пустой `catch {}` и `catch-and-return-null` — критические антипаттерны.
   - Минимум: `log.warn(...)` или `log.error(...)` с контекстом (id, аргументы).
   - Лучше: добавь действие (FAILED-лог, метрика, fallback).

4. **Лови на правильном уровне:**
   - HTTP-слой: централизованно через `@RestControllerAdvice` (см. GlobalExceptionHandler выше).
   - Сервисы: бросают доменные exceptions, не ловят свои.
   - Async/scheduled корни: ловят всё, что может прилететь, и логируют.
   - Per-item обработчик в batch: ловит per-item, гарантирует аудит-запись, не прерывает batch.

5. **Контекст в сообщении** — id, аргументы, состояние. Чтобы по логу можно было воспроизвести.

6. **Не дублируй логирование** — log-and-rethrow на каждом уровне создаёт шум. Логируй один раз, на уровне обработки.

7. **Checked vs unchecked:**
   - Checked для recoverable условий (вызывающий может что-то сделать).
   - Runtime для programming errors (баги, нарушение контракта).
   - Все наши доменные exceptions — runtime (наследуются от `EventManagerException`).

### Spring-специфичные правила

- **`@RestControllerAdvice`** — централизованный обработчик HTTP-ошибок. В контроллерах НЕ должно быть try-catch (кроме редких случаев).
- **`@Transactional` НЕ распространяется через `@Async`** — async-метод стартует новую транзакцию.
- **При `@Transactional` rollback** — runtime exceptions откатывают, checked exceptions по умолчанию НЕТ. Используй `rollbackFor = Exception.class` если нужно откатить на checked.

### Per-item обработчик в batch loop

Это ОТДЕЛЬНЫЙ паттерн (например, рассылка одного письма в массовой кампании). Здесь корректно ловить шире, потому что **failure одного item не должен прерывать batch** и **каждый исход обязан оставить аудит-запись**.

```java
// ✅ Хорошо — per-item try-catch с конкретными действиями
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void sendNotification(Template t, Guest g, Campaign c) {
    if (alreadyLogged(c, g)) return;

    var log = createLogStub(t, g, c);
    try {
        prepareAndSend(t, g, log);  // подстановки + attachments + send в приватном методе
        log.setStatus(SENT);
    } catch (DataIntegrityViolationException e) {
        // UNIQUE-индекс сработал — другой воркер записал, это нормально
        return;
    } catch (RateLimitExceededException e) {
        log.setStatus(FAILED);
        log.setErrorMessage(e.getMessage());
        logRepository.save(log);
        throw e;  // re-throw — scheduler приостановит batch
    } catch (Exception e) {
        log.setStatus(FAILED);
        log.setErrorMessage(e.getMessage());
    }
    logRepository.save(log);
}
```

**Инвариант метода:** при любом исходе оставляет запись в `notification_logs`.
**Re-throw только специфические exceptions**, которые должны прервать batch (rate limit, kill switch).
**Не "оборачивай всё в один большой try"** — extract method для подготовки данных, try только вокруг подготовка+отправка.

### Альтернатива — Result-объект (предпочтительнее, если есть возможность)

```java
// "Отправка письма с известной вероятностью провала" — это не "исключительное" событие.
// Bloch: используй exceptions ТОЛЬКО для исключительного.
public SendResult trySend(Template t, Guest g) {  // никогда не бросает
    try {
        // ...
        return SendResult.ok();
    } catch (Exception e) {
        return SendResult.failed(e.getMessage());
    }
}

// Снаружи — без try-catch, просто проверка результата:
var result = sender.trySend(template, guest);
log.setStatus(result.isOk() ? SENT : FAILED);
log.setErrorMessage(result.error());
logRepository.save(log);
```

## Антипаттерны (запрещены)

| Антипаттерн | Почему плохо |
|---|---|
| Empty catch — `catch (Exception e) {}` | Часами ищешь баг, в логах пусто |
| Overly broad — `catch (Exception)` без надобности | Маскирует `NullPointerException`, прячет реальные баги |
| Log-and-rethrow на каждом уровне | Лог дублируется, шум в логах |
| Catch-and-return-null | Информация об ошибке потеряна навсегда |
| Catch без контекста — `log.error(e.getMessage())` | Без id/аргументов воспроизвести нельзя |
| Exception для control flow | В 2 раза медленнее обычной проверки |
| Wrap everything в try-catch ради «надёжности» | Бизнес-логика тонет в обработке, читаемость 0 |
| try-catch вокруг внешнего вызова без записи исхода в БД | При повторе обработка возьмёт того же гостя снова — спам |

## Жёсткие правила
- ЗАПРЕЩЕНО возвращать stack trace в production
- ЗАПРЕЩЕНО бросать generic `RuntimeException` — создавать специфичный Exception
- ЗАПРЕЩЕНО игнорировать ошибки (пустой `catch {}` блок)
- ЗАПРЕЩЕНО `catch (Exception e)` или `catch (Throwable)` без явной причины (только в корне async/scheduled)
- ЗАПРЕЩЕНО показывать технические детали пользователю — user-friendly сообщения
- ЗАПРЕЩЕНО использовать exceptions для control flow (циклы, валидация, проверка существования)
- ЗАПРЕЩЕНО `log.error(e.getMessage())` без контекста (id, аргументы)
- ОБЯЗАТЕЛЬНО per-item обработчик в batch loop оставляет след в БД при любом исходе
- ОБЯЗАТЕЛЬНО все новые exceptions наследуют от `EventManagerException`
- ОБЯЗАТЕЛЬНО Frontend: всегда обрабатывать error callback в subscribe
- ОБЯЗАТЕЛЬНО Frontend: `finalize()` для loading state, НЕ в error/next отдельно
- ОБЯЗАТЕЛЬНО Graceful degradation для token validation — `valid=false` вместо exception
