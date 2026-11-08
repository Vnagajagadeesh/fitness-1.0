package com.jd.fitness.ui.screens.steps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.health.HealthConnectManager
import com.jd.fitness.data.health.HealthConnectStatus
import com.jd.fitness.data.local.entity.StepLogEntity
import com.jd.fitness.data.preferences.UserPreferencesRepository
import com.jd.fitness.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

data class StepUiState(
    val todaySteps: Int = 0,
    val stepGoal: Int = 10000,
    val isGoalReached: Boolean = false,
    val remainingSteps: Int = 10000,
    val weeklyHistory: List<Pair<String, Int>> = emptyList(),
    val healthConnectStatus: HealthConnectStatus = HealthConnectStatus.Available,
    val hasPermission: Boolean = false,
    val isSyncing: Boolean = false,
    val showManualEntryDialog: Boolean = false
)

class StepViewModel(
    private val repository: FitnessRepository,
    private val healthConnectManager: HealthConnectManager,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StepUiState())
    val uiState: StateFlow<StepUiState> = _uiState.asStateFlow()

    private val today = repository.getTodayString()

    init {
        checkHealthConnectStatus()
        observeData()
        syncStepsFromHealthConnect()
    }

    private fun checkHealthConnectStatus() {
        val status = healthConnectManager.getAvailabilityStatus()
        _uiState.value = _uiState.value.copy(healthConnectStatus = status)
        viewModelScope.launch {
            val hasPerm = healthConnectManager.hasPermissions()
            _uiState.value = _uiState.value.copy(hasPermission = hasPerm)
        }
    }

    private fun observeData() {
        // Today's steps
        viewModelScope.launch {
            repository.getStepsForDate(today).collect { log ->
                val count = log?.stepCount ?: 0
                val goal = _uiState.value.stepGoal
                _uiState.value = _uiState.value.copy(
                    todaySteps = count,
                    isGoalReached = count >= goal,
                    remainingSteps = (goal - count).coerceAtLeast(0)
                )
            }
        }

        // Weekly history (last 7 days)
        viewModelScope.launch {
            val weekAgo = LocalDate.now().minusDays(6).toString()
            repository.getStepsBetween(weekAgo, today).collect { logs ->
                val logMap = logs.associate { it.date to it.stepCount }
                val history = (0..6).map { i ->
                    val d = LocalDate.now().minusDays((6 - i).toLong()).toString()
                    val count = logMap[d] ?: 0
                    d to count
                }
                _uiState.value = _uiState.value.copy(weeklyHistory = history)
            }
        }

        // Preferences
        viewModelScope.launch {
            val prefs = preferencesRepository.preferencesFlow.first()
            _uiState.value = _uiState.value.copy(stepGoal = prefs.stepGoal)
        }
    }

    fun syncStepsFromHealthConnect() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            checkHealthConnectStatus()
            if (healthConnectManager.hasPermissions()) {
                val todayCount = healthConnectManager.readStepsForDate(LocalDate.now()).toInt()
                repository.saveStepCount(today, todayCount, "HEALTH_CONNECT")

                // Sync past 7 days
                val pastDays = healthConnectManager.readPastDaysSteps(7)
                pastDays.forEach { (date, count) ->
                    repository.saveStepCount(date.toString(), count.toInt(), "HEALTH_CONNECT")
                }
            }
            _uiState.value = _uiState.value.copy(isSyncing = false)
        }
    }

    fun openManualEntry() {
        _uiState.value = _uiState.value.copy(showManualEntryDialog = true)
    }

    fun closeManualEntry() {
        _uiState.value = _uiState.value.copy(showManualEntryDialog = false)
    }

    fun saveManualSteps(count: Int) {
        viewModelScope.launch {
            repository.saveStepCount(today, count, "MANUAL")
            closeManualEntry()
        }
    }
}

