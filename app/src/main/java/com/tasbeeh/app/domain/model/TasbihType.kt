package com.tasbeeh.app.domain.model

data class TasbihType(
    val id: Int,
    val nameRu: String,
    val count: Int,
    val isCustom: Boolean = false
)

val PredefinedTasbihTypes = listOf(
    TasbihType(1, "Краткий (33 раза)",     33),
    TasbihType(2, "Стандарт (66 раз)",     66),
    TasbihType(3, "Полный тасбих (99 раз)", 99),
    TasbihType(4, "100 раз",               100),
    TasbihType(5, "Свой...",               0, isCustom = true)
)
