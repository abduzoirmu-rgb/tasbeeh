package com.tasbeeh.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tasbeeh.app.data.entity.DuaItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DuaItemDao {

    @Query("SELECT * FROM dua_items WHERE categoryId = :categoryId")
    fun getByCategory(categoryId: Int): Flow<List<DuaItemEntity>>

    @Query("SELECT * FROM dua_items WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<DuaItemEntity>>

    @Query("SELECT * FROM dua_items WHERE arabicText LIKE '%' || :query || '%' OR translationRu LIKE '%' || :query || '%' OR transliteration LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<DuaItemEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<DuaItemEntity>)

    @Query("UPDATE dua_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM dua_items")
    suspend fun count(): Int
}
