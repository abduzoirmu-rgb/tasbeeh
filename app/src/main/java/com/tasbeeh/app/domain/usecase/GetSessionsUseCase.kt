package com.tasbeeh.app.domain.usecase

import com.tasbeeh.app.domain.model.Session
import com.tasbeeh.app.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSessionsUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(): Flow<List<Session>> = sessionRepository.getAllSessions()
}
