package com.tasbeeh.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasbeeh.app.domain.model.Settings
import com.tasbeeh.app.domain.repository.SessionRepository
import com.tasbeeh.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    val settings: StateFlow<Settings> = settingsRepository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Settings())

    /** Primary update — replaces the entire Settings object. */
    fun updateSettings(settings: Settings) {
        viewModelScope.launch { settingsRepository.updateSettings(settings) }
    }

    // Legacy granular helpers (kept for backward compatibility)
    fun updateVibration(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateVibration(enabled) }
    }

    fun updateClickSound(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateClickSound(enabled) }
    }

    fun updateTheme(isDark: Boolean) {
        viewModelScope.launch { settingsRepository.updateTheme(isDark) }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch { settingsRepository.updateLanguage(language) }
    }

    fun clearHistory() {
        viewModelScope.launch { sessionRepository.deleteAllSessions() }
    }
}
