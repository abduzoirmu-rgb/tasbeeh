package com.tasbeeh.app.domain.model

enum class ThemeMode { LIGHT, DARK, AMOLED, SYSTEM }
enum class VibrationIntensity { LOW, MEDIUM, HIGH }

data class Settings(
    val vibrationEnabled: Boolean = true,
    val vibrationIntensity: VibrationIntensity = VibrationIntensity.MEDIUM,
    val clickSound: Boolean = false,
    val notificationSound: String = "default",
    val isDarkTheme: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: String = "ru",
    val showTextOnScreen: Boolean = true,
    val privacyMode: Boolean = false,
    val smartTouch: Boolean = false,
    val autoRepeat: Boolean = false,
    val keepScreenOn: Boolean = false,
    val dailyGoalEnabled: Boolean = false,
    val dailyGoalValue: Int = 100,
    val isPremiumUser: Boolean = false,
    val selectedTasbihTypeId: Int = 1,
    val selectedBackgroundId: Int = 0,
    val selectedDhikrIndex: Int = 0
)
