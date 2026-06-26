package com.tasbeeh.app.domain.usecase

import com.tasbeeh.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PremiumAccessUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    fun isPremium(): Flow<Boolean> =
        settingsRepository.getSettings().map { it.isPremiumUser }

    suspend fun unlock() {
        val current = settingsRepository.getSettings().first()
        settingsRepository.updateSettings(current.copy(isPremiumUser = true))
    }
}
