package com.tasbeeh.app.domain.repository

import com.tasbeeh.app.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun saveSession(session: Session)

    /** Primary contract used by use cases and TDD tests. */
    fun getSessions(): Flow<List<Session>>

    fun getSessionsByDhikr(dhikrId: Long): Flow<List<Session>>

    // Extended operations (used by data layer / ViewModel)
    fun getAllSessions(): Flow<List<Session>> = getSessions()
    suspend fun deleteSession(id: Long) {}
    suspend fun deleteAllSessions() {}
}
