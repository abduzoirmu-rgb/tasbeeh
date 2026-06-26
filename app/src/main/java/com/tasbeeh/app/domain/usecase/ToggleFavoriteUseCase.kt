package com.tasbeeh.app.domain.usecase

import com.tasbeeh.app.domain.repository.DuaItemRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: DuaItemRepository
) {
    suspend operator fun invoke(id: Long, isFavorite: Boolean) =
        repository.setFavorite(id, isFavorite)
}
