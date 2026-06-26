package com.tasbeeh.app.presentation.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasbeeh.app.domain.model.Dhikr
import com.tasbeeh.app.domain.model.Session
import com.tasbeeh.app.domain.model.VibrationIntensity
import com.tasbeeh.app.domain.repository.SettingsRepository
import com.tasbeeh.app.domain.usecase.GetDhikrsUseCase
import com.tasbeeh.app.domain.usecase.SaveSessionUseCase
import com.tasbeeh.app.presentation.util.SoundManager
import com.tasbeeh.app.presentation.util.VibrationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------

data class CounterUiState(
    val dhikrs: List<Dhikr> = emptyList(),
    val selectedDhikrIndex: Int = 0,
    val count: Int = 0,
    val target: Int = 33,
    val isComplete: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val vibrationIntensity: VibrationIntensity = VibrationIntensity.MEDIUM,
    val selectedBackgroundId: Int = 0
) {
    val selectedDhikr: Dhikr? get() = dhikrs.getOrNull(selectedDhikrIndex)
    val isGoalReached: Boolean get() = isComplete
}

// ---------------------------------------------------------------------------
// Events
// ---------------------------------------------------------------------------

sealed interface CounterEvent {
    data object Increment : CounterEvent
    data object Reset : CounterEvent
    data class SelectDhikr(val index: Int) : CounterEvent
    data class SetTarget(val target: Int) : CounterEvent
}

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

@HiltViewModel
class CounterViewModel @Inject constructor(
    private val getDhikrsUseCase: GetDhikrsUseCase,
    private val saveSessionUseCase: SaveSessionUseCase,
    private val settingsRepository: SettingsRepository,
    private val vibrationManager: VibrationManager,
    private val soundManager: SoundManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CounterUiState())
    val uiState: StateFlow<CounterUiState> = _uiState

    init {
        viewModelScope.launch {
            getDhikrsUseCase().collect { dhikrs ->
                _uiState.update { current ->
                    val newIndex = current.selectedDhikrIndex.coerceAtMost(
                        (dhikrs.size - 1).coerceAtLeast(0)
                    )
                    current.copy(
                        dhikrs = dhikrs,
                        selectedDhikrIndex = newIndex,
                        target = dhikrs.getOrNull(newIndex)?.targetCount ?: current.target
                    )
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.getSettings().collect { s ->
                _uiState.update { current ->
                    current.copy(
                        vibrationEnabled     = s.vibrationEnabled,
                        vibrationIntensity   = s.vibrationIntensity,
                        selectedBackgroundId = s.selectedBackgroundId,
                        // Restore saved dhikr index only before dhikrs are loaded
                        selectedDhikrIndex   = if (current.dhikrs.isEmpty())
                            s.selectedDhikrIndex else current.selectedDhikrIndex,
                        // Restore saved target count (33/66/99/100/custom)
                        target = if (s.selectedTasbihTypeId > 1) s.selectedTasbihTypeId else current.target
                    )
                }
            }
        }
    }

    fun onEvent(event: CounterEvent) {
        when (event) {
            is CounterEvent.Increment   -> increment()
            is CounterEvent.Reset       -> reset()
            is CounterEvent.SelectDhikr -> selectDhikr(event.index)
            is CounterEvent.SetTarget   -> setTarget(event.target)
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private fun increment() {
        val state = _uiState.value
        if (state.isComplete) return

        val newCount = state.count + 1
        val complete = newCount >= state.target

        _uiState.update { it.copy(count = newCount, isComplete = complete) }

        soundManager.playClick()

        if (state.vibrationEnabled) {
            if (complete) vibrationManager.vibrateGoalReached()
            else vibrationManager.vibrate(state.vibrationIntensity)
        }

        if (complete) {
            persistSession(state, newCount)
        }
    }

    private fun reset() {
        _uiState.update { it.copy(count = 0, isComplete = false) }
    }

    private fun selectDhikr(index: Int) {
        val clamped = index.coerceIn(0, (_uiState.value.dhikrs.size - 1).coerceAtLeast(0))
        _uiState.update { current ->
            current.copy(
                selectedDhikrIndex = clamped,
                target = current.dhikrs.getOrNull(clamped)?.targetCount ?: current.target,
                count = 0,
                isComplete = false
            )
        }
        viewModelScope.launch {
            val s = settingsRepository.getSettings().first()
            settingsRepository.updateSettings(s.copy(selectedDhikrIndex = clamped))
        }
    }

    private fun persistSession(state: CounterUiState, count: Int) {
        viewModelScope.launch {
            val dhikr = state.selectedDhikr
            saveSessionUseCase(
                Session(
                    dhikrId   = dhikr?.id,
                    dhikrName = dhikr?.name ?: "Зикр",
                    count     = count,
                    target    = state.target,
                    completed = true,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // -----------------------------------------------------------------------
    // Legacy helpers (used by CounterScreen / other UI code)
    // -----------------------------------------------------------------------

    fun onTap() = onEvent(CounterEvent.Increment)

    fun onReset() = onEvent(CounterEvent.Reset)

    fun onSelectDhikr(dhikr: Dhikr) {
        val index = _uiState.value.dhikrs.indexOfFirst { it.id == dhikr.id }
        if (index >= 0) onEvent(CounterEvent.SelectDhikr(index))
    }

    fun onSaveSession() {
        val state = _uiState.value
        persistSession(state, state.count)
        reset()
    }

    fun onSetTarget(target: Int) = onEvent(CounterEvent.SetTarget(target))

    private fun setTarget(target: Int) {
        _uiState.update { it.copy(target = target, count = 0, isComplete = false) }
        viewModelScope.launch {
            val current = settingsRepository.getSettings().first()
            settingsRepository.updateSettings(current.copy(selectedTasbihTypeId = target))
        }
    }
}
