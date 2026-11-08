package com.jd.fitness.ui.screens.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.local.entity.WeightEntity
import com.jd.fitness.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class WeightUiState(
    val latestWeight: WeightEntity? = null,
    val allLogs: List<WeightEntity> = emptyList(),
    val allTimeAverage: Double? = null,
    val weeklyAverage: Double? = null,
    val monthlyAverage: Double? = null,
    val chartDataPoints: List<Pair<String, Double>> = emptyList(),
    val showAddDialog: Boolean = false,
    val editingWeight: WeightEntity? = null
)

class WeightViewModel(
    private val repository: FitnessRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightUiState())
    val uiState: StateFlow<WeightUiState> = _uiState.asStateFlow()

    init {
        loadWeightData()
    }

    private fun loadWeightData() {
        viewModelScope.launch {
            repository.getLatestWeight().collect { latest ->
                _uiState.value = _uiState.value.copy(latestWeight = latest)
            }
        }

        viewModelScope.launch {
            repository.getAllWeightLogs().collect { logs ->
                // Sort ascending for chart
                val ascending = logs.sortedBy { it.timestamp }
                val chartPoints = ascending.map { it.date to it.weightKg }

                // Calculate weekly & monthly averages
                val now = LocalDate.now()
                val weekAgo = now.minusDays(7).toString()
                val monthAgo = now.minusDays(30).toString()

                val weekLogs = logs.filter { it.date >= weekAgo }
                val monthLogs = logs.filter { it.date >= monthAgo }

                val weeklyAvg = if (weekLogs.isNotEmpty()) weekLogs.map { it.weightKg }.average() else null
                val monthlyAvg = if (monthLogs.isNotEmpty()) monthLogs.map { it.weightKg }.average() else null

                _uiState.value = _uiState.value.copy(
                    allLogs = logs,
                    chartDataPoints = chartPoints,
                    weeklyAverage = weeklyAvg,
                    monthlyAverage = monthlyAvg
                )
            }
        }

        viewModelScope.launch {
            repository.getAllTimeAverageWeight().collect { avg ->
                _uiState.value = _uiState.value.copy(allTimeAverage = avg)
            }
        }
    }

    fun openAddDialog() {
        _uiState.value = _uiState.value.copy(editingWeight = null, showAddDialog = true)
    }

    fun openEditDialog(weight: WeightEntity) {
        _uiState.value = _uiState.value.copy(editingWeight = weight, showAddDialog = true)
    }

    fun closeDialog() {
        _uiState.value = _uiState.value.copy(editingWeight = null, showAddDialog = false)
    }

    fun saveWeight(weightKg: Double, notes: String, date: String) {
        viewModelScope.launch {
            val editing = _uiState.value.editingWeight
            if (editing == null) {
                repository.insertWeight(kg = weightKg, notes = notes, date = date)
            } else {
                repository.updateWeight(editing.copy(weightKg = weightKg, notes = notes, date = date))
            }
            closeDialog()
        }
    }

    fun deleteWeight(weight: WeightEntity) {
        viewModelScope.launch {
            repository.deleteWeight(weight)
            closeDialog()
        }
    }
}

