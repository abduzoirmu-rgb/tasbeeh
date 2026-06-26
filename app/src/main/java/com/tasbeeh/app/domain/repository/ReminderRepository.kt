package com.tasbeeh.app.domain.repository

import com.tasbeeh.app.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getReminders(): Flow<List<Reminder>>
    suspend fun getReminderById(id: Int): Reminder?
    suspend fun saveReminder(reminder: Reminder): Long
    suspend fun deleteReminder(id: Int)
    suspend fun setEnabled(id: Int, enabled: Boolean)
}
