package com.tasbeeh.app.domain.usecase

import com.tasbeeh.app.domain.model.Category
import com.tasbeeh.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    operator fun invoke(): Flow<List<Category>> = repository.getCategories()
}
