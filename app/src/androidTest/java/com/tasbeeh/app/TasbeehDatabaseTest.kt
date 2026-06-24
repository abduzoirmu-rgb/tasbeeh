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
import org.junit.Assert.assertNull
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

    @Test
    fun insertAndGetDhikr() = runBlocking {
        val entity = DhikrEntity(name = "Test", arabicText = null, targetCount = 33, isCustom = true)
        val id = database.dhikrDao().insertDhikr(entity)
        val retrieved = database.dhikrDao().getDhikrById(id)
        assertNotNull(retrieved)
        assertEquals("Test", retrieved?.name)
    }

    @Test
    fun deleteDhikr() = runBlocking {
        val entity = DhikrEntity(name = "ToDelete", arabicText = null, targetCount = 33, isCustom = true)
        val id = database.dhikrDao().insertDhikr(entity)
        database.dhikrDao().deleteDhikrById(id)
        val retrieved = database.dhikrDao().getDhikrById(id)
        assertNull(retrieved)
    }

    @Test
    fun insertAndGetSession() = runBlocking {
        val session = SessionEntity(
            dhikrId = null,
            dhikrName = "Субханаллах",
            count = 33,
            target = 33,
            completed = true,
            timestamp = System.currentTimeMillis()
        )
        database.sessionDao().insertSession(session)
        val sessions = database.sessionDao().getAllSessions().first()
        assertEquals(1, sessions.size)
        assertEquals("Субханаллах", sessions[0].dhikrName)
    }

    @Test
    fun sessionsOrderedByTimestampDesc() = runBlocking {
        val older = SessionEntity(
            dhikrId = null,
            dhikrName = "First",
            count = 10,
            target = 33,
            completed = false,
            timestamp = 1000L
        )
        val newer = SessionEntity(
            dhikrId = null,
            dhikrName = "Second",
            count = 20,
            target = 33,
            completed = false,
            timestamp = 2000L
        )
        database.sessionDao().insertSession(older)
        database.sessionDao().insertSession(newer)

        val sessions = database.sessionDao().getAllSessions().first()
        assertEquals("Second", sessions[0].dhikrName)
        assertEquals("First", sessions[1].dhikrName)
    }
}
