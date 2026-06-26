package com.tasbeeh.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val timeHour: Int,
    val timeMinute: Int,
    val daysOfWeek: String,
    val isEnabled: Boolean,
    val linkedZikrId: Int? = null
)
