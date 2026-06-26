package com.tasbeeh.app.domain.usecase

import com.tasbeeh.app.domain.model.Reminder
import com.tasbeeh.app.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRemindersUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    operator fun invoke(): Flow<List<Reminder>> = repository.getReminders()
}
