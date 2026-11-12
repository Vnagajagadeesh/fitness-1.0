package com.jd.fitness.ui.screens.milestones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.local.entity.AchievementCategory
import com.jd.fitness.data.local.entity.AchievementEntity
import com.jd.fitness.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MilestonesUiState(
    val achievements: List<AchievementEntity> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = 0,
    val unlockPercentage: Float = 0f,
    val selectedCategory: String = "ALL",
    val newlyUnlocked: List<AchievementEntity> = emptyList()
)

class MilestonesViewModel(
    private val repository: FitnessRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MilestonesUiState())
    val uiState: StateFlow<MilestonesUiState> = _uiState.asStateFlow()

    init {
        loadAndEvaluate()
    }

    fun loadAndEvaluate() {
        viewModelScope.launch {
            val unlocked = repository.evaluateAndUnlockAchievements()
            _uiState.value = _uiState.value.copy(newlyUnlocked = unlocked)

            repository.getAllAchievements().collect { list ->
                val countUnlocked = list.count { it.isUnlocked }
                val total = list.size
                val pct = if (total > 0) (countUnlocked.toFloat() / total.toFloat()) * 100f else 0f
                _uiState.value = _uiState.value.copy(
                    achievements = list,
                    unlockedCount = countUnlocked,
                    totalCount = total,
                    unlockPercentage = pct
                )
            }
        }
    }

    fun setSelectedCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun dismissNewUnlockNotice() {
        _uiState.value = _uiState.value.copy(newlyUnlocked = emptyList())
    }
}

