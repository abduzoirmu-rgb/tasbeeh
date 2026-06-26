package com.tasbeeh.app.data.repository

import com.tasbeeh.app.data.local.db.dao.CategoryDao
import com.tasbeeh.app.domain.model.Category
import com.tasbeeh.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getCategories(): Flow<List<Category>> =
        categoryDao.getAll().map { list ->
            list.map { Category(id = it.id, title = it.title, icon = it.icon, order = it.order) }
        }

    override suspend fun getCategoryById(id: Int): Category? =
        categoryDao.getById(id)?.let { Category(id = it.id, title = it.title, icon = it.icon, order = it.order) }
}
