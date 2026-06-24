package com.tasbeeh.app.domain.model

data class Session(
    val id: Long = 0,
    val dhikrId: Long?,
    val dhikrName: String,
    val count: Int,
    val target: Int,
    val completed: Boolean,
    val timestamp: Long
)
