# API Design

## URL-структура
```
Вложенные ресурсы (основной паттерн):
  /events/{eventId}/guests
  /events/{eventId}/locations
  /events/{eventId}/invitations
  /events/{eventId}/badge-templates
  /events/{eventId}/attendance/stats

Корневые ресурсы:
  /users, /countries, /tariffs

Действия (глагол после ресурса):
  /events/{eventId}/invitations/invite     — POST
  /events/{eventId}/invitations/accept     — POST
  /events/{eventId}/invitations/decline    — POST
  /pairing/pair                            — POST
  /pairing/validate                        — POST

Публичные (без авторизации):
  /invitations/pending/token-info?inviteToken=ABC  — GET
  /invitations/accepted?inviteToken=ABC            — GET
  /quota/**                                        — GET
```

## EventId — передача в API
JwtRequestFilter автоматически извлекает eventId из:
1. Path: `/events/{eventId}/...` (regex)
2. Query: `?eventId=123` (приоритет)

Без eventId → `SessionUtils.currentUser().getEventId()` вернёт null.

```
// ПРАВИЛЬНО:
GET  /events/123/guests
POST /events/123/invitations/invite
GET  /endpoint?eventId=123

// НЕПРАВИЛЬНО:
GET  /guests  (eventId не дойдёт до контекста)
```

## HTTP-методы и коды ответа
```
GET    → 200 OK + body
POST   → 200 OK + body (большинство) или 201 CREATED + Location header
PUT    → 200 OK + body
PATCH  → 200 OK + body
DELETE → 204 NO_CONTENT (без body) или 200 OK

Ошибки:
400 BAD_REQUEST       — бизнес-ошибка, невалидный ввод
401 UNAUTHORIZED      — нет JWT или невалидный
403 FORBIDDEN         — нет доступа к event
404 NOT_FOUND         — ресурс не найден
409 CONFLICT          — конфликт состояния (payment, quota, delete)
422 UNPROCESSABLE     — ошибки валидации (MethodArgumentNotValid)
503 SERVICE_UNAVAILABLE — внешний сервис недоступен
```

## DTO-конвенции
```
Входные (request body):
  *Request     — InvitationRequest, GuestPatchRequest, AuthenticationRequest
  *Dto (вход)  — QuotaDto, EventFieldDto (используются для POST/PUT)

Выходные (response body):
  *Response    — EventFieldResponse, GuestLocationResponse, AuthenticationResponse
  *Dto (выход) — CountryDto, LocationDto, TariffDto

Внутренние:
  *Projection  — BadgeTemplateOptionProjection, GuestInfoProjection (JPA интерфейсы)
  *Info        — InviteTokenInfo, InviteAcceptedInfo (простые read-only DTO)
```

### Аннотации на DTO
```java
// Request DTO:
@Data                              — Lombok getter/setter
@NotNull, @NotBlank, @Email        — Bean Validation
@Valid                             — рекурсивная валидация вложенных
@Size(min, max)                    — ограничение размера

// Response DTO:
@Data @Builder                     — Lombok с builder pattern
@JsonInclude(NON_NULL)             — не отправлять null поля
@JsonProperty("isPaid")            — кастомное имя поля
@JsonFormat(pattern = "dd.MM.yyyy HH:mm") — формат даты
```

## Контроллеры — паттерн
```java
@RestController
@RequestMapping("/events/{eventId}/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<List<ResourceDto>> getAll(
            @PathVariable Long eventId,
            GetAllRequestParams params) {     // пагинация + фильтры
        return ResponseEntity.ok(service.getAll(eventId, params));
    }

    @PostMapping
    public ResponseEntity<ResourceDto> create(
            @PathVariable Long eventId,
            @Valid @RequestBody ResourceDto dto) {
        return ResponseEntity.ok(service.create(eventId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceDto> update(
            @PathVariable Long eventId,
            @PathVariable Long id,
            @Valid @RequestBody ResourceDto dto) {
        return ResponseEntity.ok(service.update(eventId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long eventId,
            @PathVariable Long id) {
        service.delete(eventId, id);
        return ResponseEntity.noContent().build();
    }
}
```

## Пагинация и фильтрация
```
Через GetAllRequestParams (кастомный класс):
  ?page=1&size=20&sort=id,desc&sort=name,asc

  - page — 1-indexed (внутри конвертируется в 0-indexed PageRequest)
  - sort — property,direction (можно несколько)
  - extraParams — динамические фильтры: ?quotaId=5&status=ACTIVE

Specification Pattern:
  - GuestSpecification, EventSpecification, UserEventSpecification
  - Предустановленные ключи: eventId, locationId, countryId, role, invitationStatus
  - Остальные → LIKE фильтр по атрибуту
```

## Multipart — загрузка файлов
```java
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<EventDto> create(
    @RequestPart("request") EventDto dto,     // JSON часть
    @RequestPart("files") List<MultipartFile> files) {  // файлы
    return ResponseEntity.ok(service.create(dto, files));
}
```

## Content-Type
```
Стандарт:     APPLICATION_JSON (большинство endpoints)
Файлы:        MULTIPART_FORM_DATA (upload)
Auth:         APPLICATION_FORM_URLENCODED (/authenticate)
PDF:          APPLICATION_PDF (генерация бейджей)
Изображения:  IMAGE_PNG (QR-коды)
Скачивание:   APPLICATION_OCTET_STREAM (Excel экспорт)
```

## Location Header (для 201 CREATED)
```java
URI location = ServletUriComponentsBuilder
    .fromCurrentRequest()
    .path("/{id}")
    .buildAndExpand(created.getId())
    .toUri();
return ResponseEntity.created(location).body(created);
```

## Жёсткие правила
- ЗАПРЕЩЕНО возвращать Entity напрямую — только через DTO/Response
- ЗАПРЕЩЕНО использовать @RequestBody для GET запросов
- ЗАПРЕЩЕНО создавать endpoint без eventId если нужен контекст события
- Controller вызывает ТОЛЬКО сервис — никакой бизнес-логики в контроллере
- @Valid на @RequestBody обязательно для POST/PUT
- DELETE возвращает 204 NO_CONTENT (без body)
