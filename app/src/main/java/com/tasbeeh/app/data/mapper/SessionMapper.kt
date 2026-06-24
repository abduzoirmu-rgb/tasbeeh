package com.tasbeeh.app.data.mapper

import com.tasbeeh.app.data.entity.SessionEntity
import com.tasbeeh.app.domain.model.Session

fun SessionEntity.toDomain(): Session = Session(
    id = id,
    dhikrId = dhikrId,
    dhikrName = dhikrName,
    count = count,
    target = target,
    completed = completed,
    timestamp = timestamp
)

fun Session.toEntity(): SessionEntity = SessionEntity(
    id = id,
    dhikrId = dhikrId,
    dhikrName = dhikrName,
    count = count,
    target = target,
    completed = completed,
    timestamp = timestamp
)
