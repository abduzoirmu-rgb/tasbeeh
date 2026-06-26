package com.tasbeeh.app.domain.model

data class DuaItem(
    val id: Long,
    val categoryId: Int,
    val arabicText: String,
    val transliteration: String,
    val translationRu: String,
    val source: String? = null,
    val repeatCount: Int? = null,
    val isFavorite: Boolean = false
)
