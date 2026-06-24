package com.tasbeeh.app.domain.repository

import com.tasbeeh.app.domain.model.Dhikr
import kotlinx.coroutines.flow.Flow

interface DhikrRepository {
    fun getAllDhikrs(): Flow<List<Dhikr>>
    suspend fun getDhikrById(id: Long): Dhikr?
    suspend fun saveDhikr(dhikr: Dhikr): Long
    suspend fun deleteDhikr(id: Long)
}
