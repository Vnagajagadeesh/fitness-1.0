package com.jd.fitness.ui.screens.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.local.entity.HabitCategory
import com.jd.fitness.data.local.entity.PerceivedEnergy
import com.jd.fitness.data.local.entity.RecoveryEntity
import com.jd.fitness.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class RecoveryUiState(
    val selectedDate: String = "",
    val todayEntry: RecoveryEntity? = null,
    val recentEntries: List<RecoveryEntity> = emptyList(),
    val isRestDay: Boolean = false,
    val sleepHours: Float = 7.5f,
    val perceivedEnergy: String = PerceivedEnergy.NORMAL.name,
    val workoutDone: Boolean = false,
    val workoutType: String = "Strength",
    val notes: String = "",
    val consecutiveWorkoutDays: Int = 0,
    val weeklyRestDaysCount: Int = 0,
    val averageSleepHours: Float = 7.5f,
    val intelligentRecommendation: String = ""
)

class RecoveryViewModel(
    private val repository: FitnessRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecoveryUiState())
    val uiState: StateFlow<RecoveryUiState> = _uiState.asStateFlow()

    private val todayStr = repository.getTodayString()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        _uiState.value = _uiState.value.copy(selectedDate = todayStr)
        loadData()
    }

    private fun loadData() {
        // Today's entry
        viewModelScope.launch {
            repository.getRecoveryForDate(todayStr).collect { entry ->
                if (entry != null) {
                    _uiState.value = _uiState.value.copy(
                        todayEntry = entry,
                        isRestDay = entry.isRestDay,
                        sleepHours = entry.sleepHours,
                        perceivedEnergy = entry.perceivedEnergy,
                        workoutDone = entry.workoutDone,
                        workoutType = entry.workoutType,
                        notes = entry.notes
                    )
                }
                evaluateRecommendations()
            }
        }

        // Last 14 days entries
        viewModelScope.launch {
            val startStr = LocalDate.now().minusDays(14).format(dateFormatter)
            repository.getRecoveryBetweenDates(startStr, todayStr).collect { entries ->
                val last7DaysEntries = entries.filter {
                    val date = runCatching { LocalDate.parse(it.date, dateFormatter) }.getOrNull()
                    date != null && !date.isBefore(LocalDate.now().minusDays(7))
                }
                val avgSleep = if (last7DaysEntries.isNotEmpty()) {
                    last7DaysEntries.map { it.sleepHours }.average().toFloat()
                } else 7.5f

                val restCount = last7DaysEntries.count { it.isRestDay }

                _uiState.value = _uiState.value.copy(
                    recentEntries = entries.sortedByDescending { it.date },
                    weeklyRestDaysCount = restCount,
                    averageSleepHours = avgSleep
                )
                evaluateRecommendations()
            }
        }
    }

    fun setRestDay(isRest: Boolean) {
        _uiState.value = _uiState.value.copy(
            isRestDay = isRest,
            workoutDone = if (isRest) false else _uiState.value.workoutDone
        )
        saveCurrentEntry()
    }

    fun setSleepHours(hours: Float) {
        _uiState.value = _uiState.value.copy(sleepHours = hours)
        saveCurrentEntry()
    }

    fun setPerceivedEnergy(energy: PerceivedEnergy) {
        _uiState.value = _uiState.value.copy(perceivedEnergy = energy.name)
        saveCurrentEntry()
    }

    fun setWorkoutDone(done: Boolean, type: String = _uiState.value.workoutType) {
        _uiState.value = _uiState.value.copy(
            workoutDone = done,
            workoutType = type,
            isRestDay = if (done) false else _uiState.value.isRestDay
        )
        saveCurrentEntry()
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun saveCurrentEntry() {
        viewModelScope.launch {
            val state = _uiState.value
            val entry = RecoveryEntity(
                id = state.todayEntry?.id ?: 0,
                date = todayStr,
                isRestDay = state.isRestDay,
                sleepHours = state.sleepHours,
                perceivedEnergy = state.perceivedEnergy,
                workoutDone = state.workoutDone,
                workoutType = state.workoutType,
                notes = state.notes
            )
            repository.saveRecoveryEntry(entry)
            evaluateRecommendations()
        }
    }

    private suspend fun evaluateRecommendations() {
        val state = _uiState.value
        val habits = repository.getAllHabits().first()
        val workoutHabits = habits.filter { it.category == HabitCategory.FITNESS }.map { it.id }.toSet()

        // Check consecutive workout days from habit logs
        var consecutiveWorkouts = 0
        var checkDate = LocalDate.now()
        for (i in 0..7) {
            val dateStr = checkDate.format(dateFormatter)
            val logs = repository.getHabitLogsForDate(dateStr).first()
            val hasWorkout = logs.any { it.isCompleted && workoutHabits.contains(it.habitId) }
            if (hasWorkout) {
                consecutiveWorkouts++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }

        val recommendation = when {
            consecutiveWorkouts >= 3 ->
                "You have recorded $consecutiveWorkouts consecutive workout days. Scheduling an active rest or mobility day will support muscle recovery and prevent overreaching."
            state.sleepHours < 6.0f ->
                "Your recorded sleep (${state.sleepHours}h) is shorter than usual. Prioritize an early evening wind-down, steady hydration, and lighter training volume."
            state.perceivedEnergy == PerceivedEnergy.NEED_REST.name || state.perceivedEnergy == PerceivedEnergy.TIRED.name ->
                "Your recorded energy is low today. Emphasize restorative stretching, hydration, and wholesome nutrition rather than high strain."
            state.isRestDay ->
                "Rest day active. Remember that recovery is where training adaptations occur. Enjoy your physical recharge guilt-free."
            else ->
                "Your recovery balance looks sound. Keep up regular sleep habits and listen to your body throughout your session."
        }

        _uiState.value = _uiState.value.copy(
            consecutiveWorkoutDays = consecutiveWorkouts,
            intelligentRecommendation = recommendation
        )
    }
}

