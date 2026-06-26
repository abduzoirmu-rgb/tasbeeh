package com.tasbeeh.app.domain.model

import androidx.compose.ui.graphics.Color

data class BackgroundTheme(
    val id: Int,
    val nameRu: String,
    val bgColor: Color,
    val surfaceColor: Color,
    val accentColor: Color
)

val PredefinedBackgrounds = listOf(
    BackgroundTheme(0, "Лес",     Color(0xFF0D1B15), Color(0xFF112218), Color(0xFF1D9A6C)),
    BackgroundTheme(1, "Ночь",    Color(0xFF0A0E1A), Color(0xFF121629), Color(0xFF4A5FBE)),
    BackgroundTheme(2, "Рассвет", Color(0xFF1A0A14), Color(0xFF2A1220), Color(0xFFBE4A7A)),
    BackgroundTheme(3, "Пустыня", Color(0xFF1A1208), Color(0xFF2A1E0A), Color(0xFFBE8A4A)),
    BackgroundTheme(4, "Небо",    Color(0xFF0A151A), Color(0xFF102030), Color(0xFF4A9ABE))
)
