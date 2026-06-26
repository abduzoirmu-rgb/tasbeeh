package com.tasbeeh.app.domain.usecase

import com.tasbeeh.app.domain.repository.ReminderRepository
import javax.inject.Inject

class DeleteReminderUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(id: Int) = repository.deleteReminder(id)
}
