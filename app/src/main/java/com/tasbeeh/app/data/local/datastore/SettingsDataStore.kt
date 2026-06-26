package com.tasbeeh.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tasbeeh.app.domain.model.Settings
import com.tasbeeh.app.domain.model.ThemeMode
import com.tasbeeh.app.domain.model.VibrationIntensity
import com.tasbeeh.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    companion object {
        val VIBRATION_ENABLED      = booleanPreferencesKey("vibration_enabled")
        val VIBRATION_INTENSITY    = stringPreferencesKey("vibration_intensity")
        val CLICK_SOUND            = booleanPreferencesKey("click_sound")
        val NOTIFICATION_SOUND     = stringPreferencesKey("notification_sound")
        val IS_DARK_THEME          = booleanPreferencesKey("is_dark_theme")
        val THEME_MODE             = stringPreferencesKey("theme_mode")
        val LANGUAGE               = stringPreferencesKey("language")
        val SHOW_TEXT_ON_SCREEN    = booleanPreferencesKey("show_text_on_screen")
        val PRIVACY_MODE           = booleanPreferencesKey("privacy_mode")
        val SMART_TOUCH            = booleanPreferencesKey("smart_touch")
        val AUTO_REPEAT            = booleanPreferencesKey("auto_repeat")
        val KEEP_SCREEN_ON         = booleanPreferencesKey("keep_screen_on")
        val DAILY_GOAL_ENABLED     = booleanPreferencesKey("daily_goal_enabled")
        val DAILY_GOAL_VALUE       = intPreferencesKey("daily_goal_value")
        val IS_PREMIUM_USER        = booleanPreferencesKey("is_premium_user")
        val SELECTED_TASBIH_TYPE   = intPreferencesKey("selected_tasbih_type_id")
        val SELECTED_BACKGROUND_ID = intPreferencesKey("selected_background_id")
        val SELECTED_DHIKR_INDEX   = intPreferencesKey("selected_dhikr_index")
        val LAST_OPEN_TIME         = longPreferencesKey("last_open_time")
        val WIDGET_COUNT           = intPreferencesKey("widget_count")
        val WIDGET_DHIKR           = stringPreferencesKey("widget_dhikr")
    }

    override fun getSettings(): Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            vibrationEnabled    = prefs[VIBRATION_ENABLED]      ?: true,
            vibrationIntensity  = prefs[VIBRATION_INTENSITY]
                ?.let { runCatching { VibrationIntensity.valueOf(it) }.getOrNull() }
                ?: VibrationIntensity.MEDIUM,
            clickSound          = prefs[CLICK_SOUND]             ?: false,
            notificationSound   = prefs[NOTIFICATION_SOUND]      ?: "default",
            isDarkTheme         = prefs[IS_DARK_THEME]           ?: false,
            themeMode           = prefs[THEME_MODE]
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            language            = prefs[LANGUAGE]                ?: "ru",
            showTextOnScreen    = prefs[SHOW_TEXT_ON_SCREEN]     ?: true,
            privacyMode         = prefs[PRIVACY_MODE]            ?: false,
            smartTouch          = prefs[SMART_TOUCH]             ?: false,
            autoRepeat          = prefs[AUTO_REPEAT]             ?: false,
            keepScreenOn        = prefs[KEEP_SCREEN_ON]          ?: false,
            dailyGoalEnabled    = prefs[DAILY_GOAL_ENABLED]      ?: false,
            dailyGoalValue      = prefs[DAILY_GOAL_VALUE]        ?: 100,
            isPremiumUser       = prefs[IS_PREMIUM_USER]         ?: false,
            selectedTasbihTypeId = prefs[SELECTED_TASBIH_TYPE]  ?: 1,
            selectedBackgroundId = prefs[SELECTED_BACKGROUND_ID] ?: 0,
            selectedDhikrIndex   = prefs[SELECTED_DHIKR_INDEX]   ?: 0
        )
    }

    override suspend fun updateSettings(settings: Settings) {
        context.dataStore.edit { prefs ->
            prefs[VIBRATION_ENABLED]      = settings.vibrationEnabled
            prefs[VIBRATION_INTENSITY]    = settings.vibrationIntensity.name
            prefs[CLICK_SOUND]            = settings.clickSound
            prefs[NOTIFICATION_SOUND]     = settings.notificationSound
            prefs[IS_DARK_THEME]          = settings.isDarkTheme
            prefs[THEME_MODE]             = settings.themeMode.name
            prefs[LANGUAGE]               = settings.language
            prefs[SHOW_TEXT_ON_SCREEN]    = settings.showTextOnScreen
            prefs[PRIVACY_MODE]           = settings.privacyMode
            prefs[SMART_TOUCH]            = settings.smartTouch
            prefs[AUTO_REPEAT]            = settings.autoRepeat
            prefs[KEEP_SCREEN_ON]         = settings.keepScreenOn
            prefs[DAILY_GOAL_ENABLED]     = settings.dailyGoalEnabled
            prefs[DAILY_GOAL_VALUE]       = settings.dailyGoalValue
            prefs[IS_PREMIUM_USER]        = settings.isPremiumUser
            prefs[SELECTED_TASBIH_TYPE]   = settings.selectedTasbihTypeId
            prefs[SELECTED_BACKGROUND_ID] = settings.selectedBackgroundId
            prefs[SELECTED_DHIKR_INDEX]   = settings.selectedDhikrIndex
        }
    }

    override suspend fun updateVibration(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[VIBRATION_ENABLED] = enabled }
    }

    override suspend fun updateClickSound(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[CLICK_SOUND] = enabled }
    }

    override suspend fun updateTheme(isDark: Boolean) {
        context.dataStore.edit { prefs -> prefs[IS_DARK_THEME] = isDark }
    }

    override suspend fun updateLanguage(language: String) {
        context.dataStore.edit { prefs -> prefs[LANGUAGE] = language }
    }

    suspend fun updateLastOpenTime(timeMs: Long) {
        context.dataStore.edit { prefs -> prefs[LAST_OPEN_TIME] = timeMs }
    }

    suspend fun updateWidgetState(count: Int, dhikrName: String) {
        context.dataStore.edit { prefs ->
            prefs[WIDGET_COUNT] = count
            prefs[WIDGET_DHIKR] = dhikrName
        }
    }
}
