package com.tasbeeh.app.domain.model

data class Reminder(
    val id: Int = 0,
    val title: String,
    val timeHour: Int,
    val timeMinute: Int,
    val daysOfWeek: String,
    val isEnabled: Boolean,
    val linkedZikrId: Int? = null
) {
    fun formattedTime(): String =
        "%02d:%02d".format(timeHour, timeMinute)

    fun daysLabel(): String {
        val days = daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
        if (days.isEmpty()) return "Без повтора"
        if (days.size == 7) return "Каждый день"
        val names = mapOf(1 to "Пн", 2 to "Вт", 3 to "Ср", 4 to "Чт", 5 to "Пт", 6 to "Сб", 7 to "Вс")
        return days.mapNotNull { names[it] }.joinToString(", ")
    }
}
