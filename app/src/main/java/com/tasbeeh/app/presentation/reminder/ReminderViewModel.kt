package com.tasbeeh.app.presentation.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasbeeh.app.domain.model.Reminder
import com.tasbeeh.app.domain.usecase.DeleteReminderUseCase
import com.tasbeeh.app.domain.usecase.GetRemindersUseCase
import com.tasbeeh.app.domain.usecase.SaveReminderUseCase
import com.tasbeeh.app.worker.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderUiState(
    val reminders: List<Reminder> = emptyList(),
    val showAddDialog: Boolean = false,
    val editingReminder: Reminder? = null
)

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val getRemindersUseCase: GetRemindersUseCase,
    private val saveReminderUseCase: SaveReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderUiState())
    val uiState: StateFlow<ReminderUiState> = _uiState

    init {
        viewModelScope.launch {
            getRemindersUseCase().collect { reminders ->
                _uiState.update { it.copy(reminders = reminders) }
            }
        }
    }

    fun showAddDialog(existing: Reminder? = null) {
        _uiState.update { it.copy(showAddDialog = true, editingReminder = existing) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showAddDialog = false, editingReminder = null) }
    }

    fun saveReminder(reminder: Reminder) {
        viewModelScope.launch {
            val savedId = saveReminderUseCase(reminder).toInt()
            val savedReminder = reminder.copy(id = savedId)
            reminderScheduler.schedule(savedReminder)
            _uiState.update { it.copy(showAddDialog = false, editingReminder = null) }
        }
    }

    fun deleteReminder(id: Int) {
        viewModelScope.launch {
            _uiState.value.reminders.find { it.id == id }?.let { reminderScheduler.cancel(it) }
            deleteReminderUseCase(id)
        }
    }

    fun toggleEnabled(id: Int, enabled: Boolean) {
        viewModelScope.launch {
            val updated = _uiState.value.reminders
                .find { it.id == id }
                ?.copy(isEnabled = enabled) ?: return@launch
            saveReminderUseCase(updated)
            reminderScheduler.schedule(updated)
        }
    }
}
