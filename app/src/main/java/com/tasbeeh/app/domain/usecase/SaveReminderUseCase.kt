package com.tasbeeh.app.domain.usecase

import com.tasbeeh.app.domain.model.Reminder
import com.tasbeeh.app.domain.repository.ReminderRepository
import javax.inject.Inject

class SaveReminderUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(reminder: Reminder): Long = repository.saveReminder(reminder)
}
