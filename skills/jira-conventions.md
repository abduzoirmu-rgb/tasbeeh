# Jira Conventions — Eventy / EB project

Конвенции работы с Jira проектом `EB` (eventy.biz). Используется агентом `pm-tracker`.

## Проект и доступ

- **Site:** `abadtj.atlassian.net`
- **Cloud ID:** `879635ee-67fb-41e8-a683-4121bf04b26d`
- **Project key:** `EB`
- **Credentials:** `~/.atlassian/credentials` (chmod 600). НИКОГДА не коммитить.

## Иерархия задач

```
Epic (рыночное направление / квартал)
  └─ Story (фича целиком, может иметь UI + API + БД)
       └─ Task (конкретная единица работы для одного агента/dev)
            └─ Subtask (под-шаг, редко используется)
       └─ Task
  └─ Story
```

**Правило:** одна задача = одна реализуемая единица в рамках 1 PR. Если в ADR описана фича на > 1 PR — разбей на Story + Tasks.

## Issue Types (доступные в EB)

| Type | Когда использовать |
|------|-------------------|
| **Task** | Обычная единица работы, ~1 PR, ~1-3 дня |
| **Story** | Фича в несколько Task'ов, ~1 спринт |
| **Bug** | Дефект в продукте |
| **Epic** | Стратегическое направление, недели/месяцы |
| **Subtask** | Под-шаг крупной Story; редко |

## Naming convention для summary

Формат: `[<area>/<sub-area>] <Что делается>`

Примеры:
- `[Marketing/Explore] Lead-magnet блок на detail-странице curated-ивента`
- `[Backend/Auth] OAuth2 invite flow — обработка pending invitations`
- `[Frontend/Dashboard] Виджет статистики ивента — карточки conversion`
- `[Infra] Включить Flyway миграции (production)`

**Areas (текущие):** Marketing, Backend, Frontend, Infra, Bug, Refactor, Docs.

**Лимит:** 80 символов в summary. Если не помещается — переноси детали в description.

## Структура description

Всегда markdown, всегда такая структура:

```markdown
## Контекст

Откуда задача (ADR-ссылка, marketing-документ, инцидент, запрос пользователя).

## Цель

Что должно быть после реализации. 1-2 предложения.

## Что сделать

Чёткие шаги. Список файлов/компонентов. Без воды.

Backend:
- ...

Frontend:
- ...

## Acceptance criteria

- [ ] Поведенческий критерий 1 (observable, testable)
- [ ] Критерий 2
- [ ] Покрыто E2E-тестом X

## Связь

- ADR: `docs/adr/ADR-XXX-...md`
- UI Spec: `docs/ui-specs/...md`
- Зависит от: EB-XX
- Блокирует: EB-YY
```

## Labels — обязательные

Каждая задача должна иметь **минимум 2 labels**:

1. **Area label** (один из): `backend`, `frontend`, `infra`, `marketing`, `bug`, `docs`
2. **Feature/domain label** (один из):
   - `auth`, `dashboard`, `explore`, `events`, `notifications`, `invitations`
   - `analytics`, `seo`, `retention`, `growth`
   - `curated` (для всего связанного с curated events / ADR-030)
   - `concurrency`, `migrations`, `tests`

**Можно добавлять**: `tech-debt`, `quick-win`, `blocked`, `needs-design`, `needs-adr`.

## Acceptance criteria — формат

Каждый критерий — **observable behavior**, не «реализовать X».

❌ ПЛОХО: `[ ] Создан сервис UserService`
✅ ХОРОШО: `[ ] POST /users возвращает 201 с созданным юзером; повторный вызов с тем же email — 409`

❌ ПЛОХО: `[ ] Добавлен компонент Subscribe`
✅ ХОРОШО: `[ ] На /explore с активным фильтром виден баннер «Подписаться»; при пустых фильтрах баннер скрыт`

❌ ПЛОХО: `[ ] Покрытие тестами`
✅ ХОРОШО: `[ ] E2E: subscribe → создать ивент → пришло письмо с unsubscribe-ссылкой`

## Linking — типы связей

| Link type | Когда |
|-----------|-------|
| **blocks / is blocked by** | EB-A не может стартовать пока EB-B не закрыт |
| **relates to** | Связаны темой, не блокируют |
| **duplicates / is duplicated by** | Перерезаются по сути |
| **child of / parent of** | Story ↔ Task |

При создании задачи из ADR — обязательно линкуй между собой задачи из одного ADR (`relates to`).

## Workflow (statuses)

Стандартный workflow EB:

```
To Do  →  In Progress  →  In Review  →  Done
   ↑              ↓              ↓
   └─────  Blocked  ←────────────┘
```

Переходы:
- **To Do → In Progress** — когда разработчик начал
- **In Progress → In Review** — PR создан, тесты зелёные
- **In Review → Done** — PR смержен
- **Любой → Blocked** — выявлено внешнее препятствие, добавь label `blocked` и комментарий что блокирует

Используй `getTransitionsForJiraIssue` чтобы узнать актуальные ID для текущей задачи — workflow может отличаться по issue type.

## Комментарии — конвенции

**Хорошие комментарии:**
- Дизайн-решение и его обоснование (когда не очевидно из кода)
- Изменение acceptance criteria + причина
- Прикрепление артефакта со ссылкой на путь в репо
- Результаты тестов с разбивкой passed/failed
- При закрытии — короткий summary что сделано

**Плохие:**
- «Сделал», «Готово» (использовать transition)
- Пересказ кода
- Дублирование описания задачи

## Аттачи

MCP не поддерживает upload. Используй curl:

```bash
source ~/.atlassian/credentials
curl -s -u "$email:$token" -X POST \
  -H "X-Atlassian-Token: no-check" \
  -F "file=@/path/to/file.png" \
  "https://$site/rest/api/3/issue/EB-XX/attachments"
```

**Что прикреплять:**
- Mockups (PNG) — после designer
- Test reports (HTML/PDF) — после tester
- DB diagrams, sequence diagrams (PNG) — после architect

**Что НЕ прикреплять:**
- Исходный код
- `.pen` файлы (encrypted, бесполезны без Pencil)
- Большие файлы > 10 MB (используй ссылку на репо/cloud)

## JQL шаблоны для частых запросов

```
# Мои задачи в работе
assignee = currentUser() AND statusCategory != Done ORDER BY priority DESC

# Не закрытые задачи фичи curated
project = EB AND labels = curated AND status != Done

# Готовые к ревью
project = EB AND status = "In Review"

# Заблокированные сейчас
project = EB AND status = Blocked

# Созданные за последнюю неделю
project = EB AND created >= -7d ORDER BY created DESC
```

## Чек-лист перед созданием задачи

- [ ] Summary < 80 символов, формата `[Area/Sub] Что делается`
- [ ] Description содержит все 5 секций (Контекст / Цель / Что сделать / AC / Связь)
- [ ] Минимум 2 labels (area + feature)
- [ ] Acceptance criteria observable
- [ ] Если есть зависимость — создан Issue Link
- [ ] Если есть mockup — прикреплён через curl
- [ ] Issue type выбран корректно (Task / Story / Bug)
