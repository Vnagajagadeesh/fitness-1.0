package com.jd.fitness.ui.screens.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.local.entity.CoachInsightEntity
import com.jd.fitness.data.local.entity.CoachPersonalityState
import com.jd.fitness.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

data class CoachUiState(
    val currentInsight: CoachInsightEntity? = null,
    val personalityState: CoachPersonalityState = CoachPersonalityState.HIGH_PERFORMANCE,
    val guidanceText: String = "",
    val supportingDataSummary: String = "",
    val actionableGoals: List<String> = emptyList(),
    val isAiGenerated: Boolean = false,
    val allowJournalConsent: Boolean = false,
    val isRefreshing: Boolean = false,
    val historyInsights: List<CoachInsightEntity> = emptyList()
)

class CoachViewModel(
    private val repository: FitnessRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoachUiState())
    val uiState: StateFlow<CoachUiState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Check today's or generate new
            repository.getLatestCoachInsight().collect { insight ->
                if (insight != null) {
                    val pState = runCatching { CoachPersonalityState.valueOf(insight.personalityState) }
                        .getOrDefault(CoachPersonalityState.HIGH_PERFORMANCE)

                    val goals = runCatching {
                        json.parseToJsonElement(insight.actionableGoals).jsonArray.map { it.jsonPrimitive.content }
                    }.getOrDefault(emptyList())

                    _uiState.value = _uiState.value.copy(
                        currentInsight = insight,
                        personalityState = pState,
                        guidanceText = insight.guidanceText,
                        supportingDataSummary = insight.supportingDataSummary,
                        actionableGoals = goals,
                        isAiGenerated = insight.isAiGenerated
                    )
                } else {
                    refreshInsight()
                }
            }
        }

        viewModelScope.launch {
            repository.getRecentCoachInsights(14).collect { list ->
                _uiState.value = _uiState.value.copy(historyInsights = list)
            }
        }
    }

    fun refreshInsight() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            val insight = repository.generateAdaptiveCoachInsight(_uiState.value.allowJournalConsent)
            val pState = runCatching { CoachPersonalityState.valueOf(insight.personalityState) }
                .getOrDefault(CoachPersonalityState.HIGH_PERFORMANCE)

            val goals = runCatching {
                json.parseToJsonElement(insight.actionableGoals).jsonArray.map { it.jsonPrimitive.content }
            }.getOrDefault(emptyList())

            _uiState.value = _uiState.value.copy(
                currentInsight = insight,
                personalityState = pState,
                guidanceText = insight.guidanceText,
                supportingDataSummary = insight.supportingDataSummary,
                actionableGoals = goals,
                isAiGenerated = insight.isAiGenerated,
                isRefreshing = false
            )
        }
    }

    fun setJournalConsent(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(allowJournalConsent = enabled)
        refreshInsight()
    }
}
