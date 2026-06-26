package com.tasbeeh.app.domain.usecase

import com.tasbeeh.app.domain.model.Session
import com.tasbeeh.app.domain.repository.SessionRepository
import javax.inject.Inject

class SaveSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(session: Session) = repository.saveSession(session)
}
