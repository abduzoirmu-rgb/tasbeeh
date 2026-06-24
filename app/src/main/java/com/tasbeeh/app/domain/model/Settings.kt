package com.tasbeeh.app.domain.model

data class Settings(
    val vibrationEnabled: Boolean = true,
    val clickSoundEnabled: Boolean = false,
    val isDarkTheme: Boolean = false,
    val language: String = "ru"
)
