package com.tasbeeh.app.domain.model

data class Dhikr(
    val id: Long,
    val name: String,
    val arabicText: String?,
    val targetCount: Int,
    val isCustom: Boolean = false
)
