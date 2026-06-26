package com.tasbeeh.app.data.repository

import com.tasbeeh.app.data.local.db.dao.DhikrDao
import com.tasbeeh.app.data.mapper.toDomain
import com.tasbeeh.app.data.mapper.toEntity
import com.tasbeeh.app.domain.model.Dhikr
import com.tasbeeh.app.domain.repository.DhikrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DhikrRepositoryImpl @Inject constructor(
    private val dhikrDao: DhikrDao
) : DhikrRepository {

    override fun getDhikrs(): Flow<List<Dhikr>> =
        dhikrDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getDhikrById(id: Long): Dhikr? =
        dhikrDao.getById(id)?.toDomain()

    override suspend fun saveDhikr(dhikr: Dhikr): Long =
        dhikrDao.insert(dhikr.toEntity())

    override suspend fun deleteDhikr(id: Long) =
        dhikrDao.deleteById(id)
}
