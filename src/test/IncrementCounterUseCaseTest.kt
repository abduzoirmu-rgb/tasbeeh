package com.example.tasbeh.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for [IncrementCounterUseCase].
 *
 * Covered scenarios:
 *  T1 – Tap → count increments by 1
 *  T2 – Reaching goal → isGoalReached = true
 *       edge cases: count=0 (first tap), count=target-1 (last tap before goal),
 *                   count=target (already at goal, keeps incrementing)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IncrementCounterUseCaseTest {

    private lateinit var useCase: IncrementCounterUseCase

    @BeforeEach
    fun setUp() {
        useCase = IncrementCounterUseCase()
    }

    // -------------------------------------------------------------------------
    // T1 – Basic increment
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("T1 – Increment behaviour")
    inner class IncrementBehaviour {

        @Test
        @DisplayName("count 0 → 1 when target not yet reached")
        fun `increment from zero returns one`() = runTest {
            val result = useCase(currentCount = 0, target = 33)

            assertEquals(1, result.count)
        }

        @Test
        @DisplayName("count 10 → 11 for arbitrary mid-range tap")
        fun `increment mid-range returns next value`() = runTest {
            val result = useCase(currentCount = 10, target = 100)

            assertEquals(11, result.count)
        }

        @Test
        @DisplayName("result count is always currentCount + 1")
        fun `result count equals current plus one`() = runTest {
            val current = 57
            val result = useCase(currentCount = current, target = 100)

            assertEquals(current + 1, result.count)
        }
    }

    // -------------------------------------------------------------------------
    // T2 – Goal detection
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("T2 – Goal detection")
    inner class GoalDetection {

        @Test
        @DisplayName("isGoalReached = false when count < target")
        fun `goal not reached when count below target`() = runTest {
            val result = useCase(currentCount = 0, target = 33)

            assertFalse(result.isGoalReached)
        }

        @Test
        @DisplayName("isGoalReached = false on the tap before reaching target (count=target-2)")
        fun `goal not reached one step before last tap`() = runTest {
            val target = 33
            val result = useCase(currentCount = target - 2, target = target)

            assertFalse(result.isGoalReached)
            assertEquals(target - 1, result.count)
        }

        @Test
        @DisplayName("isGoalReached = true exactly when newCount == target (last tap)")
        fun `goal reached on the tap that hits target`() = runTest {
            val target = 33
            val result = useCase(currentCount = target - 1, target = target)

            assertTrue(result.isGoalReached)
            assertEquals(target, result.count)
        }

        @Test
        @DisplayName("isGoalReached = true when tapping beyond target")
        fun `goal stays reached when tapping past target`() = runTest {
            val target = 33
            val result = useCase(currentCount = target, target = target)

            assertTrue(result.isGoalReached)
            assertEquals(target + 1, result.count)
        }

        @Test
        @DisplayName("isGoalReached = true for target=1 on very first tap")
        fun `goal reached immediately for target one`() = runTest {
            val result = useCase(currentCount = 0, target = 1)

            assertTrue(result.isGoalReached)
            assertEquals(1, result.count)
        }

        @Test
        @DisplayName("isGoalReached = false for very large target mid-progress")
        fun `goal not reached mid-progress on large target`() = runTest {
            val result = useCase(currentCount = 500, target = 1000)

            assertFalse(result.isGoalReached)
            assertEquals(501, result.count)
        }
    }

    // -------------------------------------------------------------------------
    // CounterResult data integrity
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("CounterResult integrity")
    inner class ResultIntegrity {

        @Test
        @DisplayName("returned CounterResult carries the new count, not the old one")
        fun `result count is not the original count`() = runTest {
            val original = 7
            val result = useCase(currentCount = original, target = 100)

            assertEquals(original + 1, result.count)
        }

        @Test
        @DisplayName("two successive calls are independent – state is not held inside use-case")
        fun `use case is stateless between calls`() = runTest {
            val first  = useCase(currentCount = 5, target = 33)
            val second = useCase(currentCount = 5, target = 33)

            assertEquals(first.count, second.count)
            assertEquals(first.isGoalReached, second.isGoalReached)
        }
    }
}
