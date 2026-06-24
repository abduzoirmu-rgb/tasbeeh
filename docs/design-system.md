# Дизайн-система «Тасбех»
> Версия 1.0 · Android · Jetpack Compose · базовая единица 4dp

---

## 1. Цветовая палитра

### Светлая тема

| Токен | HEX | Описание |
|---|---|---|
| `color.primary` | `#2D6A4F` | Основной зелёный |
| `color.primary.light` | `#52B788` | Зелёный светлый |
| `color.primary.container` | `#D8F3DC` | Фон зелёного контейнера |
| `color.accent` | `#C9972B` | Золотой акцент |
| `color.accent.light` | `#E9C46A` | Золотой светлый |
| `color.accent.container` | `#FFF3CD` | Фон золотого контейнера |
| `color.background` | `#FAFAF7` | Основной фон (бежевый белый) |
| `color.surface` | `#FFFFFF` | Поверхность карточек |
| `color.surface.variant` | `#F2EFE9` | Альтернативная поверхность |
| `color.on.background` | `#1B1B1B` | Текст на фоне |
| `color.on.surface` | `#2C2C2C` | Текст на поверхности |
| `color.on.surface.variant` | `#6B6B6B` | Второстепенный текст |
| `color.outline` | `#C8C4BB` | Разделители, обводки |
| `color.error` | `#B00020` | Ошибки |

### Тёмная тема

| Токен | HEX | Описание |
|---|---|---|
| `color.dark.primary` | `#74C69D` | Основной зелёный (тёмная) |
| `color.dark.primary.container` | `#1B4332` | Контейнер зелёного |
| `color.dark.accent` | `#E9C46A` | Золотой акцент (тёмная) |
| `color.dark.accent.container` | `#4A3800` | Контейнер золотого |
| `color.dark.background` | `#121212` | Основной фон |
| `color.dark.surface` | `#1E1E1E` | Поверхность карточек |
| `color.dark.surface.variant` | `#2A2A2A` | Альтернативная поверхность |
| `color.dark.on.background` | `#E8E8E4` | Текст на фоне |
| `color.dark.on.surface` | `#D4D4CF` | Текст на поверхности |
| `color.dark.on.surface.variant` | `#9A9A94` | Второстепенный текст |
| `color.dark.outline` | `#3D3D38` | Разделители |

---

## 2. Типографика

Основной шрифт: **Nunito** (Google Fonts)  
Числовой дисплей: **Nunito ExtraBold** (для счётчика)  
Арабский текст: **Amiri** (Google Fonts)

| Токен | Шрифт | Размер | Вес | Line Height | Использование |
|---|---|---|---|---|---|
| `text.display.counter` | Nunito | 96sp | ExtraBold 800 | 1.0 | Число счётчика |
| `text.display.goal` | Nunito | 32sp | SemiBold 600 | 1.2 | Цель сессии |
| `text.headline.large` | Nunito | 28sp | Bold 700 | 1.3 | Заголовки экранов |
| `text.headline.medium` | Nunito | 22sp | SemiBold 600 | 1.3 | Подзаголовки |
| `text.title` | Nunito | 18sp | SemiBold 600 | 1.4 | Названия карточек |
| `text.body.large` | Nunito | 16sp | Regular 400 | 1.5 | Основной текст |
| `text.body.medium` | Nunito | 14sp | Regular 400 | 1.5 | Второстепенный текст |
| `text.label` | Nunito | 12sp | Medium 500 | 1.4 | Метки, подписи |
| `text.arabic.large` | Amiri | 24sp | Regular 400 | 1.6 | Зикр на арабском |
| `text.arabic.medium` | Amiri | 18sp | Regular 400 | 1.6 | Зикр в списке |

---

## 3. Сетка и отступы

Базовая единица: **4dp**

| Токен | Значение | Использование |
|---|---|---|
| `spacing.xs` | 4dp | Внутренние мини-отступы |
| `spacing.sm` | 8dp | Отступы внутри компонентов |
| `spacing.md` | 12dp | Стандартный внутренний отступ |
| `spacing.lg` | 16dp | Padding экрана (горизонтальный) |
| `spacing.xl` | 24dp | Отступы между секциями |
| `spacing.2xl` | 32dp | Крупные отступы |
| `spacing.3xl` | 48dp | Экстра-крупные отступы |

