package com.tasbeeh.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dhikrs")
data class DhikrEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val arabicText: String?,
    val targetCount: Int,
    val isCustom: Boolean
)
