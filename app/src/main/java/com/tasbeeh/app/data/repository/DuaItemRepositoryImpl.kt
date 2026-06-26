package com.tasbeeh.app.data.repository

import com.tasbeeh.app.data.local.db.dao.DuaItemDao
import com.tasbeeh.app.domain.model.DuaItem
import com.tasbeeh.app.domain.repository.DuaItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DuaItemRepositoryImpl @Inject constructor(
    private val duaItemDao: DuaItemDao
) : DuaItemRepository {

    override fun getDuasByCategory(categoryId: Int): Flow<List<DuaItem>> =
        duaItemDao.getByCategory(categoryId).map { list -> list.map { it.toDomain() } }

    override fun getFavorites(): Flow<List<DuaItem>> =
        duaItemDao.getFavorites().map { list -> list.map { it.toDomain() } }

    override fun search(query: String): Flow<List<DuaItem>> =
        duaItemDao.search(query).map { list -> list.map { it.toDomain() } }

    override suspend fun setFavorite(id: Long, isFavorite: Boolean) =
        duaItemDao.setFavorite(id, isFavorite)

    private fun com.tasbeeh.app.data.entity.DuaItemEntity.toDomain() = DuaItem(
        id              = id,
        categoryId      = categoryId,
        arabicText      = arabicText,
        transliteration = transliteration,
        translationRu   = translationRu,
        source          = source,
        repeatCount     = repeatCount,
        isFavorite      = isFavorite
    )
}
