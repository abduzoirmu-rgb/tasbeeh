package com.tasbeeh.app.domain.repository

import com.tasbeeh.app.domain.model.DuaItem
import kotlinx.coroutines.flow.Flow

interface DuaItemRepository {
    fun getDuasByCategory(categoryId: Int): Flow<List<DuaItem>>
    fun getFavorites(): Flow<List<DuaItem>>
    fun search(query: String): Flow<List<DuaItem>>
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
}
