package com.jd.fitness.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.health.HealthConnectManager
import com.jd.fitness.data.local.entity.FastingEntity
import com.jd.fitness.data.local.entity.HabitEntity
import com.jd.fitness.data.local.entity.HabitLogEntity
import com.jd.fitness.data.local.entity.WeightEntity
import com.jd.fitness.data.preferences.AppUserPreferences
import com.jd.fitness.data.preferences.UserPreferencesRepository
import com.jd.fitness.data.repository.ChallengeProgress
import com.jd.fitness.data.repository.DaySummary
import com.jd.fitness.data.repository.FitnessRepository
import com.jd.fitness.data.repository.StreakInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DashboardUiState(
    val currentDateFormatted: String = "",
    val challengeProgress: ChallengeProgress? = null,
    val daySummary: DaySummary? = null,
    val streakInfo: StreakInfo = StreakInfo(0, 0, 0, 0),
    val todaySteps: Int = 0,
    val latestWeight: WeightEntity? = null,
    val activeFastingSession: FastingEntity? = null,
    val habits: List<Pair<HabitEntity, Boolean>> = emptyList(),
    val preferences: AppUserPreferences = AppUserPreferences()
)

class DashboardViewModel(
    private val repository: FitnessRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val todayStr = repository.getTodayString()

    init {
        val displayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.US))
        _uiState.value = _uiState.value.copy(currentDateFormatted = displayDate)

        observeData()
        refreshSteps()
    }

    private fun observeData() {
        // Challenge progress
        viewModelScope.launch {
            repository.getChallengeProgressFlow().collect { progress ->
                _uiState.value = _uiState.value.copy(challengeProgress = progress)
            }
        }

        // Day summary (habits completion %)
        viewModelScope.launch {
            repository.getDaySummary(todayStr).collect { summary ->
                _uiState.value = _uiState.value.copy(daySummary = summary)
            }
        }

        // Streaks
        viewModelScope.launch {
            val streaks = repository.calculateStreaks()
            _uiState.value = _uiState.value.copy(streakInfo = streaks)
        }

        // Active habits and completion for today
        viewModelScope.launch {
            repository.getActiveHabits().collect { activeHabits ->
                repository.getHabitLogsForDate(todayStr).collect { logs ->
                    val logsMap = logs.associate { it.habitId to it.isCompleted }
                    val habitPairs = activeHabits.map { habit ->
                        habit to (logsMap[habit.id] ?: false)
                    }
                    _uiState.value = _uiState.value.copy(habits = habitPairs)
                }
            }
        }

        // Latest weight
        viewModelScope.launch {
            repository.getLatestWeight().collect { weight ->
                _uiState.value = _uiState.value.copy(latestWeight = weight)
            }
        }

        // Active fasting
        viewModelScope.launch {
            repository.getActiveFastingSession().collect { fasting ->
                _uiState.value = _uiState.value.copy(activeFastingSession = fasting)
            }
        }

        // Steps for today from database
        viewModelScope.launch {
            repository.getStepsForDate(todayStr).collect { stepLog ->
                _uiState.value = _uiState.value.copy(todaySteps = stepLog?.stepCount ?: 0)
            }
        }

        // User preferences
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(preferences = prefs)
            }
        }
    }

    fun toggleHabit(habitId: Long, isChecked: Boolean) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(todayStr, habitId, isChecked)
            val updatedStreaks = repository.calculateStreaks()
            _uiState.value = _uiState.value.copy(streakInfo = updatedStreaks)
        }
    }

    fun refreshSteps() {
        viewModelScope.launch {
            if (healthConnectManager.hasPermissions()) {
                val steps = healthConnectManager.readStepsForDate(LocalDate.now()).toInt()
                if (steps > 0) {
                    repository.saveStepCount(todayStr, steps, "HEALTH_CONNECT")
                }
            }
        }
    }
}

