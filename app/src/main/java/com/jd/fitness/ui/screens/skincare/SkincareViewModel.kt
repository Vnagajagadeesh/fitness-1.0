package com.jd.fitness.ui.screens.skincare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.local.entity.SkincareLogEntity
import com.jd.fitness.data.local.entity.SkincareRoutineEntity
import com.jd.fitness.data.local.entity.SkincareRoutineType
import com.jd.fitness.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SkincareUiState(
    val date: String = "",
    val morningSteps: List<SkincareRoutineEntity> = emptyList(),
    val nightSteps: List<SkincareRoutineEntity> = emptyList(),
    val morningLog: SkincareLogEntity? = null,
    val nightLog: SkincareLogEntity? = null,
    val morningStreak: Int = 0,
    val nightStreak: Int = 0,
    val showEditStepDialog: Boolean = false,
    val editingStep: SkincareRoutineEntity? = null
)

class SkincareViewModel(
    private val repository: FitnessRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SkincareUiState())
    val uiState: StateFlow<SkincareUiState> = _uiState.asStateFlow()

    init {
        val today = repository.getTodayString()
        _uiState.value = _uiState.value.copy(date = today)
        loadRoutinesAndLogs(today)
    }

    private fun loadRoutinesAndLogs(dateStr: String) {
        viewModelScope.launch {
            repository.getSkincareRoutine(SkincareRoutineType.MORNING).collect { steps ->
                _uiState.value = _uiState.value.copy(morningSteps = steps)
            }
        }

        viewModelScope.launch {
            repository.getSkincareRoutine(SkincareRoutineType.NIGHT).collect { steps ->
                _uiState.value = _uiState.value.copy(nightSteps = steps)
            }
        }

        viewModelScope.launch {
            repository.getSkincareLog(dateStr, SkincareRoutineType.MORNING).collect { log ->
                _uiState.value = _uiState.value.copy(morningLog = log)
            }
        }

        viewModelScope.launch {
            repository.getSkincareLog(dateStr, SkincareRoutineType.NIGHT).collect { log ->
                _uiState.value = _uiState.value.copy(nightLog = log)
            }
        }
    }

    fun toggleRoutine(type: SkincareRoutineType, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleSkincareCompletion(_uiState.value.date, type, isCompleted)
        }
    }

    fun openAddStep(type: SkincareRoutineType) {
        val currentOrder = if (type == SkincareRoutineType.MORNING) _uiState.value.morningSteps.size + 1 else _uiState.value.nightSteps.size + 1
        val newStep = SkincareRoutineEntity(
            routineType = type,
            stepOrder = currentOrder,
            stepTitle = "",
            defaultProduct = ""
        )
        _uiState.value = _uiState.value.copy(editingStep = newStep, showEditStepDialog = true)
    }

    fun openEditStep(step: SkincareRoutineEntity) {
        _uiState.value = _uiState.value.copy(editingStep = step, showEditStepDialog = true)
    }

    fun closeDialog() {
        _uiState.value = _uiState.value.copy(editingStep = null, showEditStepDialog = false)
    }

    fun saveStep(title: String, product: String) {
        viewModelScope.launch {
            val step = _uiState.value.editingStep ?: return@launch
            repository.saveSkincareStep(step.copy(stepTitle = title, defaultProduct = product))
            closeDialog()
        }
    }

    fun deleteStep(step: SkincareRoutineEntity) {
        viewModelScope.launch {
            repository.deleteSkincareStep(step)
            closeDialog()
        }
    }
}

