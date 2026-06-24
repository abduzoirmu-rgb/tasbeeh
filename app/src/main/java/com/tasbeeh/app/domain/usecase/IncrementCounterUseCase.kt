package com.tasbeeh.app.domain.usecase

import javax.inject.Inject

data class CounterResult(val count: Int, val isGoalReached: Boolean)

class IncrementCounterUseCase @Inject constructor() {
    operator fun invoke(currentCount: Int, target: Int): CounterResult {
        val newCount = currentCount + 1
        val isGoalReached = target > 0 && newCount >= target
        return CounterResult(count = newCount, isGoalReached = isGoalReached)
    }
}
