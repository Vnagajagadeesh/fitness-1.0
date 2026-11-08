package com.jd.fitness.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.local.entity.HabitCategory
import com.jd.fitness.data.local.entity.HabitEntity
import com.jd.fitness.data.local.entity.HabitLogEntity
import com.jd.fitness.data.repository.DaySummary
import com.jd.fitness.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HabitItemUi(
    val habit: HabitEntity,
    val isCompleted: Boolean,
    val streak: Int = 0
)

data class HabitUiState(
    val selectedDate: String = "",
    val selectedDateDisplay: String = "",
    val isToday: Boolean = true,
    val habitItems: List<HabitItemUi> = emptyList(),
    val daySummary: DaySummary? = null,
    val selectedCategory: HabitCategory? = null, // null means All
    val editingHabit: HabitEntity? = null,
    val showAddEditDialog: Boolean = false
)

class HabitViewModel(
    private val repository: FitnessRepository
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.US)

    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    init {
        setDate(LocalDate.now())
    }

    fun setDate(date: LocalDate) {
        val dateStr = date.format(dateFormatter)
        val isToday = date == LocalDate.now()
        _uiState.value = _uiState.value.copy(
            selectedDate = dateStr,
            selectedDateDisplay = date.format(displayFormatter),
            isToday = isToday
        )
        loadDataForDate(dateStr)
    }

    fun previousDay() {
        val current = runCatching { LocalDate.parse(_uiState.value.selectedDate, dateFormatter) }.getOrDefault(LocalDate.now())
        setDate(current.minusDays(1))
    }

    fun nextDay() {
        val current = runCatching { LocalDate.parse(_uiState.value.selectedDate, dateFormatter) }.getOrDefault(LocalDate.now())
        setDate(current.plusDays(1))
    }

    fun setCategoryFilter(category: HabitCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    private fun loadDataForDate(dateStr: String) {
        viewModelScope.launch {
            repository.getDaySummary(dateStr).collect { summary ->
                _uiState.value = _uiState.value.copy(daySummary = summary)
            }
        }

        viewModelScope.launch {
            repository.getActiveHabits().collect { habits ->
                repository.getHabitLogsForDate(dateStr).collect { logs ->
                    val logMap = logs.associate { it.habitId to it.isCompleted }
                    val items = habits.map { habit ->
                        HabitItemUi(
                            habit = habit,
                            isCompleted = logMap[habit.id] ?: false,
                            streak = 0
                        )
                    }
                    _uiState.value = _uiState.value.copy(habitItems = items)
                }
            }
        }
    }

    fun toggleHabit(habitId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(_uiState.value.selectedDate, habitId, isCompleted)
        }
    }

    fun openAddHabit() {
        _uiState.value = _uiState.value.copy(editingHabit = null, showAddEditDialog = true)
    }

    fun openEditHabit(habit: HabitEntity) {
        _uiState.value = _uiState.value.copy(editingHabit = habit, showAddEditDialog = true)
    }

    fun closeDialog() {
        _uiState.value = _uiState.value.copy(editingHabit = null, showAddEditDialog = false)
    }

    fun saveHabit(title: String, category: HabitCategory, description: String, isPrivate: Boolean) {
        viewModelScope.launch {
            val current = _uiState.value.editingHabit
            val habit = (current ?: HabitEntity(title = title, category = category)).copy(
                title = title,
                category = category,
                description = description,
                isPrivate = isPrivate
            )
            repository.saveHabit(habit)
            closeDialog()
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            closeDialog()
        }
    }
}

