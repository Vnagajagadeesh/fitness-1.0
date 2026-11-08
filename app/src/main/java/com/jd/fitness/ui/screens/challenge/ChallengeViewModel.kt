package com.jd.fitness.ui.screens.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.local.entity.ChallengeEntity
import com.jd.fitness.data.repository.ChallengeProgress
import com.jd.fitness.data.repository.FitnessRepository
import com.jd.fitness.data.repository.StreakInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChallengeUiState(
    val config: ChallengeEntity = ChallengeEntity(),
    val progress: ChallengeProgress? = null,
    val streakInfo: StreakInfo = StreakInfo(0, 0, 0, 0)
)

class ChallengeViewModel(
    private val repository: FitnessRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getChallengeConfig().collect { config ->
                config?.let { _uiState.value = _uiState.value.copy(config = it) }
            }
        }

        viewModelScope.launch {
            repository.getChallengeProgressFlow().collect { progress ->
                _uiState.value = _uiState.value.copy(progress = progress)
            }
        }

        viewModelScope.launch {
            val streaks = repository.calculateStreaks()
            _uiState.value = _uiState.value.copy(streakInfo = streaks)
        }
    }

    fun updateDates(startDate: String, endDate: String) {
        viewModelScope.launch {
            val current = _uiState.value.config
            repository.updateChallengeConfig(current.copy(startDate = startDate, endDate = endDate))
        }
    }
}

