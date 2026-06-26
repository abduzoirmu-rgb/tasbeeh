package com.tasbeeh.app

import app.cash.turbine.test
import com.tasbeeh.app.domain.model.Dhikr
import com.tasbeeh.app.domain.model.Session
import com.tasbeeh.app.domain.repository.DhikrRepository
import com.tasbeeh.app.domain.repository.SessionRepository
import com.tasbeeh.app.domain.usecase.GetDhikrsUseCase
import com.tasbeeh.app.domain.usecase.SaveSessionUseCase
import com.tasbeeh.app.presentation.counter.CounterEvent
import com.tasbeeh.app.presentation.counter.CounterUiState
import com.tasbeeh.app.presentation.counter.CounterViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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

    // Three preset dhikrs matching the DB seed from the ADR
    private val dhikr1 = Dhikr(id = 1L, name = "Субханаллах",    arabicText = "سُبْحَانَ ٱللَّٰهِ", targetCount = 33)
    private val dhikr2 = Dhikr(id = 2L, name = "Альхамдулиллах", arabicText = "ٱلْحَمْدُ لِلَّٰهِ", targetCount = 33)
    private val dhikr3 = Dhikr(id = 3L, name = "Аллаху Акбар",   arabicText = "ٱللَّٰهُ أَكْبَرُ",  targetCount = 34)

    private val dhikrRepository: DhikrRepository   = mockk()
    private val sessionRepository: SessionRepository = mockk(relaxed = true)

    private lateinit var getDhikrsUseCase: GetDhikrsUseCase
    private lateinit var saveSessionUseCase: SaveSessionUseCase
    private lateinit var viewModel: CounterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { dhikrRepository.getDhikrs() } returns flowOf(listOf(dhikr1, dhikr2, dhikr3))

        getDhikrsUseCase  = GetDhikrsUseCase(dhikrRepository)
        saveSessionUseCase = SaveSessionUseCase(sessionRepository)

        viewModel = CounterViewModel(
            getDhikrsUseCase   = getDhikrsUseCase,
            saveSessionUseCase = saveSessionUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // 1. After the dhikrs flow emits the ViewModel should have all three dhikrs,
    //    count = 0, selectedDhikrIndex = 0, target = 33, isComplete = false.
    @Test
    fun initialState_isCorrect() = runTest {
        viewModel.uiState.test {
            // Advance so the init coroutine processes the flow emission
            testDispatcher.scheduler.advanceUntilIdle()

            val state: CounterUiState = expectMostRecentItem()

            assertEquals(3,     state.dhikrs.size)
            assertEquals(0,     state.count)
            assertEquals(0,     state.selectedDhikrIndex)
            assertEquals(33,    state.target)
            assertFalse(state.isComplete)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // 2. A single Increment raises count to 1 and isComplete stays false.
    @Test
    fun increment_increasesCount() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem() // consume settled initial state

            viewModel.onEvent(CounterEvent.Increment)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals(1, state.count)
            assertFalse(state.isComplete)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // 3. Tapping 33 times (== target) marks isComplete = true and calls saveSessionUseCase
    //    with completed = true.
    @Test
    fun increment_toTarget_setsIsComplete_andSavesSession() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        repeat(33) { viewModel.onEvent(CounterEvent.Increment) }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)

        coVerify {
            saveSessionUseCase(
                match { session: Session -> session.completed && session.count == 33 }
            )
        }
    }

    // 4. After 5 increments, Reset brings count back to 0.
    @Test
    fun reset_resetsCount() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        repeat(5) { viewModel.onEvent(CounterEvent.Increment) }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(CounterEvent.Reset)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.count)
    }

    // 5. SelectDhikr(1) after 5 taps → selectedDhikrIndex = 1, count = 0.
    @Test
    fun selectDhikr_changesIndexAndResetsCount() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        repeat(5) { viewModel.onEvent(CounterEvent.Increment) }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(CounterEvent.SelectDhikr(1))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.selectedDhikrIndex)
        assertEquals(0, state.count)
    }

    // 6. dhikr at index 2 ("Аллаху Акбар") has target = 34 per the ADR.
    @Test
    fun selectDhikrIndex2_hasTarget34() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(CounterEvent.SelectDhikr(2))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(34, viewModel.uiState.value.target)
    }
}
