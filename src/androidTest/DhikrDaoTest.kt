package com.example.tasbeh.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tasbeh.data.local.dao.DhikrDao
import com.example.tasbeh.data.local.database.AppDatabase
import com.example.tasbeh.data.local.entity.DhikrEntity
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

/**
 * Instrumented tests for [DhikrDao] using an in-memory [AppDatabase].
 *
 * Covered scenarios:
 *  – Insert and retrieve all dhikrs                     (→ T5, T6)
 *  – Get dhikr by id (existing & non-existing)
 *  – Update dhikr name / target
 *  – Delete dhikr
 *  – T6: custom dhikr inserted appears in getAllDhikrs()
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class DhikrDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: DhikrDao

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.dhikrDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // -------------------------------------------------------------------------
    // Insert & Read
    // -------------------------------------------------------------------------

    @Test
    fun insertDhikr_andGetAll_returnsInsertedItem() = runTest {
        val dhikr = DhikrEntity(name = "SubhanAllah", target = 33)
        dao.insertDhikr(dhikr)

        val all = dao.getAllDhikrs().first()
        assertEquals(1, all.size)
        assertEquals("SubhanAllah", all[0].name)
        assertEquals(33, all[0].target)
    }

    @Test
    fun insertMultipleDhikrs_returnsAllOfThem() = runTest {
        dao.insertDhikr(DhikrEntity(name = "SubhanAllah", target = 33))
        dao.insertDhikr(DhikrEntity(name = "Alhamdulillah", target = 33))
        dao.insertDhikr(DhikrEntity(name = "AllahuAkbar", target = 34))

        val all = dao.getAllDhikrs().first()
        assertEquals(3, all.size)
    }

    @Test
    fun emptyTable_getAllDhikrs_returnsEmptyList() = runTest {
        val all = dao.getAllDhikrs().first()
        assertTrue(all.isEmpty())
    }

    // -------------------------------------------------------------------------
    // GetById
    // -------------------------------------------------------------------------

    @Test
    fun getDhikrById_existingId_returnsDhikr() = runTest {
        val insertedId = dao.insertDhikr(DhikrEntity(name = "SubhanAllah", target = 33))

        val found = dao.getDhikrById(insertedId)
        assertNotNull(found)
        assertEquals(insertedId, found!!.id)
        assertEquals("SubhanAllah", found.name)
    }

    @Test
    fun getDhikrById_nonExistingId_returnsNull() = runTest {
        val found = dao.getDhikrById(999L)
        assertNull(found)
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Test
    fun updateDhikr_changesNameAndTarget() = runTest {
        val id = dao.insertDhikr(DhikrEntity(name = "Old Name", target = 10))
        val updated = DhikrEntity(id = id, name = "New Name", target = 100)

        dao.updateDhikr(updated)

        val found = dao.getDhikrById(id)
        assertNotNull(found)
        assertEquals("New Name", found!!.name)
        assertEquals(100, found.target)
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Test
    fun deleteDhikr_removesItFromTable() = runTest {
        val id = dao.insertDhikr(DhikrEntity(name = "SubhanAllah", target = 33))
        val entity = dao.getDhikrById(id)!!

        dao.deleteDhikr(entity)

        val all = dao.getAllDhikrs().first()
        assertTrue(all.isEmpty())
        assertNull(dao.getDhikrById(id))
    }

    // -------------------------------------------------------------------------
    // T6 – Custom dhikr appears in list
    // -------------------------------------------------------------------------

    @Test
    fun insertCustomDhikr_appearsInGetAllDhikrs() = runTest {
        // Pre-populate with two default dhikrs
        dao.insertDhikr(DhikrEntity(name = "SubhanAllah", target = 33))
        dao.insertDhikr(DhikrEntity(name = "Alhamdulillah", target = 33))

        // User creates a custom dhikr (T6)
        dao.insertDhikr(DhikrEntity(name = "My Custom Dhikr", target = 200))

        val all = dao.getAllDhikrs().first()
        assertEquals(3, all.size)
        assertTrue(all.any { it.name == "My Custom Dhikr" && it.target == 200 })
    }

    // -------------------------------------------------------------------------
    // Flow reactivity
    // -------------------------------------------------------------------------

    @Test
    fun getAllDhikrs_flow_emitsUpdatedListAfterInsert() = runTest {
        val flow = dao.getAllDhikrs()

        // Empty at start
        assertTrue(flow.first().isEmpty())

        dao.insertDhikr(DhikrEntity(name = "SubhanAllah", target = 33))

        // Should emit new list after insert
        val updated = flow.first()
        assertEquals(1, updated.size)
    }
}
