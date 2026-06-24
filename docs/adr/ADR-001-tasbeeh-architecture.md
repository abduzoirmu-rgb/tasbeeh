# ADR-001: Архитектура приложения «Тасбех»

**Статус:** Принято  
**Дата:** 2026-06-23

---

## Задача

Спроектировать автономное Android-приложение «Тасбех» (счётчик зикра) без backend. Хранение данных — только локально. Требуется поддерживаемая, тестируемая архитектура с возможностью расширения.

**Выбранный стек:** Kotlin · MVVM + Clean Architecture · Jetpack Compose · Room · Hilt · StateFlow

---

## Слои и компоненты

### Presentation (UI)
| Файл | Описание |
|---|---|
| `presentation/counter/CounterScreen.kt` | Главный экран счётчика |
| `presentation/counter/CounterViewModel.kt` | ViewModel счётчика, держит `UiState` |
| `presentation/history/HistoryScreen.kt` | Экран истории сессий |
| `presentation/history/HistoryViewModel.kt` | ViewModel истории |
| `presentation/settings/SettingsScreen.kt` | Экран настроек |
| `presentation/settings/SettingsViewModel.kt` | ViewModel настроек |
| `presentation/dhikr/DhikrListScreen.kt` | Список зикров |
| `presentation/dhikr/DhikrViewModel.kt` | ViewModel списка зикров |
| `presentation/navigation/NavGraph.kt` | Compose Navigation граф |
| `presentation/theme/Theme.kt` | Material3 тема (светлая/тёмная) |

### Domain (бизнес-логика)
| Файл | Описание |
|---|---|
| `domain/model/Dhikr.kt` | Модель зикра |
| `domain/model/Session.kt` | Модель сессии |
| `domain/model/Settings.kt` | Модель настроек |
| `domain/repository/DhikrRepository.kt` | Интерфейс репозитория зикров |
| `domain/repository/SessionRepository.kt` | Интерфейс репозитория сессий |
| `domain/repository/SettingsRepository.kt` | Интерфейс репозитория настроек |
| `domain/usecase/IncrementCounterUseCase.kt` | Увеличить счётчик, проверить цель |
| `domain/usecase/SaveSessionUseCase.kt` | Сохранить завершённую сессию |
| `domain/usecase/GetSessionsUseCase.kt` | Получить историю сессий |
| `domain/usecase/GetDhikrsUseCase.kt` | Получить список зикров |
| `domain/usecase/SaveDhikrUseCase.kt` | Сохранить/обновить зикр |

### Data (хранилище)
| Файл | Описание |
|---|---|
| `data/local/db/TasbeehDatabase.kt` | Room база данных |
| `data/local/db/dao/DhikrDao.kt` | DAO зикров |
| `data/local/db/dao/SessionDao.kt` | DAO сессий |
| `data/local/entity/DhikrEntity.kt` | Room entity зикра |
| `data/local/entity/SessionEntity.kt` | Room entity сессии |
| `data/local/datastore/SettingsDataStore.kt` | DataStore Preferences для настроек |
| `data/repository/DhikrRepositoryImpl.kt` | Реализация DhikrRepository |
| `data/repository/SessionRepositoryImpl.kt` | Реализация SessionRepository |
| `data/repository/SettingsRepositoryImpl.kt` | Реализация SettingsRepository |
| `data/mapper/DhikrMapper.kt` | Entity <-> Domain mapper |
| `data/mapper/SessionMapper.kt` | Entity <-> Domain mapper |

### DI
| Файл | Описание |
|---|---|
| `di/DatabaseModule.kt` | Provides Room DB, DAOs |
| `di/RepositoryModule.kt` | Binds интерфейсы к реализациям |
| `di/UseCaseModule.kt` | Provides UseCases |
| `TasbeehApplication.kt` | `@HiltAndroidApp` |

---

## Структура пакетов

```
com.tasbeeh.app/
├── data/
│   ├── local/
│   │   ├── db/
│   │   │   ├── TasbeehDatabase.kt
│   │   │   └── dao/
│   │   └── datastore/
│   ├── mapper/
│   ├── repository/
│   └── entity/
├── di/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
├── presentation/
│   ├── counter/
│   ├── dhikr/
│   ├── history/
│   ├── settings/
│   ├── navigation/
│   └── theme/
└── TasbeehApplication.kt
```

---

## Room Database Schema

**Версия БД:** 1  
**Имя файла:** `tasbeeh.db`

### Таблица `dhikrs`
| Поле | Тип | Ограничение |
|---|---|---|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT |
| `name` | TEXT | NOT NULL |
| `arabic_text` | TEXT | NULLABLE |
| `target_count` | INTEGER | NOT NULL DEFAULT 33 |
| `is_custom` | INTEGER (Boolean) | NOT NULL DEFAULT 0 |

