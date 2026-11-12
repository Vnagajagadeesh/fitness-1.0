package com.jd.fitness.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.health.HealthConnectManager
import com.jd.fitness.data.local.entity.CoachInsightEntity
import com.jd.fitness.data.local.entity.FastingEntity
import com.jd.fitness.data.local.entity.HabitEntity
import com.jd.fitness.data.local.entity.PlannerTaskEntity
import com.jd.fitness.data.local.entity.RecoveryEntity
import com.jd.fitness.data.local.entity.WeeklyReportEntity
import com.jd.fitness.data.local.entity.WeightEntity
import com.jd.fitness.data.preferences.AppUserPreferences
import com.jd.fitness.data.preferences.UserPreferencesRepository
import com.jd.fitness.data.repository.ChallengeProgress
import com.jd.fitness.data.repository.DaySummary
import com.jd.fitness.data.repository.FitnessRepository
import com.jd.fitness.data.repository.StreakInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    val preferences: AppUserPreferences = AppUserPreferences(),
    val coachInsight: CoachInsightEntity? = null,
    val todayPlannerTasks: List<PlannerTaskEntity> = emptyList(),
    val todayRecovery: RecoveryEntity? = null,
    val unlockedMilestonesCount: Int = 0,
    val latestWeeklyReport: WeeklyReportEntity? = null
)

class DashboardViewModel(
    private val repository: FitnessRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
        observeData()
    }

    fun refreshAll() {
        val displayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.US))
        _uiState.value = _uiState.value.copy(currentDateFormatted = displayDate)

        viewModelScope.launch {
            val streaks = repository.calculateStreaks()
            _uiState.value = _uiState.value.copy(streakInfo = streaks)
            repository.evaluateAndUnlockAchievements()
        }

        refreshSteps()
    }

    private fun observeData() {
        val todayStr = repository.getTodayString()

        // 1. Challenge progress
        viewModelScope.launch {
            repository.getChallengeProgressFlow().collect { progress ->
                _uiState.value = _uiState.value.copy(challengeProgress = progress)
            }
        }

        // 2. Day summary (habits completion %)
        viewModelScope.launch {
            repository.getDaySummary(todayStr).collect { summary ->
                _uiState.value = _uiState.value.copy(daySummary = summary)
            }
        }

        // 3. Active habits and completion for today (safe combine without nested collect)
        viewModelScope.launch {
            combine(
                repository.getActiveHabits(),
                repository.getHabitLogsForDate(todayStr)
            ) { activeHabits, logs ->
                val logsMap = logs.associate { it.habitId to it.isCompleted }
                activeHabits.map { habit ->
                    habit to (logsMap[habit.id] ?: false)
                }
            }.collect { habitPairs ->
                _uiState.value = _uiState.value.copy(habits = habitPairs)
            }
        }

        // 4. Latest weight
        viewModelScope.launch {
            repository.getLatestWeight().collect { weight ->
                _uiState.value = _uiState.value.copy(latestWeight = weight)
            }
        }

        // 5. Active fasting
        viewModelScope.launch {
            repository.getActiveFastingSession().collect { fasting ->
                _uiState.value = _uiState.value.copy(activeFastingSession = fasting)
            }
        }

        // 6. Steps for today from database
        viewModelScope.launch {
            repository.getStepsForDate(todayStr).collect { stepLog ->
                _uiState.value = _uiState.value.copy(todaySteps = stepLog?.stepCount ?: 0)
            }
        }

        // 7. User preferences
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(preferences = prefs)
            }
        }

        // 8. FITNESS 1.2: AI Coach Insight
        viewModelScope.launch {
            repository.getLatestCoachInsight().collect { insight ->
                _uiState.value = _uiState.value.copy(coachInsight = insight)
            }
        }

        // 9. FITNESS 1.2: Today's Planner Tasks
        viewModelScope.launch {
            repository.getPlannerTasksForDate(todayStr).collect { tasks ->
                _uiState.value = _uiState.value.copy(todayPlannerTasks = tasks)
            }
        }

        // 10. FITNESS 1.2: Recovery status
        viewModelScope.launch {
            repository.getRecoveryForDate(todayStr).collect { recovery ->
                _uiState.value = _uiState.value.copy(todayRecovery = recovery)
            }
        }

        // 11. FITNESS 1.2: Unlocked Milestones
        viewModelScope.launch {
            repository.getUnlockedAchievementsCount().collect { count ->
                _uiState.value = _uiState.value.copy(unlockedMilestonesCount = count)
            }
        }

        // 12. FITNESS 1.2: Latest Weekly Report
        viewModelScope.launch {
            repository.getLatestWeeklyReport().collect { report ->
                _uiState.value = _uiState.value.copy(latestWeeklyReport = report)
            }
        }
    }

    fun toggleHabit(habitId: Long, isChecked: Boolean) {
        val todayStr = repository.getTodayString()
        viewModelScope.launch {
            repository.toggleHabitCompletion(todayStr, habitId, isChecked)
            val updatedStreaks = repository.calculateStreaks()
            _uiState.value = _uiState.value.copy(streakInfo = updatedStreaks)
            repository.evaluateAndUnlockAchievements()
        }
    }

    fun refreshSteps() {
        val todayStr = repository.getTodayString()
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
