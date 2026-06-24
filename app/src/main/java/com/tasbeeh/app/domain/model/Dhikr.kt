package com.tasbeeh.app.domain.model

data class Dhikr(
    val id: Long = 0,
    val name: String,
    val arabicText: String?,
    val targetCount: Int = 33,
    val isCustom: Boolean = false
)
