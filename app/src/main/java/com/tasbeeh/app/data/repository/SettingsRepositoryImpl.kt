package com.tasbeeh.app.data.repository

import com.tasbeeh.app.data.local.datastore.SettingsDataStore
import com.tasbeeh.app.domain.model.Settings
import com.tasbeeh.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore
) : SettingsRepository {

    override fun getSettings(): Flow<Settings> = dataStore.getSettings()

    override suspend fun updateSettings(settings: Settings) {
        dataStore.updateSettings(settings)
    }

    override suspend fun updateVibration(enabled: Boolean) {
        dataStore.updateVibration(enabled)
    }

    override suspend fun updateClickSound(enabled: Boolean) {
        dataStore.updateClickSound(enabled)
    }

    override suspend fun updateTheme(isDark: Boolean) {
        dataStore.updateTheme(isDark)
    }

    override suspend fun updateLanguage(language: String) {
        dataStore.updateLanguage(language)
    }
}
