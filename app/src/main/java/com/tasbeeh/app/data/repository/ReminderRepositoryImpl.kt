package com.tasbeeh.app.data.repository

import com.tasbeeh.app.data.entity.ReminderEntity
import com.tasbeeh.app.data.local.db.dao.ReminderDao
import com.tasbeeh.app.domain.model.Reminder
import com.tasbeeh.app.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun getReminders(): Flow<List<Reminder>> =
        reminderDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getReminderById(id: Int): Reminder? =
        reminderDao.getById(id)?.toDomain()

    override suspend fun saveReminder(reminder: Reminder): Long =
        reminderDao.insert(reminder.toEntity())

    override suspend fun deleteReminder(id: Int) =
        reminderDao.deleteById(id)

    override suspend fun setEnabled(id: Int, enabled: Boolean) =
        reminderDao.setEnabled(id, enabled)

    private fun ReminderEntity.toDomain() = Reminder(
        id           = id,
        title        = title,
        timeHour     = timeHour,
        timeMinute   = timeMinute,
        daysOfWeek   = daysOfWeek,
        isEnabled    = isEnabled,
        linkedZikrId = linkedZikrId
    )

    private fun Reminder.toEntity() = ReminderEntity(
        id           = id,
        title        = title,
        timeHour     = timeHour,
        timeMinute   = timeMinute,
        daysOfWeek   = daysOfWeek,
        isEnabled    = isEnabled,
        linkedZikrId = linkedZikrId
    )
}
