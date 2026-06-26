package com.tasbeeh.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dua_items",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class DuaItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Int,
    val arabicText: String,
    val transliteration: String,
    val translationRu: String,
    val source: String? = null,
    val repeatCount: Int? = null,
    val isFavorite: Boolean = false
)
