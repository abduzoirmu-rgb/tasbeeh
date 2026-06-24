package com.example.tasbeh.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tasbeh.data.local.dao.DhikrDao
import com.example.tasbeh.data.local.dao.SessionDao
import com.example.tasbeh.data.local.database.AppDatabase
import com.example.tasbeh.data.local.entity.DhikrEntity
import com.example.tasbeh.data.local.entity.SessionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * Instrumented tests for [SessionDao] using an in-memory [AppDatabase].
 *
 * Covered scenarios:
 *  T4 – Saved session appears in history (getAllSessions)
 *  T8 – Sessions survive across ViewModel restarts (data persists in DB)
 *  T9 – Deleting a dhikr sets dhikr_id = NULL in linked sessions (ON DELETE SET NULL)
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SessionDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dhikrDao: DhikrDao
    private lateinit var sessionDao: SessionDao

    /** Helper — inserts a dhikr and returns its auto-generated id. */
    private suspend fun insertDhikr(name: String = "SubhanAllah", target: Int = 33): Long =
        dhikrDao.insertDhikr(DhikrEntity(name = name, target = target))

    /** Helper — inserts a session linked to the given dhikrId. */
    private suspend fun insertSession(
        dhikrId: Long?,
        count: Int = 33,
        timestamp: Long = Instant.now().epochSecond
    ): Long = sessionDao.insertSession(
        SessionEntity(dhikrId = dhikrId, count = count, timestamp = timestamp)
    )

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dhikrDao   = db.dhikrDao()
        sessionDao = db.sessionDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // -------------------------------------------------------------------------
    // T4 – Saved session appears in history
    // -------------------------------------------------------------------------

    @Test
    fun insertSession_appearsInGetAllSessions() = runTest {
        val dhikrId = insertDhikr()
        insertSession(dhikrId = dhikrId, count = 33)

        val sessions = sessionDao.getAllSessions().first()
        assertEquals(1, sessions.size)
        assertEquals(33, sessions[0].count)
        assertEquals(dhikrId, sessions[0].dhikrId)
    }

    @Test
    fun insertMultipleSessions_allAppearInHistory() = runTest {
        val dhikrId = insertDhikr()
        insertSession(dhikrId = dhikrId, count = 10)
        insertSession(dhikrId = dhikrId, count = 20)
        insertSession(dhikrId = dhikrId, count = 33)

        val sessions = sessionDao.getAllSessions().first()
        assertEquals(3, sessions.size)
    }

    @Test
    fun noSessions_getAllSessions_returnsEmpty() = runTest {
        val sessions = sessionDao.getAllSessions().first()
        assertTrue(sessions.isEmpty())
    }

    // -------------------------------------------------------------------------
    // GetById
    // -------------------------------------------------------------------------

    @Test
    fun getSessionById_existingId_returnsSession() = runTest {
        val dhikrId   = insertDhikr()
        val sessionId = insertSession(dhikrId = dhikrId, count = 25)

        val found = sessionDao.getSessionById(sessionId)
        assertNotNull(found)
        assertEquals(sessionId, found!!.id)
        assertEquals(25, found.count)
    }

    @Test
    fun getSessionById_nonExistingId_returnsNull() = runTest {
        val found = sessionDao.getSessionById(999L)
        assertNull(found)
    }

    // -------------------------------------------------------------------------
    // Session with null dhikr_id (no dhikr selected)
    // -------------------------------------------------------------------------

    @Test
    fun insertSessionWithNullDhikrId_storedAndRetrieved() = runTest {
        insertSession(dhikrId = null, count = 15)

        val sessions = sessionDao.getAllSessions().first()
        assertEquals(1, sessions.size)
        assertNull(sessions[0].dhikrId)
        assertEquals(15, sessions[0].count)
    }

    // -------------------------------------------------------------------------
    // T9 – ON DELETE SET NULL: deleting dhikr must not delete sessions
    // -------------------------------------------------------------------------

    @Test
    fun deleteDhikr_linkedSessionsNotDeleted_dhikrIdSetToNull() = runTest {
        val dhikrId = insertDhikr(name = "SubhanAllah", target = 33)
        insertSession(dhikrId = dhikrId, count = 33)
        insertSession(dhikrId = dhikrId, count = 20)

        // Verify sessions exist with non-null dhikr_id before deletion
        val before = sessionDao.getAllSessions().first()
        assertEquals(2, before.size)
        assertTrue(before.all { it.dhikrId == dhikrId })

        // Delete the dhikr
        val dhikr = dhikrDao.getDhikrById(dhikrId)!!
        dhikrDao.deleteDhikr(dhikr)

        // Sessions must still exist, but dhikr_id becomes NULL
        val after = sessionDao.getAllSessions().first()
        assertEquals(2, after.size)                          // rows NOT deleted
        assertTrue(after.all { it.dhikrId == null })         // FK set to NULL
    }

    @Test
    fun deleteDhikr_otherDhikrSessions_unaffected() = runTest {
        val dhikrA = insertDhikr(name = "Dhikr A", target = 33)
        val dhikrB = insertDhikr(name = "Dhikr B", target = 100)

        insertSession(dhikrId = dhikrA, count = 33)
        insertSession(dhikrId = dhikrB, count = 50)

        // Delete only dhikr A
        dhikrDao.deleteDhikr(dhikrDao.getDhikrById(dhikrA)!!)

        val after = sessionDao.getAllSessions().first()
        assertEquals(2, after.size)

        val sessionA = after.first { it.count == 33 }
        val sessionB = after.first { it.count == 50 }

        assertNull(sessionA.dhikrId)        // A's session → NULL
        assertEquals(dhikrB, sessionB.dhikrId) // B's session → unchanged
    }

    // -------------------------------------------------------------------------
    // T8 – Data persists (survives writes and reads within same DB instance)
    // -------------------------------------------------------------------------

    @Test
    fun sessionsPersistedInDb_retrievedAfterMultipleOperations() = runTest {
        val dhikrId = insertDhikr()

        // Simulate several app interactions
        insertSession(dhikrId = dhikrId, count = 10, timestamp = 1_000L)
        insertSession(dhikrId = dhikrId, count = 20, timestamp = 2_000L)
        insertSession(dhikrId = dhikrId, count = 33, timestamp = 3_000L)

        // All three sessions should still be retrievable
        val sessions = sessionDao.getAllSessions().first()
        assertEquals(3, sessions.size)

        val counts = sessions.map { it.count }.sorted()
        assertEquals(listOf(10, 20, 33), counts)
    }

    // -------------------------------------------------------------------------
    // Delete session
    // -------------------------------------------------------------------------

    @Test
    fun deleteSession_removesOnlyThatSession() = runTest {
        val dhikrId = insertDhikr()
        val id1 = insertSession(dhikrId = dhikrId, count = 10)
        insertSession(dhikrId = dhikrId, count = 20)

        val toDelete = sessionDao.getSessionById(id1)!!
        sessionDao.deleteSession(toDelete)

        val after = sessionDao.getAllSessions().first()
        assertEquals(1, after.size)
        assertEquals(20, after[0].count)
    }

    // -------------------------------------------------------------------------
    // Flow reactivity
    // -------------------------------------------------------------------------

    @Test
    fun getAllSessions_flow_emitsUpdatedListAfterInsert() = runTest {
        val dhikrId = insertDhikr()
        val flow = sessionDao.getAllSessions()

        assertTrue(flow.first().isEmpty())

        insertSession(dhikrId = dhikrId, count = 33)

        assertEquals(1, flow.first().size)
    }
}
