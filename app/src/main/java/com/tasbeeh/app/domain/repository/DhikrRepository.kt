package com.tasbeeh.app.domain.repository

import com.tasbeeh.app.domain.model.Dhikr
import kotlinx.coroutines.flow.Flow

interface DhikrRepository {
    /** Primary contract used by use cases and TDD tests. */
    fun getDhikrs(): Flow<List<Dhikr>>

    // Extended operations (used by data layer / ViewModel)
    fun getAllDhikrs(): Flow<List<Dhikr>> = getDhikrs()
    suspend fun getDhikrById(id: Long): Dhikr? = null
    suspend fun saveDhikr(dhikr: Dhikr): Long = 0L
    suspend fun deleteDhikr(id: Long) {}
}
