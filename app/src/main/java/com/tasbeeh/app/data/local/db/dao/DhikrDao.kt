package com.tasbeeh.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tasbeeh.app.data.entity.DhikrEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DhikrDao {

    @Query("SELECT * FROM dhikrs")
    fun getAll(): Flow<List<DhikrEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg dhikrs: DhikrEntity)

    @Query("SELECT * FROM dhikrs WHERE id = :id")
    suspend fun getById(id: Long): DhikrEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dhikr: DhikrEntity): Long

    @Query("DELETE FROM dhikrs WHERE id = :id")
    suspend fun deleteById(id: Long)
}
