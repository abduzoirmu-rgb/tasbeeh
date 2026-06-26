package com.tasbeeh.app.domain.usecase

import com.tasbeeh.app.domain.model.Dhikr
import com.tasbeeh.app.domain.repository.DhikrRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDhikrsUseCase @Inject constructor(
    private val repository: DhikrRepository
) {
    operator fun invoke(): Flow<List<Dhikr>> = repository.getDhikrs()
}
