package com.jd.fitness.ui.screens.fasting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.local.entity.FastingEntity
import com.jd.fitness.data.local.entity.FastingMealEntity
import com.jd.fitness.data.local.entity.FastingScheduleEntity
import com.jd.fitness.data.local.entity.FastingState
import com.jd.fitness.data.preferences.UserPreferencesRepository
import com.jd.fitness.data.repository.FitnessRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class FastingStageInfo(
    val stageNumber: Int,
    val rangeText: String,
    val title: String,
    val description: String,
    val isCurrent: Boolean,
    val isCompleted: Boolean
)

data class FastingUiState(
    val activeSession: FastingEntity? = null,
    val targetHours: Int = 16,
    val isOMAD: Boolean = false,
    val plannedStartTime: Long = System.currentTimeMillis(),
    val plannedEndTime: Long? = null,
    val pastSessions: List<FastingEntity> = emptyList(),
    val savedSchedules: List<FastingScheduleEntity> = emptyList(),
    val sessionMeals: List<FastingMealEntity> = emptyList(),
    val isUnderageNoticeAccepted: Boolean = false,
    val showScheduleDialog: Boolean = false,
    val showCustomTimeDialog: Boolean = false,
    val showMealDialog: Boolean = false,
    val editingSession: FastingEntity? = null,
    val weeklyTotalHours: Float = 0f,
    val monthlyTotalHours: Float = 0f
)

class FastingViewModel(
    private val repository: FitnessRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FastingUiState())
    val uiState: StateFlow<FastingUiState> = _uiState.asStateFlow()

    // Isolated live timer state to prevent recomposing non-timer elements
    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    init {
        loadData()
        startLiveTimer()
    }

    private fun loadData() {
        // Active session
        viewModelScope.launch {
            repository.getActiveFastingSession().collect { session ->
                _uiState.value = _uiState.value.copy(
                    activeSession = session,
                    targetHours = session?.targetHours ?: _uiState.value.targetHours,
                    isOMAD = session?.isOMAD ?: false,
                    plannedStartTime = session?.plannedStartTime ?: System.currentTimeMillis(),
                    plannedEndTime = session?.plannedEndTime
                )
                if (session != null) {
                    val elapsed = (System.currentTimeMillis() - session.startTime) / 1000
                    _elapsedSeconds.value = elapsed.coerceAtLeast(0)
                    loadMealsForSession(session.id)
                } else {
                    _elapsedSeconds.value = 0L
                }
            }
        }

        // Past sessions
        viewModelScope.launch {
            repository.getAllFastingSessions().collect { sessions ->
                val completed = sessions.filter { it.state != FastingState.ACTIVE }
                val now = System.currentTimeMillis()
                val oneWeekAgo = now - 7L * 24 * 3600 * 1000
                val oneMonthAgo = now - 30L * 24 * 3600 * 1000

                val weeklyHours = completed.filter { it.startTime >= oneWeekAgo && it.endTime != null }
                    .sumOf { (it.endTime!! - it.startTime) / 3600000.0 }.toFloat()

                val monthlyHours = completed.filter { it.startTime >= oneMonthAgo && it.endTime != null }
                    .sumOf { (it.endTime!! - it.startTime) / 3600000.0 }.toFloat()

                _uiState.value = _uiState.value.copy(
                    pastSessions = completed,
                    weeklyTotalHours = weeklyHours,
                    monthlyTotalHours = monthlyHours
                )
            }
        }

        // Saved schedules
        viewModelScope.launch {
            repository.getAllFastingSchedules().collect { schedules ->
                _uiState.value = _uiState.value.copy(savedSchedules = schedules)
            }
        }

        // Preferences
        viewModelScope.launch {
            val prefs = preferencesRepository.preferencesFlow.first()
            _uiState.value = _uiState.value.copy(
                isUnderageNoticeAccepted = prefs.isUnderageNoticeAccepted
            )
        }
    }

    private fun loadMealsForSession(sessionId: Long) {
        viewModelScope.launch {
            repository.getFastingMealsForSession(sessionId).collect { meals ->
                _uiState.value = _uiState.value.copy(sessionMeals = meals)
            }
        }
    }

    private fun startLiveTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val session = _uiState.value.activeSession
                if (session != null) {
                    val elapsed = (System.currentTimeMillis() - session.startTime) / 1000
                    _elapsedSeconds.value = elapsed.coerceAtLeast(0)
                }
            }
        }
    }

    fun setSchedule(fastHours: Int, isOMAD: Boolean) {
        val now = System.currentTimeMillis()
        val plannedEnd = now + (fastHours * 3600L * 1000L)
        _uiState.value = _uiState.value.copy(
            targetHours = fastHours,
            isOMAD = isOMAD,
            plannedStartTime = now,
            plannedEndTime = plannedEnd
        )
    }

    fun setCustomTimes(startMillis: Long, endMillis: Long) {
        val durationHours = ((endMillis - startMillis) / 3600000.0).toInt().coerceAtLeast(1)
        _uiState.value = _uiState.value.copy(
            plannedStartTime = startMillis,
            plannedEndTime = endMillis,
            targetHours = durationHours,
            isOMAD = durationHours >= 23
        )
    }

    fun startFast(notes: String = "") {
        viewModelScope.launch {
            val start = _uiState.value.plannedStartTime
            val hours = _uiState.value.targetHours
            val end = _uiState.value.plannedEndTime ?: (start + hours * 3600L * 1000L)
            repository.startFastingWithCustomTimes(
                plannedStartTime = start,
                plannedEndTime = end,
                targetHours = hours,
                isOMAD = _uiState.value.isOMAD,
                notes = notes
            )
        }
    }

    fun endFast(notes: String = "") {
        viewModelScope.launch {
            val active = _uiState.value.activeSession ?: return@launch
            repository.endFasting(active, notes)
            repository.evaluateAndUnlockAchievements()
        }
    }

    fun cancelFast() {
        viewModelScope.launch {
            val active = _uiState.value.activeSession ?: return@launch
            repository.cancelFasting(active)
        }
    }

    fun saveSchedule(name: String, fastHours: Int, eatHours: Int, defaultStart: String) {
        viewModelScope.launch {
            repository.saveFastingSchedule(
                FastingScheduleEntity(
                    name = name,
                    fastingHours = fastHours,
                    eatingHours = eatHours,
                    defaultStartTime = defaultStart
                )
            )
        }
    }

    fun deleteSchedule(schedule: FastingScheduleEntity) {
        viewModelScope.launch {
            repository.deleteFastingSchedule(schedule)
        }
    }

    fun logMeal(title: String, isOMADMeal: Boolean = false, notes: String = "") {
        viewModelScope.launch {
            val sessionId = _uiState.value.activeSession?.id
            repository.logMeal(title, isOMADMeal, notes, sessionId)
        }
    }

    fun updatePastSession(session: FastingEntity) {
        viewModelScope.launch {
            repository.updateFastingSession(session)
        }
    }

    fun deletePastSession(session: FastingEntity) {
        viewModelScope.launch {
            repository.deleteFastingSession(session)
        }
    }

    fun setEditingSession(session: FastingEntity?) {
        _uiState.value = _uiState.value.copy(editingSession = session)
    }

    fun setShowScheduleDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showScheduleDialog = show)
    }

    fun setShowCustomTimeDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCustomTimeDialog = show)
    }

    fun setShowMealDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showMealDialog = show)
    }

    fun acceptUnderageNotice() {
        viewModelScope.launch {
            preferencesRepository.setUnderageNoticeAccepted(true)
            _uiState.value = _uiState.value.copy(isUnderageNoticeAccepted = true)
        }
    }
}
