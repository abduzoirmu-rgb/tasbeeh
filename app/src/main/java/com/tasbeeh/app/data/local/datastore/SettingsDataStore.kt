package com.tasbeeh.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tasbeeh.app.domain.model.Settings
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
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val CLICK_SOUND = booleanPreferencesKey("click_sound")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val LANGUAGE = stringPreferencesKey("language")
        val LAST_OPEN_TIME = longPreferencesKey("last_open_time")
        val WIDGET_COUNT = androidx.datastore.preferences.core.intPreferencesKey("widget_count")
        val WIDGET_DHIKR = stringPreferencesKey("widget_dhikr")
    }

    override val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            vibrationEnabled = prefs[VIBRATION_ENABLED] ?: true,
            clickSoundEnabled = prefs[CLICK_SOUND] ?: false,
            isDarkTheme = prefs[IS_DARK_THEME] ?: false,
            language = prefs[LANGUAGE] ?: "ru"
        )
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
