package com.jd.fitness.ui.screens.motivation

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.local.entity.MotivationCategory
import com.jd.fitness.data.local.entity.MotivationEntity
import com.jd.fitness.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class MotivationUiState(
    val quotes: List<MotivationEntity> = emptyList(),
    val favoriteQuotes: List<MotivationEntity> = emptyList(),
    val todayQuote: MotivationEntity? = null,
    val selectedCategory: String = "ALL",
    val adaptiveEncouragement: String = "",
    val dailyDisciplineChallenge: String = "",
    val showAddQuoteDialog: Boolean = false
)

class MotivationViewModel(
    private val repository: FitnessRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MotivationUiState())
    val uiState: StateFlow<MotivationUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getAllMotivationQuotes().collect { list ->
                val favs = list.filter { it.isFavorite }
                val randomToday = list.firstOrNull()

                _uiState.value = _uiState.value.copy(
                    quotes = list,
                    favoriteQuotes = favs,
                    todayQuote = _uiState.value.todayQuote ?: randomToday
                )
            }
        }

        viewModelScope.launch {
            val streaks = repository.calculateStreaks()
            val today = repository.getTodayString()
            val daySummary = repository.getDaySummary(today).first()

            val encouragement = when {
                streaks.currentStreak >= 7 ->
                    "Incredible ${streaks.currentStreak}-day discipline streak. Your daily consistency is transforming your foundation into an impenetrable fortress."
                daySummary.percentage >= 80f ->
                    "Strong momentum today with ${daySummary.completedHabits} habits locked in. Finish the remaining routines with clarity."
                streaks.currentStreak == 0 ->
                    "Every master was once a beginner who refused to surrender. Today is Day 1 of your new unbroken streak. Step forward with clean focus."
                else ->
                    "Momentum is built one rep, one step, and one disciplined decision at a time. Stay focused on today's execution."
            }

            val challenges = listOf(
                "Complete today's 1-hour study session without checking your phone once.",
                "Hit 10,000 steps before 20:00 tonight.",
                "Hydrate with 3 full liters of clean water and zero added sugar.",
                "Perform 50 pushups or an intensive bodyweight mobility session today.",
                "Complete night skincare and log your reflections 30 minutes before sleep."
            )

            _uiState.value = _uiState.value.copy(
                adaptiveEncouragement = encouragement,
                dailyDisciplineChallenge = challenges.random()
            )
        }
    }

    fun setSelectedCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun toggleFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavoriteQuote(id, isFavorite)
        }
    }

    fun addCustomQuote(quote: String, category: MotivationCategory) {
        viewModelScope.launch {
            repository.saveMotivationQuote(
                MotivationEntity(
                    quote = quote,
                    category = category.name,
                    isFavorite = true
                )
            )
            _uiState.value = _uiState.value.copy(showAddQuoteDialog = false)
        }
    }

    fun setShowAddQuoteDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddQuoteDialog = show)
    }

    fun shareQuote(context: Context, quote: String, category: String) {
        val shareText = "⚡ \"$quote\"\n\n— FITNESS 1.2 Discipline & Mindset ($category)"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Motivation"))
    }
}

