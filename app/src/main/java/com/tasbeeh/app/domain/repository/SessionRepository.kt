package com.tasbeeh.app.domain.repository

import com.tasbeeh.app.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getAllSessions(): Flow<List<Session>>
    suspend fun saveSession(session: Session)
    suspend fun deleteSession(id: Long)
    suspend fun deleteAllSessions()
}
