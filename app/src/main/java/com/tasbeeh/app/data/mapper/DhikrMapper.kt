package com.tasbeeh.app.data.mapper

import com.tasbeeh.app.data.entity.DhikrEntity
import com.tasbeeh.app.domain.model.Dhikr

fun DhikrEntity.toDomain(): Dhikr = Dhikr(
    id = id,
    name = name,
    arabicText = arabicText,
    targetCount = targetCount,
    isCustom = isCustom
)

fun Dhikr.toEntity(): DhikrEntity = DhikrEntity(
    id = id,
    name = name,
    arabicText = arabicText,
    targetCount = targetCount,
    isCustom = isCustom
)
