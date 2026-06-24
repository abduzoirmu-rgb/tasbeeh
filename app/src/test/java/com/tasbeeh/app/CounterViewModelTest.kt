package com.tasbeeh.app

import app.cash.turbine.test
import com.tasbeeh.app.domain.model.Dhikr
import com.tasbeeh.app.domain.model.Session
import com.tasbeeh.app.domain.model.Settings
import com.tasbeeh.app.domain.repository.DhikrRepository
import com.tasbeeh.app.domain.repository.SessionRepository
import com.tasbeeh.app.domain.repository.SettingsRepository
import com.tasbeeh.app.domain.usecase.IncrementCounterUseCase
import com.tasbeeh.app.domain.usecase.SaveSessionUseCase
import com.tasbeeh.app.presentation.counter.CounterViewModel
import com.tasbeeh.app.presentation.util.VibrationManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CounterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val defaultDhikr = Dhikr(id = 1L, name = "SubhanAllah", arabicText = null, targetCount = 33, isCustom = false)
    private val anotherDhikr = Dhikr(id = 2L, name = "Alhamdulillah", arabicText = null, targetCount = 100, isCustom = false)

    private val dhikrRepository: DhikrRepository = mockk()
    private val sessionRepository: SessionRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk()
    private val saveSessionUseCase: SaveSessionUseCase = mockk(relaxed = true)
    private val vibrationManager: VibrationManager = mockk(relaxed = true)

    private lateinit var viewModel: CounterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { dhikrRepository.getAllDhikrs() } returns flowOf(listOf(defaultDhikr, anotherDhikr))
        coEvery { dhikrRepository.getDhikrById(defaultDhikr.id) } returns defaultDhikr
        coEvery { dhikrRepository.getDhikrById(anotherDhikr.id) } returns anotherDhikr
        every { settingsRepository.settings } returns flowOf(Settings(vibrationEnabled = true))

        viewModel = CounterViewModel(
            incrementCounterUseCase = IncrementCounterUseCase(),
            saveSessionUseCase = saveSessionUseCase,
            dhikrRepository = dhikrRepository,
            sessionRepository = sessionRepository,
            settingsRepository = settingsRepository,
            vibrationManager = vibrationManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // T1 – onTap increments count

    @Test
    fun `first tap increments count to one`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.onTap()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals(1, state.count)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `three taps produce count three`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            repeat(3) {
                viewModel.onTap()
                testDispatcher.scheduler.advanceUntilIdle()
                awaitItem()
            }
            val state = expectMostRecentItem()
            assertEquals(3, state.count)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isGoalReached false before reaching target`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.onTap()
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertFalse(state.isGoalReached)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // T2 – Goal detection

    @Test
    fun `goal reached when count equals target`() = runTest {
        repeat(defaultDhikr.targetCount - 1) { viewModel.onTap() }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val beforeGoal = awaitItem()
            assertEquals(defaultDhikr.targetCount - 1, beforeGoal.count)
            assertFalse(beforeGoal.isGoalReached)

            viewModel.onTap()
            testDispatcher.scheduler.advanceUntilIdle()

            val atGoal = awaitItem()
            assertEquals(defaultDhikr.targetCount, atGoal.count)
            assertTrue(atGoal.isGoalReached)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // T3 – Reset

    @Test
    fun `reset sets count to zero`() = runTest {
        repeat(5) { viewModel.onTap() }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val beforeReset = awaitItem()
            assertEquals(5, beforeReset.count)

            viewModel.onReset()
            testDispatcher.scheduler.advanceUntilIdle()

            val afterReset = awaitItem()
            assertEquals(0, afterReset.count)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reset clears isGoalReached`() = runTest {
        repeat(defaultDhikr.targetCount) { viewModel.onTap() }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onReset()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isGoalReached)
        assertEquals(0, state.count)
    }

    // T4 – Save session

    @Test
    fun `save session delegates to use case with correct arguments`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle() // let init collect dhikrs

        repeat(10) { viewModel.onTap() }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSaveSession()
        testDispatcher.scheduler.advanceUntilIdle()

        val sessionSlot = slot<Session>()
        coVerify { saveSessionUseCase(capture(sessionSlot)) }

        assertEquals(10, sessionSlot.captured.count)
        assertEquals(defaultDhikr.id, sessionSlot.captured.dhikrId)
    }

    @Test
    fun `save session with zero count calls use case`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onSaveSession()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { saveSessionUseCase(any()) }
    }

    // T5 – Select dhikr

    @Test
    fun `select dhikr updates target`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onSelectDhikr(anotherDhikr.id)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals(anotherDhikr.targetCount, state.target)
            assertEquals(anotherDhikr.id, state.selectedDhikr?.id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `select dhikr resets count`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        repeat(5) { viewModel.onTap() }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSelectDhikr(anotherDhikr.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.count)
    }
}
