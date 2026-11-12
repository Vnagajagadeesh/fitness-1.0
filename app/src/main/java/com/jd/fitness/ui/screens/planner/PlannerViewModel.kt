package com.jd.fitness.ui.screens.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.local.entity.PlannerTaskEntity
import com.jd.fitness.data.local.entity.TaskCategory
import com.jd.fitness.data.preferences.UserPreferencesRepository
import com.jd.fitness.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class PlannerUiState(
    val selectedDate: String = "",
    val tasks: List<PlannerTaskEntity> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val completionPercentage: Float = 0f,
    val showAddDialog: Boolean = false,
    val editingTask: PlannerTaskEntity? = null,
    val showSuggestDialog: Boolean = false
)

class PlannerViewModel(
    private val repository: FitnessRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlannerUiState())
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        val today = repository.getTodayString()
        _uiState.value = _uiState.value.copy(selectedDate = today)
        loadTasksForDate(today)
    }

    fun setSelectedDate(date: String) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadTasksForDate(date)
    }

    private fun loadTasksForDate(date: String) {
        viewModelScope.launch {
            repository.seedSuggestedPlannerIfEmpty(date)
            repository.getPlannerTasksForDate(date).collect { tasks ->
                val completed = tasks.count { it.isCompleted }
                val total = tasks.size
                val pct = if (total > 0) (completed.toFloat() / total.toFloat()) * 100f else 0f
                _uiState.value = _uiState.value.copy(
                    tasks = tasks,
                    completedCount = completed,
                    totalCount = total,
                    completionPercentage = pct
                )
            }
        }
    }

    fun toggleTask(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.togglePlannerTask(id, isCompleted)
            repository.evaluateAndUnlockAchievements()
        }
    }

    fun saveTask(
        title: String,
        category: TaskCategory,
        startTime: String,
        endTime: String,
        priority: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val state = _uiState.value
            val task = PlannerTaskEntity(
                id = state.editingTask?.id ?: 0,
                date = state.selectedDate,
                title = title,
                category = category.name,
                startTime = startTime,
                endTime = endTime,
                priority = priority,
                orderIndex = state.tasks.size + 1,
                notes = notes
            )
            repository.savePlannerTask(task)
            _uiState.value = _uiState.value.copy(showAddDialog = false, editingTask = null)
        }
    }

    fun deleteTask(task: PlannerTaskEntity) {
        viewModelScope.launch {
            repository.deletePlannerTask(task)
        }
    }

    fun setEditingTask(task: PlannerTaskEntity?) {
        _uiState.value = _uiState.value.copy(
            editingTask = task,
            showAddDialog = task != null
        )
    }

    fun setShowAddDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddDialog = show, editingTask = if (!show) null else _uiState.value.editingTask)
    }

    fun generateIntelligentSchedule() {
        viewModelScope.launch {
            val date = _uiState.value.selectedDate
            val prefs = preferencesRepository.preferencesFlow.first()

            val intelligentTasks = listOf(
                PlannerTaskEntity(date = date, title = "Morning Skincare & Clean Hydration", category = TaskCategory.MORNING_ROUTINE.name, startTime = prefs.morningSkincareReminder, endTime = "08:15", priority = 3, orderIndex = 1),
                PlannerTaskEntity(date = date, title = "Deep Focus Study Session (1h minimum)", category = TaskCategory.STUDY.name, startTime = prefs.studyReminder, endTime = "18:00", priority = 3, orderIndex = 2),
                PlannerTaskEntity(date = date, title = "Disciplined Home Workout & Active Movement", category = TaskCategory.FITNESS.name, startTime = prefs.workoutReminder, endTime = "19:30", priority = 3, orderIndex = 3),
                PlannerTaskEntity(date = date, title = "Wholesome Nourishing Meal (Zero Added Sugar)", category = TaskCategory.MEALS.name, startTime = "19:45", endTime = "20:30", priority = 2, orderIndex = 4),
                PlannerTaskEntity(date = date, title = "Night Skincare & Daily Review", category = TaskCategory.EVENING_ROUTINE.name, startTime = prefs.nightSkincareReminder, endTime = "22:00", priority = 3, orderIndex = 5),
                PlannerTaskEntity(date = date, title = "Sleep & Full Restoration", category = TaskCategory.SLEEP.name, startTime = "22:45", endTime = "06:45", priority = 3, orderIndex = 6)
            )

            // Remove existing uncompleted tasks and insert intelligent balanced schedule
            val existing = repository.getPlannerTasksForDate(date).first()
            existing.filter { !it.isCompleted }.forEach { repository.deletePlannerTask(it) }
            intelligentTasks.forEach { repository.savePlannerTask(it) }
        }
    }
}

