package com.jd.fitness.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.local.entity.HabitEntity
import com.jd.fitness.data.local.entity.HabitLogEntity
import com.jd.fitness.data.repository.DaySummary
import com.jd.fitness.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class CalendarDayUi(
    val date: LocalDate,
    val completedCount: Int,
    val totalCount: Int,
    val isPerfect: Boolean,
    val hasLogs: Boolean
)

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val monthDays: List<CalendarDayUi> = emptyList(),
    val selectedDateLogs: List<Pair<HabitEntity, Boolean>> = emptyList(),
    val selectedDaySummary: DaySummary? = null
)

class CalendarViewModel(
    private val repository: FitnessRepository
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadMonthData(_uiState.value.currentMonth)
        selectDate(LocalDate.now())
    }

    fun previousMonth() {
        val prev = _uiState.value.currentMonth.minusMonths(1)
        _uiState.value = _uiState.value.copy(currentMonth = prev)
        loadMonthData(prev)
    }

    fun nextMonth() {
        val next = _uiState.value.currentMonth.plusMonths(1)
        _uiState.value = _uiState.value.copy(currentMonth = next)
        loadMonthData(next)
    }

    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        val dateStr = date.format(dateFormatter)

        viewModelScope.launch {
            repository.getDaySummary(dateStr).collect { summary ->
                _uiState.value = _uiState.value.copy(selectedDaySummary = summary)
            }
        }

        viewModelScope.launch {
            repository.getActiveHabits().collect { activeHabits ->
                repository.getHabitLogsForDate(dateStr).collect { logs ->
                    val logMap = logs.associate { it.habitId to it.isCompleted }
                    val list = activeHabits.map { habit ->
                        habit to (logMap[habit.id] ?: false)
                    }
                    _uiState.value = _uiState.value.copy(selectedDateLogs = list)
                }
            }
        }
    }

    fun toggleHabitForSelectedDate(habitId: Long, isChecked: Boolean) {
        viewModelScope.launch {
            val dateStr = _uiState.value.selectedDate.format(dateFormatter)
            repository.toggleHabitCompletion(dateStr, habitId, isChecked)
            loadMonthData(_uiState.value.currentMonth)
        }
    }

    private fun loadMonthData(month: YearMonth) {
        viewModelScope.launch {
            val start = month.atDay(1).format(dateFormatter)

            repository.getActiveHabits().collect { activeHabits ->
                val totalActive = activeHabits.size
                repository.getHabitLogsForDate(start) // trigger flow or get between dates
                // Load logs for the month
                val allDatesInMonth = (1..month.lengthOfMonth()).map { month.atDay(it) }
                val dayUis = allDatesInMonth.map { date ->
                    CalendarDayUi(
                        date = date,
                        completedCount = 0,
                        totalCount = totalActive,
                        isPerfect = false,
                        hasLogs = false
                    )
                }
                _uiState.value = _uiState.value.copy(monthDays = dayUis)
            }
        }
    }
}

