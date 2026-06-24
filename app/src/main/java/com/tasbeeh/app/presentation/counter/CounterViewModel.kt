package com.tasbeeh.app.presentation.counter

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasbeeh.app.data.local.datastore.SettingsDataStore
import com.tasbeeh.app.domain.model.Dhikr
import com.tasbeeh.app.domain.model.Session
import com.tasbeeh.app.domain.repository.DhikrRepository
import com.tasbeeh.app.domain.repository.SettingsRepository
import com.tasbeeh.app.domain.usecase.GetDhikrsUseCase
import com.tasbeeh.app.domain.usecase.IncrementCounterUseCase
import com.tasbeeh.app.domain.usecase.SaveSessionUseCase
import com.tasbeeh.app.presentation.util.VibrationManager
import com.tasbeeh.app.widget.TasbeehGlanceWidget
import com.tasbeeh.app.widget.WidgetCountKey
import com.tasbeeh.app.widget.WidgetDhikrKey
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CounterUiState(
    val count: Int = 0,
    val target: Int = 33,
    val selectedDhikr: Dhikr? = null,
    val isGoalReached: Boolean = false,
    val dhikrs: List<Dhikr> = emptyList(),
    val vibrationEnabled: Boolean = true
)

@HiltViewModel
class CounterViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val incrementCounterUseCase: IncrementCounterUseCase,
    private val saveSessionUseCase: SaveSessionUseCase,
    private val getDhikrsUseCase: GetDhikrsUseCase,
    private val dhikrRepository: DhikrRepository,
    private val settingsRepository: SettingsRepository,
    private val vibrationManager: VibrationManager,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(CounterUiState())
    val uiState: StateFlow<CounterUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                getDhikrsUseCase(),
                settingsRepository.settings
            ) { dhikrs, settings ->
                val current = _uiState.value
                val selectedDhikr = if (current.selectedDhikr == null && dhikrs.isNotEmpty()) {
                    dhikrs.first()
                } else {
                    current.selectedDhikr?.let { sel ->
                        dhikrs.find { it.id == sel.id } ?: sel
                    }
                }
                current.copy(
                    dhikrs = dhikrs,
                    selectedDhikr = selectedDhikr,
                    target = selectedDhikr?.targetCount ?: current.target,
                    vibrationEnabled = settings.vibrationEnabled
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun onTap() {
        val state = _uiState.value
        val result = incrementCounterUseCase(currentCount = state.count, target = state.target)
        _uiState.update { it.copy(count = result.count, isGoalReached = result.isGoalReached) }
        if (result.isGoalReached && state.vibrationEnabled) {
            vibrationManager.vibrateGoalReached()
        } else if (state.vibrationEnabled) {
            vibrationManager.vibrate()
        }
    }

    fun onReset() {
        _uiState.update { it.copy(count = 0, isGoalReached = false) }
    }

    fun onSaveSession() {
        val state = _uiState.value
        viewModelScope.launch {
            val session = Session(
                dhikrId = state.selectedDhikr?.id,
                dhikrName = state.selectedDhikr?.name ?: "Зикр",
                count = state.count,
                target = state.target,
                completed = state.isGoalReached,
                timestamp = System.currentTimeMillis()
            )
            saveSessionUseCase(session)
            updateWidget(state.count, state.selectedDhikr?.name ?: "")
            onReset()
        }
    }

    fun onSelectDhikr(dhikr: Dhikr) {
        _uiState.update {
            it.copy(
                selectedDhikr = dhikr,
                target = dhikr.targetCount,
                count = 0,
                isGoalReached = false
            )
        }
    }

    fun onSelectDhikr(id: Long) {
        viewModelScope.launch {
            val dhikr = dhikrRepository.getDhikrById(id) ?: return@launch
            onSelectDhikr(dhikr)
        }
    }

    fun onSetTarget(target: Int) {
        _uiState.update { it.copy(target = target, count = 0, isGoalReached = false) }
    }

    fun onAutoReset() {
        _uiState.update { it.copy(count = 0, isGoalReached = false) }
    }

    private suspend fun updateWidget(count: Int, dhikrName: String) {
        try {
            val manager = GlanceAppWidgetManager(context)
            val ids = manager.getGlanceIds(TasbeehGlanceWidget::class.java)
            for (id in ids) {
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[WidgetCountKey] = count
                        this[WidgetDhikrKey] = dhikrName
                    }
                }
                TasbeehGlanceWidget().update(context, id)
            }
        } catch (_: Exception) {
            // Widget not added to home screen — ignore
        }
    }
}