Предзаполнение (Room `RoomDatabase.Callback`): Субханаллах (33), Альхамдулиллях (33), Аллаху Акбар (34).

### Таблица `sessions`
| Поле | Тип | Ограничение |
|---|---|---|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT |
| `dhikr_id` | INTEGER | FOREIGN KEY → dhikrs(id) ON DELETE SET NULL |
| `dhikr_name` | TEXT | NOT NULL (snapshot имени) |
| `count` | INTEGER | NOT NULL |
| `target` | INTEGER | NOT NULL |
| `completed` | INTEGER (Boolean) | NOT NULL DEFAULT 0 |
| `timestamp` | INTEGER | NOT NULL (Unix ms) |

---

## Интерфейсы репозиториев

```kotlin
// DhikrRepository.kt
interface DhikrRepository {
    fun getAllDhikrs(): Flow<List<Dhikr>>
    suspend fun getDhikrById(id: Long): Dhikr?
    suspend fun saveDhikr(dhikr: Dhikr): Long
    suspend fun deleteDhikr(id: Long)
}

// SessionRepository.kt
interface SessionRepository {
    fun getAllSessions(): Flow<List<Session>>
    suspend fun saveSession(session: Session)
    suspend fun deleteSession(id: Long)
}

// SettingsRepository.kt
interface SettingsRepository {
    val settings: Flow<Settings>
    suspend fun updateVibration(enabled: Boolean)
    suspend fun updateClickSound(enabled: Boolean)
    suspend fun updateTheme(isDark: Boolean)
}
```

---

## Зависимости между компонентами

```
CounterViewModel
  -> IncrementCounterUseCase -> (нет репозитория, чистая логика)
  -> SaveSessionUseCase      -> SessionRepository -> SessionDao
  -> GetDhikrsUseCase        -> DhikrRepository   -> DhikrDao
  -> SettingsRepository      -> SettingsDataStore

HistoryViewModel
  -> GetSessionsUseCase -> SessionRepository -> SessionDao

DhikrViewModel
  -> GetDhikrsUseCase / SaveDhikrUseCase -> DhikrRepository -> DhikrDao

SettingsViewModel
  -> SettingsRepository -> SettingsDataStore
```

Вибрация: `CounterViewModel` вызывает `VibrationManager` (системный сервис, обёрнутый в `VibrationManager.kt` в пакете `presentation/util/`) при `count == target`, если вибрация включена в настройках.

---

## Что developer должен реализовать

1. **Room DB** — создать `TasbeehDatabase`, два Entity, два DAO, `RoomDatabase.Callback` для предзаполнения.
2. **DataStore** — `SettingsDataStore` с тремя ключами (`vibration_enabled`, `click_sound`, `is_dark_theme`).
3. **Mappers** — extension-функции `DhikrEntity.toDomain()` / `Dhikr.toEntity()` и аналог для Session.
4. **Repositories** — реализовать три интерфейса, пробросить Flow из DAO/DataStore.
5. **UseCases** — `IncrementCounterUseCase` содержит логику: `newCount = current + 1`, возвращает `CounterResult(count, isGoalReached)`. Остальные — делегируют репозиторию.
6. **CounterViewModel** — `UiState(count, target, selectedDhikr, isGoalReached)`, методы `onTap()`, `onReset()`, `onSaveSession()`, `onSelectDhikr(id)`.
7. **Hilt модули** — `DatabaseModule` (`@Singleton` DB, DAOs), `RepositoryModule` (`@Binds`), `UseCaseModule`.
8. **NavGraph** — четыре маршрута: `counter`, `dhikr_list`, `history`, `settings`. BottomNavigation или TopAppBar с иконками.
9. **CounterScreen** — большая кнопка (минимум 180dp), отображение `count / target`, кнопка сброса, выбор зикра.
10. **VibrationManager** — использовать `VibrationEffect.createOneShot(200, DEFAULT_AMPLITUDE)` для API 26+.

---

## Что tester должен проверить

| # | Сценарий | Ожидаемый результат |
|---|---|---|
| T1 | Тап на кнопку | Счётчик увеличивается на 1 |
| T2 | Счётчик достигает цели | Вибрация (если включена), визуальный индикатор |
| T3 | Сброс счётчика | Счётчик = 0, сессия не сохраняется |
| T4 | Сохранение сессии | Запись появляется в истории с датой и именем зикра |
| T5 | Выбор зикра из списка | Цель обновляется согласно `target_count` зикра |
| T6 | Создание кастомного зикра | Появляется в списке, доступен для выбора |
| T7 | Отключение вибрации в настройках | При достижении цели вибрации нет |
| T8 | Смена темы | UI переключается без перезапуска Activity |
| T9 | Перезапуск приложения | Настройки и история сохранены |
| T10 | Удаление зикра | Связанные сессии не удаляются (dhikr_id = NULL) |
