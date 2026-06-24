package com.tasbeeh.app.domain.usecase

import com.tasbeeh.app.domain.model.Dhikr
import com.tasbeeh.app.domain.repository.DhikrRepository
import javax.inject.Inject

class SaveDhikrUseCase @Inject constructor(
    private val dhikrRepository: DhikrRepository
) {
    suspend operator fun invoke(dhikr: Dhikr): Long = dhikrRepository.saveDhikr(dhikr)
}
