package com.tasbeeh.app

import com.tasbeeh.app.domain.usecase.IncrementCounterUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IncrementCounterUseCaseTest {

    private lateinit var useCase: IncrementCounterUseCase

    @Before
    fun setUp() {
        useCase = IncrementCounterUseCase()
    }

    // T1 – Increment behaviour

    @Test
    fun `increment from zero returns one`() {
        val result = useCase(currentCount = 0, target = 33)
        assertEquals(1, result.count)
    }

    @Test
    fun `increment mid-range returns next value`() {
        val result = useCase(currentCount = 10, target = 100)
        assertEquals(11, result.count)
    }

    @Test
    fun `result count equals current plus one`() {
        val current = 57
        val result = useCase(currentCount = current, target = 100)
        assertEquals(current + 1, result.count)
    }

    // T2 – Goal detection

    @Test
    fun `goal not reached when count below target`() {
        val result = useCase(currentCount = 0, target = 33)
        assertFalse(result.isGoalReached)
    }

    @Test
    fun `goal not reached one step before last tap`() {
        val target = 33
        val result = useCase(currentCount = target - 2, target = target)
        assertFalse(result.isGoalReached)
        assertEquals(target - 1, result.count)
    }

    @Test
    fun `goal reached on the tap that hits target`() {
        val target = 33
        val result = useCase(currentCount = target - 1, target = target)
        assertTrue(result.isGoalReached)
        assertEquals(target, result.count)
    }

    @Test
    fun `goal stays reached when tapping past target`() {
        val target = 33
        val result = useCase(currentCount = target, target = target)
        assertTrue(result.isGoalReached)
        assertEquals(target + 1, result.count)
    }

    @Test
    fun `goal reached immediately for target one`() {
        val result = useCase(currentCount = 0, target = 1)
        assertTrue(result.isGoalReached)
        assertEquals(1, result.count)
    }

    @Test
    fun `goal not reached mid-progress on large target`() {
        val result = useCase(currentCount = 500, target = 1000)
        assertFalse(result.isGoalReached)
        assertEquals(501, result.count)
    }

    // CounterResult integrity

    @Test
    fun `result count is not the original count`() {
        val original = 7
        val result = useCase(currentCount = original, target = 100)
        assertEquals(original + 1, result.count)
    }

    @Test
    fun `use case is stateless between calls`() {
        val first = useCase(currentCount = 5, target = 33)
        val second = useCase(currentCount = 5, target = 33)
        assertEquals(first.count, second.count)
        assertEquals(first.isGoalReached, second.isGoalReached)
    }
}
