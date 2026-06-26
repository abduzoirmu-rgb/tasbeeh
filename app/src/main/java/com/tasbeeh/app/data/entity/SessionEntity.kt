package com.tasbeeh.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = DhikrEntity::class,
            parentColumns = ["id"],
            childColumns = ["dhikr_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["dhikr_id"])]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "dhikr_id")
    val dhikrId: Long?,
    val dhikrName: String,
    val count: Int,
    val target: Int,
    val completed: Boolean,
    val timestamp: Long
)
