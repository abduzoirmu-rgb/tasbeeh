package com.tasbeeh.app.data.repository

import com.tasbeeh.app.data.local.db.dao.SessionDao
import com.tasbeeh.app.data.mapper.toDomain
import com.tasbeeh.app.data.mapper.toEntity
import com.tasbeeh.app.domain.model.Session
import com.tasbeeh.app.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {

    override fun getSessions(): Flow<List<Session>> =
        sessionDao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getSessionsByDhikr(dhikrId: Long): Flow<List<Session>> =
        sessionDao.getByDhikr(dhikrId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveSession(session: Session) {
        sessionDao.insert(session.toEntity())
    }

    override suspend fun deleteSession(id: Long) =
        sessionDao.deleteById(id)

    override suspend fun deleteAllSessions() =
        sessionDao.deleteAll()
}
