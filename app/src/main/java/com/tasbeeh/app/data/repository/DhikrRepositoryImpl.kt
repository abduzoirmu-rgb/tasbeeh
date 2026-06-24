package com.tasbeeh.app.data.repository

import com.tasbeeh.app.data.local.db.dao.DhikrDao
import com.tasbeeh.app.data.mapper.toDomain
import com.tasbeeh.app.data.mapper.toEntity
import com.tasbeeh.app.domain.model.Dhikr
import com.tasbeeh.app.domain.repository.DhikrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DhikrRepositoryImpl @Inject constructor(
    private val dhikrDao: DhikrDao
) : DhikrRepository {

    override fun getAllDhikrs(): Flow<List<Dhikr>> =
        dhikrDao.getAllDhikrs().map { list -> list.map { it.toDomain() } }

    override suspend fun getDhikrById(id: Long): Dhikr? =
        dhikrDao.getDhikrById(id)?.toDomain()

    override suspend fun saveDhikr(dhikr: Dhikr): Long =
        dhikrDao.insertDhikr(dhikr.toEntity())

    override suspend fun deleteDhikr(id: Long) =
        dhikrDao.deleteDhikrById(id)
}
