package com.tasbeeh.app.domain.usecase

import com.tasbeeh.app.domain.model.DuaItem
import com.tasbeeh.app.domain.repository.DuaItemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDuasByCategoryUseCase @Inject constructor(
    private val repository: DuaItemRepository
) {
    operator fun invoke(categoryId: Int): Flow<List<DuaItem>> =
        repository.getDuasByCategory(categoryId)
}