### Скруглённые углы (Corner Radius)

| Токен | Значение | Использование |
|---|---|---|
| `radius.xs` | 4dp | Мелкие чипы |
| `radius.sm` | 8dp | Кнопки второго уровня |
| `radius.md` | 12dp | Карточки |
| `radius.lg` | 16dp | BottomSheet, диалоги |
| `radius.xl` | 24dp | Floating-элементы |
| `radius.full` | 50% | Счётчик-кнопка (круглая) |

### Высота компонентов

| Токен | Значение |
|---|---|
| `size.top.bar` | 56dp |
| `size.bottom.nav` | 64dp |
| `size.counter.button` | 240dp (диаметр) |
| `size.zikr.card` | min 72dp |
| `size.history.item` | 64dp |
| `size.settings.row` | 56dp |
| `size.preset.chip` | 40dp высота |

---

## 4. Тени и Elevation

| Токен | Elevation | Применение |
|---|---|---|
| `elevation.none` | 0dp | Фоновые поверхности |
| `elevation.low` | 1dp | Карточки в списке |
| `elevation.medium` | 4dp | Плавающие кнопки |
| `elevation.high` | 8dp | BottomSheet, счётчик-кнопка |

---

## 5. Иконки

Набор: **Material Symbols Rounded** (weight 300, grade 0, optical size 24)

| Иконка | Material Symbol | Использование |
|---|---|---|
| Счётчик | `radio_button_unchecked` / custom | Главный экран (nav) |
| Список зикров | `format_list_bulleted` | Навигация |
| История | `history` | Навигация |
| Настройки | `settings` | Навигация |
| Вибрация вкл | `vibration` | Настройки |
| Вибрация выкл | `phone_disabled` | Настройки |
| Звук | `volume_up` | Настройки |
| Тема | `dark_mode` | Настройки |
| Добавить | `add` | FAB |
| Удалить | `delete_outline` | Свайп-действие |
| Сброс | `refresh` | Кнопка сброса счётчика |

---

## 6. Компоненты

### 6.1 CounterButton

Главный интерактивный элемент приложения.

```
Форма:         Круглая, диаметр size.counter.button (240dp)
Фон:           color.primary.container → радиальный градиент к color.primary.light (центр)
Обводка:       2dp, color.accent (золотой), с внешним свечением
Текст:         text.display.counter, color.primary
Тень:          elevation.high

Состояния:
  - Default:   как описано выше
  - Pressed:   scale 0.92, brightness +10%, ripple color.primary (400ms spring)
  - Goal met:  фон → color.accent.container, пульсация 3 раза (400ms), вибрация
  - Dark:      фон color.dark.primary.container, обводка color.dark.accent
```

**Анимация нажатия:**
1. `scaleDown` до 0.92 за 80ms (easeIn)
2. Haptic feedback (короткий импульс)
3. `scaleUp` до 1.0 за 200ms (spring damping 0.6)
4. Число обновляется в момент нажатия с fade 50ms

### 6.2 ZikrCard

Карточка зикра в списке выбора.

```
Размер:        Ширина — fullWidth − spacing.lg×2, высота — min 72dp
Фон:           color.surface
Радиус:        radius.md
Тень:          elevation.low
Padding:       spacing.md (12dp)
Обводка:       1dp color.outline (только для выбранного: 2dp color.primary)

Структура:
  ┌─────────────────────────────────────────┐
  │  [Арабский текст]             [●]       │  ← radio или checkmark
  │  [Транслитерация / рус. имя]           │
  │  [Цель: 33 / 99 / 100 / кастом]        │
  └─────────────────────────────────────────┘

Состояния:
  - Default:    обводка outline
  - Selected:   обводка color.primary, фон color.primary.container
  - Custom:     иконка "edit" вместо radio, дополнительная строка счётчика
```

### 6.3 HistoryItem

Элемент списка истории.

