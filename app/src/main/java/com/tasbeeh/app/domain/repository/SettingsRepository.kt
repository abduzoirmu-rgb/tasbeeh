package com.tasbeeh.app.domain.repository

import com.tasbeeh.app.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<Settings>
    suspend fun updateVibration(enabled: Boolean)
    suspend fun updateClickSound(enabled: Boolean)
    suspend fun updateTheme(isDark: Boolean)
    suspend fun updateLanguage(language: String)
}
