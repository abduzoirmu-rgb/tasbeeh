# UI Spec — Тасбех (все экраны)
> Все размеры и цвета ссылаются на токены из `design-system.md`

---

## Навигация

**BottomNavigation** (постоянная, 4 таба):
`Счётчик` · `Зикры` · `История` · `Настройки`

Переходы: Fade `AnimatedContent` 200ms между табами.

---

## 1. MainScreen — Счётчик

### Макет (Portrait, 360dp+)

```
┌─────────────────────────────────┐
│         [Название зикра]        │  text.title, color.on.surface
│       [Арабский текст зикра]    │  text.arabic.large, center
├─────────────────────────────────┤
│                                 │  spacing.3xl сверху
│       ┌─────────────┐           │
│       │      X      │           │  CounterButton (240dp), center
│       └─────────────┘           │
│                                 │  spacing.xl снизу
│      [Прогресс-дуга: X / Y]     │  CircularProgressIndicator, color.accent
├─────────────────────────────────┤
│  [33]  [99]  [100]  [∞ Своё]    │  PresetChip ряд, spacing.lg padding
├─────────────────────────────────┤
│        [ Сбросить ]             │  TextButton, color.error, text.label
└─────────────────────────────────┘
         [BottomNavigation]
```

### Компоненты

| Компонент | Токены |
|---|---|
| Название зикра | `text.title`, `color.on.surface`, tap → ZikrListScreen |
| Арабский текст | `text.arabic.large`, `color.on.surface.variant`, `textAlign.Center` |
| CounterButton | см. компонент 6.1, диаметр 240dp |
| Прогресс-дуга | `strokeWidth` 6dp, track `color.outline`, progress `color.accent` |
| PresetChip × 4 | см. компонент 6.4, `spacing.sm` gap между чипами |
| Кнопка Сбросить | `TextButton`, `color.error`, `text.label` |

### Состояния

**Idle (по умолчанию):**
- Счётчик = 0, цель = 33 (первый пресет)
- Прогресс-дуга пустая
- CounterButton в Default-состоянии

**Counting (счёт идёт):**
- Число обновляется при каждом тапе
- Прогресс-дуга заполняется пропорционально
- При 50% цели: дуга меняет цвет → `color.accent.light`

**Goal Reached (цель достигнута):**
- CounterButton: состояние "Goal met" (золотой фон, пульсация)
- Вибрация: двойной импульс (если включена)
- Звук клика (если включён)
- Прогресс-дуга: полная, цвет `color.accent`
- Через 1.5с: автоматический сброс → Idle (если не ручной режим)

**No Goal (пресет "Своё", цель не задана):**
- Прогресс-дуга скрыта
- CounterButton без ограничения

### UX Flow

```
Пользователь открывает приложение
  → MainScreen с последним выбранным зикром
  → Тап по названию зикра → ZikrListScreen (BottomSheet)
  → Выбор пресета → обновляет цель, сброс счётчика
  → Тапы по CounterButton → инкремент + haptic
  → Достижение цели → уведомление + вибрация
  → Длинный тап → диалог сброса
```

---

## 2. ZikrListScreen — Список зикров

### Макет

```
┌─────────────────────────────────┐
│  ←  Выбор зикра             [+] │  TopBar + FAB-иконка добавления
├─────────────────────────────────┤
│  [ZikrCard: Субханаллах]        │
│  [ZikrCard: Альхамдулиллях]     │
│  [ZikrCard: Аллаху Акбар]       │
│  [ZikrCard: Ля иляха илляллах]  │
│  ─── Пользовательские ───       │  Divider + text.label, color.outline
│  [ZikrCard: Кастомный зикр]     │
│  ...                            │
└─────────────────────────────────┘
         [BottomNavigation]
```

### Компоненты

| Компонент | Описание |
|---|---|
| TopBar | Кнопка "Назад" (или просто заголовок при навигации табом), кнопка "+" |
| ZikrCard | см. компонент 6.2; выбранный — обводка `color.primary` |
| Divider | `color.outline` 1dp, label `text.label` "Пользовательские" |
| FAB / TopBar "+" | Открывает BottomSheet создания зикра |

### Состояния

**Default (стандартные зикры):**
- 4 предустановленных зикра, нередактируемые (иконка замка)
- Тап → выбирает, возвращает на MainScreen

