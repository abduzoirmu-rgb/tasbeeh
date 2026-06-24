package com.example.tasbeh.presentation.counter

import app.cash.turbine.test
import com.example.tasbeh.domain.model.CounterResult
import com.example.tasbeh.domain.model.Dhikr
import com.example.tasbeh.domain.model.Session
import com.example.tasbeh.domain.repository.DhikrRepository
import com.example.tasbeh.domain.repository.SessionRepository
import com.example.tasbeh.domain.usecase.IncrementCounterUseCase
import com.example.tasbeh.domain.usecase.SaveSessionUseCase
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for [CounterViewModel].
 *
 * Dependencies are replaced with MockK doubles.
 * StateFlow emissions are verified with Turbine.
 *
 * Covered scenarios:
 *  T1 – onTap() → count +1
 *  T2 – onTap() at target-1 → isGoalReached = true
 *  T3 – onReset() → count = 0, isGoalReached = false
 *  T4 – onSaveSession() → SaveSessionUseCase called with correct data
 *  T5 – onSelectDhikr(id) → target updated in UiState
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CounterViewModelTest {

    // region test doubles & helpers

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var incrementUseCase: IncrementCounterUseCase
    private lateinit var saveSessionUseCase: SaveSessionUseCase
    private lateinit var dhikrRepository: DhikrRepository
    private lateinit var sessionRepository: SessionRepository
    private lateinit var viewModel: CounterViewModel

    private val defaultDhikr = Dhikr(id = 1L, name = "SubhanAllah", target = 33)
    private val anotherDhikr = Dhikr(id = 2L, name = "Alhamdulillah", target = 100)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        incrementUseCase   = IncrementCounterUseCase()          // real implementation
        saveSessionUseCase = mockk(relaxed = true)
        dhikrRepository    = mockk()
        sessionRepository  = mockk(relaxed = true)

        every { dhikrRepository.getAllDhikrs() } returns flowOf(listOf(defaultDhikr, anotherDhikr))
        coEvery { dhikrRepository.getDhikrById(defaultDhikr.id) } returns defaultDhikr
        coEvery { dhikrRepository.getDhikrById(anotherDhikr.id) } returns anotherDhikr

        viewModel = CounterViewModel(
            incrementCounterUseCase = incrementUseCase,
            saveSessionUseCase      = saveSessionUseCase,
            dhikrRepository         = dhikrRepository,
            sessionRepository       = sessionRepository
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // endregion

    // -------------------------------------------------------------------------
    // T1 – onTap() increments count
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("T1 – onTap increments counter")
    inner class OnTap {

        @Test
        @DisplayName("first tap: count goes from 0 to 1")
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
        @DisplayName("three taps produce count = 3")
        fun `three taps produce count three`() = runTest {
            viewModel.uiState.test {
                awaitItem() // initial state

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
        @DisplayName("isGoalReached = false while count < target")
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
    }

    // -------------------------------------------------------------------------
    // T2 – Goal reached
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("T2 – Goal detection in ViewModel")
    inner class GoalDetection {

        @Test
        @DisplayName("isGoalReached = true on the tap that hits the target")
        fun `goal reached when count equals target`() = runTest {
            // advance count to target-1 silently
            repeat(defaultDhikr.target - 1) { viewModel.onTap() }
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.test {
                // consume whatever is already in the buffer
                val beforeGoal = awaitItem()
                assertEquals(defaultDhikr.target - 1, beforeGoal.count)
                assertFalse(beforeGoal.isGoalReached)

                viewModel.onTap()
                testDispatcher.scheduler.advanceUntilIdle()

                val atGoal = awaitItem()
                assertEquals(defaultDhikr.target, atGoal.count)
                assertTrue(atGoal.isGoalReached)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    // -------------------------------------------------------------------------
    // T3 – onReset()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("T3 – Reset counter")
    inner class OnReset {

        @Test
        @DisplayName("reset after taps sets count = 0")
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
        @DisplayName("reset clears isGoalReached flag")
        fun `reset clears isGoalReached`() = runTest {
            repeat(defaultDhikr.target) { viewModel.onTap() }
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onReset()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isGoalReached)
            assertEquals(0, state.count)
        }

        @Test
        @DisplayName("reset on already-zero count keeps count at 0 (idempotent)")
        fun `reset on zero count is idempotent`() = runTest {
            viewModel.onReset()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(0, viewModel.uiState.value.count)
        }
    }

    // -------------------------------------------------------------------------
    // T4 – onSaveSession()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("T4 – Save session")
    inner class OnSaveSession {

        @Test
        @DisplayName("SaveSessionUseCase is called with the current count and dhikr id")
        fun `save session delegates to use case with correct arguments`() = runTest {
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
        @DisplayName("saving a session with count=0 still delegates to use case")
        fun `save session with zero count calls use case`() = runTest {
            viewModel.onSaveSession()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { saveSessionUseCase(any()) }
        }
    }

    // -------------------------------------------------------------------------
    // T5 – onSelectDhikr()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("T5 – Select dhikr updates target")
    inner class OnSelectDhikr {

        @Test
        @DisplayName("selecting another dhikr updates target in UiState")
        fun `select dhikr updates target`() = runTest {
            viewModel.uiState.test {
                awaitItem() // initial state with defaultDhikr

                viewModel.onSelectDhikr(anotherDhikr.id)
                testDispatcher.scheduler.advanceUntilIdle()

                val state = awaitItem()
                assertEquals(anotherDhikr.target, state.target)
                assertEquals(anotherDhikr.id, state.selectedDhikr?.id)

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        @DisplayName("selecting a dhikr resets count to 0")
        fun `select dhikr resets count`() = runTest {
            repeat(5) { viewModel.onTap() }
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onSelectDhikr(anotherDhikr.id)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(0, state.count)
        }

        @Test
        @DisplayName("re-selecting the same dhikr keeps target unchanged")
        fun `reselect same dhikr keeps same target`() = runTest {
            viewModel.onSelectDhikr(defaultDhikr.id)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(defaultDhikr.target, state.target)
            assertEquals(defaultDhikr.id, state.selectedDhikr?.id)
        }
    }

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Initial UiState")
    inner class InitialState {

        @Test
        @DisplayName("initial count is 0")
        fun `initial count is zero`() {
            assertEquals(0, viewModel.uiState.value.count)
        }

        @Test
        @DisplayName("initial isGoalReached is false")
        fun `initial isGoalReached is false`() {
            assertFalse(viewModel.uiState.value.isGoalReached)
        }

        @Test
        @DisplayName("initial target equals default dhikr target")
        fun `initial target equals default dhikr target`() {
            assertEquals(defaultDhikr.target, viewModel.uiState.value.target)
        }
    }
}
