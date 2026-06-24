package com.example.tasbeh.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Instrumented tests for [SettingsRepository] backed by DataStore Preferences.
 *
 * A temporary DataStore file is created per test run and deleted in tearDown().
 *
 * Covered scenarios:
 *  – Default values for vibration_enabled, click_sound, is_dark_theme
 *  – Persisting and reading each setting (write → read round-trip)
 *  – T7: vibration_enabled = false is stored and retrievable
 *  – T8: settings survive across repository re-instantiation (same file)
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SettingsDataStoreTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope      = TestScope(testDispatcher)

    private lateinit var context: Context
    private lateinit var dataStoreFile: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SettingsRepository

    // Preference keys must match those used in the production SettingsRepository.
    private val KEY_VIBRATION   = booleanPreferencesKey("vibration_enabled")
    private val KEY_CLICK_SOUND = booleanPreferencesKey("click_sound")
    private val KEY_DARK_THEME  = booleanPreferencesKey("is_dark_theme")

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        // Unique file per test to avoid state leakage
        dataStoreFile = File(context.filesDir, "test_settings_${System.nanoTime()}.preferences_pb")

        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { dataStoreFile }
        )

        repository = SettingsRepository(dataStore)
    }

    @After
    fun tearDown() {
        dataStoreFile.delete()
    }

    // -------------------------------------------------------------------------
    // Default values
    // -------------------------------------------------------------------------

    @Test
    fun vibrationEnabled_defaultIsTrue() = runTest {
        assertTrue(repository.isVibrationEnabled().first())
    }

    @Test
    fun clickSound_defaultIsTrue() = runTest {
        assertTrue(repository.isClickSoundEnabled().first())
    }

    @Test
    fun darkTheme_defaultIsFalse() = runTest {
        assertFalse(repository.isDarkThemeEnabled().first())
    }

    // -------------------------------------------------------------------------
    // Write → Read round-trips
    // -------------------------------------------------------------------------

    @Test
    fun setVibrationEnabled_false_persistsAndReadsBack() = runTest {
        repository.setVibrationEnabled(false)

        assertFalse(repository.isVibrationEnabled().first())
    }

    @Test
    fun setVibrationEnabled_true_persistsAndReadsBack() = runTest {
        // Start by disabling
        repository.setVibrationEnabled(false)
        // Then re-enable
        repository.setVibrationEnabled(true)

        assertTrue(repository.isVibrationEnabled().first())
    }

    @Test
    fun setClickSound_false_persistsAndReadsBack() = runTest {
        repository.setClickSoundEnabled(false)

        assertFalse(repository.isClickSoundEnabled().first())
    }

    @Test
    fun setDarkTheme_true_persistsAndReadsBack() = runTest {
        repository.setDarkThemeEnabled(true)

        assertTrue(repository.isDarkThemeEnabled().first())
    }

    @Test
    fun setDarkTheme_false_persistsAndReadsBack() = runTest {
        repository.setDarkThemeEnabled(true)
        repository.setDarkThemeEnabled(false)

        assertFalse(repository.isDarkThemeEnabled().first())
    }

    // -------------------------------------------------------------------------
    // T7 – Vibration disabled: setting is stored and retrievable
    // -------------------------------------------------------------------------

    @Test
    fun t7_vibrationDisabled_storedCorrectly() = runTest {
        repository.setVibrationEnabled(false)

        val value = repository.isVibrationEnabled().first()
        assertFalse("T7: vibration_enabled should be false after being disabled", value)
    }

    @Test
    fun t7_vibrationEnabled_storedCorrectly() = runTest {
        repository.setVibrationEnabled(false)
        repository.setVibrationEnabled(true)

        val value = repository.isVibrationEnabled().first()
        assertTrue("T7: vibration_enabled should be true after being re-enabled", value)
    }

    // -------------------------------------------------------------------------
    // T8 – Settings survive re-instantiation (same DataStore file, new repo)
    // -------------------------------------------------------------------------

    @Test
    fun t8_settingsPersistedAcrossRepositoryReinstantiation() = runTest {
        // Write settings via the first repository instance
        repository.setVibrationEnabled(false)
        repository.setDarkThemeEnabled(true)
        repository.setClickSoundEnabled(false)

        // Create a new repository pointing at the same DataStore file
        val secondDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { dataStoreFile }
        )
        val secondRepository = SettingsRepository(secondDataStore)

        // Settings must survive
        assertFalse(secondRepository.isVibrationEnabled().first())
        assertTrue(secondRepository.isDarkThemeEnabled().first())
        assertFalse(secondRepository.isClickSoundEnabled().first())
    }

    // -------------------------------------------------------------------------
    // Independence of settings
    // -------------------------------------------------------------------------

    @Test
    fun changingOneSettingDoesNotAffectOthers() = runTest {
        // Only change vibration
        repository.setVibrationEnabled(false)

        // Other settings must retain their defaults
        assertTrue(repository.isClickSoundEnabled().first())
        assertFalse(repository.isDarkThemeEnabled().first())
    }

    @Test
    fun allSettingsCanBeChangedIndependently() = runTest {
        repository.setVibrationEnabled(false)
        repository.setClickSoundEnabled(false)
        repository.setDarkThemeEnabled(true)

        assertFalse(repository.isVibrationEnabled().first())
        assertFalse(repository.isClickSoundEnabled().first())
        assertTrue(repository.isDarkThemeEnabled().first())
    }

    // -------------------------------------------------------------------------
    // Flow reactivity
    // -------------------------------------------------------------------------

    @Test
    fun isVibrationEnabled_flow_emitsUpdatedValueAfterWrite() = runTest {
        val flow = repository.isVibrationEnabled()

        // Default
        assertTrue(flow.first())

        repository.setVibrationEnabled(false)

        // Updated
        assertFalse(flow.first())
    }
}
