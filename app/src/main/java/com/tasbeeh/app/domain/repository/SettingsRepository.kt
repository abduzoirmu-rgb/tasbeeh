package com.tasbeeh.app.domain.repository

import com.tasbeeh.app.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    /** Primary contract: emits current settings as a stream. */
    fun getSettings(): Flow<Settings>

    suspend fun updateSettings(settings: Settings)

    // Legacy granular update methods
    suspend fun updateVibration(enabled: Boolean) {}
    suspend fun updateClickSound(enabled: Boolean) {}
    suspend fun updateTheme(isDark: Boolean) {}
    suspend fun updateLanguage(language: String) {}
}