```
Высота:        size.history.item (64dp)
Padding:       spacing.lg горизонтальный, spacing.sm вертикальный
Разделитель:   1dp color.outline, indent от текста

Структура (горизонтальная):
  [Иконка даты 24dp] · [Название зикра + дата]  ·  [Число / цель]

Типографика:
  - Зикр:      text.title
  - Дата:      text.body.medium, color.on.surface.variant
  - Число:     text.headline.medium, color.primary (выровнено вправо)
  - Цель:      text.label, color.accent

Состояния:
  - Default:   как описано
  - Swipe left: показывает кнопку "Удалить" (color.error фон, icon delete)
```

### 6.4 PresetChip

Чип выбора цели (33 / 99 / 100 / Custom).

```
Высота:        size.preset.chip (40dp)
Padding:       spacing.sm × spacing.lg
Радиус:        radius.full
Фон Default:   color.surface.variant
Фон Selected:  color.primary
Текст Default: text.label, color.on.surface.variant
Текст Selected:text.label, White
```

### 6.5 SettingsToggle

Строка настройки с переключателем.

```
Высота:        size.settings.row (56dp)
Padding:       spacing.lg
Структура:     [Иконка 24dp] [Заголовок + подпись] [Switch]

Switch:
  On:   thumb color.primary, track color.primary.container
  Off:  thumb color.outline, track color.surface.variant

Типографика:
  Заголовок: text.body.large
  Подпись:   text.body.medium, color.on.surface.variant
```

### 6.6 TopBar

```
Высота:        56dp
Фон:           color.background (без тени, прозрачный при скролле)
Заголовок:     text.headline.medium, выровнен по центру
Actions:       иконки 24dp, touch area 48×48dp
```

### 6.7 BottomNavigation

```
Высота:        64dp + system nav insets
Фон:           color.surface
Тень:          elevation.medium (только верх)
Индикатор:     pill 64×32dp, color.primary.container
Иконка active: color.primary
Иконка inactive: color.on.surface.variant
Метка:         text.label
```

---

## 7. Паттерны взаимодействия

### Вибрация (Haptic)

| Событие | Тип | Описание |
|---|---|---|
| Тап по счётчику | `HapticFeedbackType.TextHandleMove` | Лёгкий клик |
| Достижение цели | `VibrationEffect.createWaveform([0,100,50,100])` | Двойной импульс |
| Смена зикра | `HapticFeedbackType.LongPress` | Подтверждение |
| Удаление | `HapticFeedbackType.LongPress` | Предупреждение |

### Переходы между экранами

| Переход | Анимация |
|---|---|
| BottomNav → любой экран | Fade через `AnimatedContent` (200ms) |
| Главный → ZikrList (выбор) | Slide up BottomSheet (300ms, spring) |
| Главный → Settings | Shared axis Z (forward 300ms) |
| Добавление зикра | BottomSheet expand (400ms) |

### Сброс счётчика

1. Длинное нажатие на счётчик — появляется диалог подтверждения
2. Диалог: "Сбросить счётчик?" + кнопки "Отмена" / "Сбросить"
3. При подтверждении — counter → 0 с анимацией flip (200ms)

---

## 8. Accessibility

- Минимальный touch target: **48×48dp**
- Контраст текст/фон: ≥ 4.5:1 (WCAG AA) для всех цветовых пар
- `contentDescription` для всех иконок и CounterButton
- Поддержка TalkBack: порядок фокуса сверху вниз
- Масштабирование шрифта: UI адаптируется до 150% font scale
- Счётчик озвучивает: "Значение: X из Y" при каждом тапе

---

## 9. Темизация в Compose

```kotlin
// MaterialTheme настройка
val TasbeehLightColors = lightColorScheme(
    primary        = Color(0xFF2D6A4F),
    onPrimary      = Color(0xFFFFFFFF),
    primaryContainer    = Color(0xFFD8F3DC),
    secondary      = Color(0xFFC9972B),
    secondaryContainer  = Color(0xFFFFF3CD),
    background     = Color(0xFFFAFAF7),
    surface        = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF2EFE9),
    outline        = Color(0xFFC8C4BB),
)

val TasbeehDarkColors = darkColorScheme(
    primary        = Color(0xFF74C69D),
    primaryContainer    = Color(0xFF1B4332),
    secondary      = Color(0xFFE9C46A),
    secondaryContainer  = Color(0xFF4A3800),
    background     = Color(0xFF121212),
    surface        = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2A2A2A),
    outline        = Color(0xFF3D3D38),
)
```
