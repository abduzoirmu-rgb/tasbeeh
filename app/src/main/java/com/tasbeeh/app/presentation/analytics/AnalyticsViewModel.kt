package com.tasbeeh.app.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasbeeh.app.domain.model.Session
import com.tasbeeh.app.domain.usecase.GetSessionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DayStats(val dayOfWeek: Int, val count: Int, val isToday: Boolean)

data class AnalyticsUiState(
    val sessions: List<Session> = emptyList(),
    val totalZikrs: Int = 0,
    val completedSessions: Int = 0,
    val weekStats: List<DayStats> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getSessionsUseCase: GetSessionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState

    init {
        viewModelScope.launch {
            getSessionsUseCase().collect { sessions ->
                _uiState.update {
                    it.copy(
                        sessions          = sessions.take(50),
                        totalZikrs        = sessions.sumOf { s -> s.count },
                        completedSessions = sessions.count { s -> s.completed },
                        weekStats         = buildWeekStats(sessions),
                        isLoading         = false
                    )
                }
            }
        }
    }

    private fun buildWeekStats(sessions: List<Session>): List<DayStats> {
        val cal = Calendar.getInstance()
        return (6 downTo 0).map { daysAgo ->
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val startOfDay = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val endOfDay = startOfDay + 86_400_000L
            val count = sessions.filter { it.timestamp in startOfDay until endOfDay }.sumOf { it.count }
            DayStats(dayOfWeek = dayOfWeek, count = count, isToday = daysAgo == 0)
        }
    }
}
