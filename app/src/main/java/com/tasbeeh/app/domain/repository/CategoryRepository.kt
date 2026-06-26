package com.tasbeeh.app.domain.repository

import com.tasbeeh.app.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Int): Category?
}
