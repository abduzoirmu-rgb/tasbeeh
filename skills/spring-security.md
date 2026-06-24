# Spring Security

## Архитектура безопасности проекта
- JWT (HMAC-SHA512, 24h expiry) + OAuth2 (Google SSO)
- STATELESS session policy (JWT в каждом запросе)
- JwtRequestFilter перед UsernamePasswordAuthenticationFilter

## JWT Flow
```
POST /authenticate (form-urlencoded: email, password)
  → BCrypt проверка
  → JwtTokenUtil.generateToken(subject=email, HS512, 24h)
  → AuthenticationResponse { jwt, userId, name, email, defaultEvent }

Каждый запрос:
  Header: Authorization: Bearer <JWT>
  → JwtRequestFilter извлекает username из JWT
  → Загружает UserDetailsAdapter
  → Валидирует подпись + expiration
  → Устанавливает SecurityContext
```

## EventId — КРИТИЧЕСКИ ВАЖНО
JwtRequestFilter автоматически извлекает eventId из запроса:
1. **Приоритет 1**: query-параметр `?eventId=123`
2. **Приоритет 2**: path через regex `(?:^/|/events?/)(\d+)(?:/|$)`

```
GET /events/123/guests     → eventId = 123 (из path)
GET /endpoint?eventId=456  → eventId = 456 (из query, приоритет)
GET /endpoint              → eventId = null
```

После извлечения eventId:
- Проверяется UserEvent связь (DECLINED и EXPIRED отсеиваются)
- При отсутствии связи → **403 Forbidden** (фильтр останавливает запрос)
- При наличии → `userDetails.setEventId(eventId)` → `SessionUtils.currentUser().getEventId()`

### Role Resolution (context-aware)
```java
// Роль определяется ПО ТЕКУЩЕМУ EVENT:
userDetails.getAuthorities()
  → user.getUserEvents().stream()
     .filter(ue -> ue.getEvent().getId().equals(getEventId()))
     .findFirst() → Role (OWNER, ADMIN_EVENT, EMPLOYEE, GUARD)

// Если eventId не установлен → fallback на default event
// Если нет связи с событием → Role.UNVERIFIED
```

## Публичные URL (whitelist в WebSecurityConfig)
```
POST /users                           — регистрация
POST /authenticate                    — логин
/oauth2/**, /login/**                 — Google OAuth2
/users/activate                       — верификация email
/users/password/**                    — сброс пароля
/invitations/pending/token-info       — info о pending-приглашении
/invitations/accepted                 — info о принятом приглашении
/api/public/telegram/**               — Telegram deep-links
/tariffs/webhook, /payments/webhook   — payment webhooks
/payments/status/**, /payments/*/receipt/** — публичный статус платежа
/pairing/pair, /pairing/validate, /pairing/qrcode — device pairing
/guests/load-image/**, /badge-templates/load-badge/* — ресурсы
/countries/load-image/**, /countries/*/flag
/public/**, /quota/**
/v3/api-docs/**, /swagger-ui*/**      — OpenAPI docs
/ws/**                                — WebSocket (JWT на handshake)
/health-check, /templates
```

## OAuth2 (Google SSO) + Invite Token Flow
```
1. CustomAuthorizationRequestResolver:
   - Читает ?invite_token из URL
   - Сохраняет в session: setAttribute("oauth2_pending_invite_token", token)

2. Google OAuth callback:
   - Spring конвертирует в OAuth2AuthenticationToken (НЕ OAuth2LoginAuthenticationToken!)

3. OAuth2AuthenticationSuccessHandler:
   - Читает invite_token из session.getAttribute()
   - СРАЗУ удаляет из session (cleanup)
   - Проверяет email совпадение (OAuth email vs invited email)
   - При совпадении → linkIfPendingExists() + redirect с JWT + inviteToken
   - При несовпадении → redirect с ssoError=email_mismatch
   - При expired → redirect с error=invite_expired
```

## Жёсткие правила
- ЗАПРЕЩЕНО: `authentication instanceof OAuth2LoginAuthenticationToken` — всегда false
- ЗАПРЕЩЕНО: ожидать eventId без передачи в path или query — будет null
- ЗАПРЕЩЕНО: использовать SessionUtils вне authenticated request (async, scheduled)
- ЗАПРЕЩЕНО: хранить пароль без BCrypt encode
- ЗАПРЕЩЕНО: добавлять custom header для eventId — поддерживаются только path и query
- Новый публичный endpoint → ОБЯЗАТЕЛЬНО добавить в WebSecurityConfig whitelist
- InvitationStatus проверка ОБЯЗАТЕЛЬНА при доступе к event (DECLINED, EXPIRED отсеиваются)

## WebSocket Security
```
1. /ws/** в whitelist (публичный endpoint)
2. HTTP Upgrade проходит через JwtRequestFilter → JWT валидация
3. WebSocket сессия наследует SecurityContext от HTTP handshake
4. STOMP сообщения от authenticated user
```

## Чеклист для нового endpoint
- [ ] Определить scope: публичный (whitelist) или приватный (authenticated)
- [ ] Нужен ли eventId? Если да → передать в path `/events/{id}/...` или `?eventId=id`
- [ ] Получить из контекста: `SessionUtils.currentUser().getEventId()`
- [ ] Какие роли имеют доступ? JwtRequestFilter проверяет связь, но не роль
- [ ] Если публичный → добавить в WebSecurityConfig.requestMatchers().permitAll()
