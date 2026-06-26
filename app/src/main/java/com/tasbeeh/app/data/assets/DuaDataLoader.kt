package com.tasbeeh.app.data.assets

import android.content.Context
import com.tasbeeh.app.data.entity.CategoryEntity
import com.tasbeeh.app.data.entity.DuaItemEntity
import com.tasbeeh.app.data.local.db.dao.CategoryDao
import com.tasbeeh.app.data.local.db.dao.DuaItemDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DuaDataLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val categoryDao: CategoryDao,
    private val duaItemDao: DuaItemDao
) {
    suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        if (categoryDao.count() > 0) return@withContext

        val json = context.assets.open("dua_data.json")
            .bufferedReader()
            .use { it.readText() }

        val root = JSONObject(json)

        val categoriesArray = root.getJSONArray("categories")
        val categories = (0 until categoriesArray.length()).map { i ->
            val obj = categoriesArray.getJSONObject(i)
            CategoryEntity(
                id    = obj.getInt("id"),
                title = obj.getString("title"),
                icon  = obj.getString("icon"),
                order = obj.getInt("order")
            )
        }
        categoryDao.insertAll(categories)

        val duaArray = root.getJSONArray("duaItems")
        val duas = (0 until duaArray.length()).map { i ->
            val obj = duaArray.getJSONObject(i)
            DuaItemEntity(
                categoryId      = obj.getInt("categoryId"),
                arabicText      = obj.getString("arabicText"),
                transliteration = obj.getString("transliteration"),
                translationRu   = obj.getString("translationRu"),
                source          = if (obj.isNull("source")) null else obj.getString("source"),
                repeatCount     = if (obj.isNull("repeatCount")) null else obj.getInt("repeatCount"),
                isFavorite      = false
            )
        }
        duaItemDao.insertAll(duas)
    }
}