**Empty custom (нет кастомных):**
- Секция "Пользовательские" скрыта
- Подсказка: `text.body.medium` "Нажмите + чтобы добавить свой зикр"

**Add Zikr BottomSheet:**
```
Поля: [Название] [Арабский текст (необязательно)] [Цель по умолчанию]
Кнопки: [Отмена] [Сохранить]
```

**Swipe left на кастомном зикре:** кнопка удаления (`color.error`)

---

## 3. HistoryScreen — История

### Макет

```
┌─────────────────────────────────┐
│           История               │  TopBar, без кнопки назад
├─────────────────────────────────┤
│  Сегодня, 23 июня               │  StickyHeader: text.label, color.accent
│  [HistoryItem]                  │
│  [HistoryItem]                  │
│  Вчера, 22 июня                 │  StickyHeader
│  [HistoryItem]                  │
│  ...                            │
└─────────────────────────────────┘
         [BottomNavigation]
```

### Компоненты

| Компонент | Описание |
|---|---|
| StickyHeader | `text.label`, `color.accent`, `background` полупрозрачный `color.background` |
| HistoryItem | см. компонент 6.3 |
| LazyColumn | Группировка по дням |

### Состояния

**Empty:**
```
Центрированная иллюстрация (icon: history 64dp, color.outline)
text.body.large: "Пока нет записей"
text.body.medium: "Начните первую сессию"
```

**Data:**
- Список сессий, сгруппированных по дате
- Swipe left → удаление с undo Snackbar (3 секунды)

**Данные HistoryItem:**
- Название зикра · Дата · Время начала
- Число (достигнуто) / Цель
- Индикатор: галочка если цель выполнена (`color.primary`), крестик если нет (`color.on.surface.variant`)

---

## 4. SettingsScreen — Настройки

### Макет

```
┌─────────────────────────────────┐
│          Настройки              │  TopBar
├─────────────────────────────────┤
│  ЗВУК И ВИБРАЦИЯ                │  GroupHeader: text.label, color.accent
│  [SettingsToggle: Вибрация]     │
│  [SettingsToggle: Звук клика]   │
│  [SettingsSlider: Громкость]    │  Visible only if Звук = on
├─────────────────────────────────┤
│  ВНЕШНИЙ ВИД                    │  GroupHeader
│  [SettingsToggle: Тёмная тема]  │
│  [SettingsSelect: Язык]         │  Dropdown: Русский / English / العربية
├─────────────────────────────────┤
│  ДАННЫЕ                         │  GroupHeader
│  [SettingsRow: Очистить историю]│  Деструктивный — color.error
├─────────────────────────────────┤
│  Версия 1.0.0                   │  text.label, color.on.surface.variant, center
└─────────────────────────────────┘
         [BottomNavigation]
```

### Компоненты

| Компонент | Токены |
|---|---|
| GroupHeader | `text.label`, uppercase, `color.accent`, `spacing.lg` padding top |
| SettingsToggle | см. компонент 6.5 |
| SettingsSlider | Track `color.primary`, thumb `color.primary`, высота 56dp |
| SettingsSelect | Trailing иконка `chevron_right`, открывает AlertDialog |
| SettingsRow (деструктивный) | Текст `color.error`, иконка `delete_outline` |

### Состояния

**Вибрация OFF:** строка звука клика задизейблена (`alpha 0.38`)

**Очистить историю (тап):**
- AlertDialog: "Удалить всю историю?"
- Кнопки: "Отмена" / "Удалить" (`color.error`)
- После подтверждения: Snackbar "История удалена"

**Тёмная тема Toggle:**
- Немедленное переключение темы без перезапуска
- Анимация: `AnimatedContent` crossfade 300ms

---

## Accessibility (общее)

| Требование | Реализация |
|---|---|
| Touch targets | Минимум 48×48dp для всех интерактивных элементов |
| TalkBack | `contentDescription` на CounterButton: "Тасбех, значение X из Y, нажмите для счёта" |
| Contrast | Все пары текст/фон ≥ 4.5:1 (WCAG AA) |
| Font scaling | Layout адаптируется до 150%, числа не обрезаются |
| Motion | Respect `prefers-reduced-motion` — отключает анимации при системной настройке |
| RTL | Поддержка LayoutDirection.Rtl для арабского интерфейса |
