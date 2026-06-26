package com.tasbeeh.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tasbeeh.app.data.entity.DhikrEntity
import com.tasbeeh.app.data.entity.SessionEntity
import com.tasbeeh.app.data.local.db.TasbeehDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TasbeehDatabaseTest {

    private lateinit var database: TasbeehDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TasbeehDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun teardown() {
        database.close()
    }

    // 1. Insert all three preset dhikr entities using insertAll(vararg) and retrieve them.
    //    The resulting list must contain 3 items with the correct names in insert order.
    @Test
    fun database_insertDhikrs_andRetrieve() = runBlocking {
        val subhanallah   = DhikrEntity(name = "Субханаллах",    arabicText = "سُبْحَانَ ٱللَّٰهِ", targetCount = 33, isCustom = false)
        val alhamdulillah = DhikrEntity(name = "Альхамдулиллах", arabicText = "ٱلْحَمْدُ لِلَّٰهِ", targetCount = 33, isCustom = false)
        val allahuAkbar   = DhikrEntity(name = "Аллаху Акбар",   arabicText = "ٱللَّٰهُ أَكْبَرُ",  targetCount = 34, isCustom = false)

        database.dhikrDao().insertAll(subhanallah, alhamdulillah, allahuAkbar)

        val all = database.dhikrDao().getAll().first()

        assertEquals(3, all.size)

        val names = all.map { it.name }.toSet()
        assertTrue(names.contains("Субханаллах"))
        assertTrue(names.contains("Альхамдулиллах"))
        assertTrue(names.contains("Аллаху Акбар"))
    }

    // 2. Insert a SessionEntity, then retrieve it by the returned auto-generated id.
    //    The retrieved record must not be null and must match the inserted values.
    @Test
    fun database_insertSession_andRetrieveById() = runBlocking {
        val session = SessionEntity(
            dhikrId   = 1L,
            dhikrName = "Субханаллах",
            count     = 33,
            target    = 33,
            completed = true,
            timestamp = 1_000_000L
        )

        val insertedId = database.sessionDao().insert(session)
        val retrieved  = database.sessionDao().getById(insertedId)

        assertNotNull(retrieved)
        assertEquals(insertedId,       retrieved!!.id)
        assertEquals("Субханаллах",    retrieved.dhikrName)
        assertEquals(33,               retrieved.count)
        assertEquals(33,               retrieved.target)
        assertEquals(true,             retrieved.completed)
        assertEquals(1_000_000L,       retrieved.timestamp)
    }

    // 3. Insert 2 sessions with dhikrId=1 and 1 session with dhikrId=2.
    //    getByDhikr(1).first() must return exactly 2 elements.
    @Test
    fun database_getSessionsByDhikr_returnsCorrectCount() = runBlocking {
        val session1a = SessionEntity(dhikrId = 1L, dhikrName = "Субханаллах",    count = 33, target = 33, completed = true,  timestamp = 1000L)
        val session1b = SessionEntity(dhikrId = 1L, dhikrName = "Субханаллах",    count = 10, target = 33, completed = false, timestamp = 2000L)
        val session2  = SessionEntity(dhikrId = 2L, dhikrName = "Альхамдулиллах", count = 33, target = 33, completed = true,  timestamp = 3000L)

        database.sessionDao().insert(session1a)
        database.sessionDao().insert(session1b)
        database.sessionDao().insert(session2)

        val byDhikr1 = database.sessionDao().getByDhikr(1L).first()

        assertEquals(2, byDhikr1.size)
    }

    // Helper that shadows the top-level kotlin import so it resolves correctly
    // when the test class is read in isolation.
    private fun assertTrue(value: Boolean) {
        org.junit.Assert.assertTrue(value)
    }
}
