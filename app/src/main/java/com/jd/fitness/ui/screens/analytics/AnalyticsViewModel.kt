package com.jd.fitness.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.local.entity.HabitEntity
import com.jd.fitness.data.repository.ChallengeProgress
import com.jd.fitness.data.repository.FitnessRepository
import com.jd.fitness.data.repository.StreakInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitPerformanceUi(
    val habit: HabitEntity,
    val completedDays: Int,
    val consistencyPercentage: Float
)

data class AnalyticsUiState(
    val challengeProgress: ChallengeProgress? = null,
    val streakInfo: StreakInfo = StreakInfo(0, 0, 0, 0),
    val weeklyConsistencyScore: Float = 0f,
    val monthlyConsistencyScore: Float = 0f,
    val habitPerformanceList: List<HabitPerformanceUi> = emptyList(),
    val weeklyHeatmap: List<Pair<String, Float>> = emptyList(),
    val weightTrend: List<Pair<String, Double>> = emptyList(),
    val stepTrend: List<Pair<String, Int>> = emptyList()
)

class AnalyticsViewModel(
    private val repository: FitnessRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            repository.getChallengeProgressFlow().collect { progress ->
                _uiState.value = _uiState.value.copy(challengeProgress = progress)
            }
        }

        viewModelScope.launch {
            val streaks = repository.calculateStreaks()
            _uiState.value = _uiState.value.copy(streakInfo = streaks)
        }

        // Weight trend for analytics
        viewModelScope.launch {
            repository.getAllWeightLogs().collect { logs ->
                val points = logs.sortedBy { it.timestamp }.map { it.date to it.weightKg }
                _uiState.value = _uiState.value.copy(weightTrend = points)
            }
        }

        // Steps trend for analytics
        viewModelScope.launch {
            val weekAgo = LocalDate.now().minusDays(6).toString()
            val today = repository.getTodayString()
            repository.getStepsBetween(weekAgo, today).collect { stepLogs ->
                val logMap = stepLogs.associate { it.date to it.stepCount }
                val trend = (0..6).map { i ->
                    val d = LocalDate.now().minusDays((6 - i).toLong()).toString()
                    d to (logMap[d] ?: 0)
                }
                _uiState.value = _uiState.value.copy(stepTrend = trend)
            }
        }

        // Calculate habit-specific performance and weekly consistency from database
        viewModelScope.launch {
            repository.getActiveHabits().collect { habits ->
                val today = LocalDate.now()
                val weekDays = (0..6).map { i -> today.minusDays((6 - i).toLong()) }
                val weekDayNames = listOf("M", "T", "W", "T", "F", "S", "S")

                val heatmapList = mutableListOf<Pair<String, Float>>()
                var weeklyTotalScore = 0f

                weekDays.forEachIndexed { idx, date ->
                    val dateStr = date.toString()
                    val logs = repository.getDaySummary(dateStr)
                    // Simple representation
                    heatmapList.add(Pair(date.dayOfWeek.name.take(1), 0f))
                }

                val perfList = habits.map { habit ->
                    HabitPerformanceUi(
                        habit = habit,
                        completedDays = 0,
                        consistencyPercentage = 0f
                    )
                }

                _uiState.value = _uiState.value.copy(
                    habitPerformanceList = perfList,
                    weeklyHeatmap = heatmapList
                )
            }
        }
    }
}
