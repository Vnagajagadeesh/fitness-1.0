package com.jd.fitness.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.local.entity.JournalEntity
import com.jd.fitness.data.local.entity.JournalMood
import com.jd.fitness.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class JournalUiState(
    val entries: List<JournalEntity> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val showAddEditDialog: Boolean = false,
    val editingEntry: JournalEntity? = null,
    val datesWithEntries: Set<String> = emptySet()
)

class JournalViewModel(
    private val repository: FitnessRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    init {
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            repository.getAllJournalEntries().collect { list ->
                val dates = list.map { it.date }.toSet()
                _uiState.value = _uiState.value.copy(
                    entries = list,
                    datesWithEntries = dates
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, isSearching = query.isNotBlank())
        viewModelScope.launch {
            if (query.isBlank()) {
                repository.getAllJournalEntries().collect { list ->
                    _uiState.value = _uiState.value.copy(entries = list)
                }
            } else {
                repository.searchJournalEntries(query).collect { list ->
                    _uiState.value = _uiState.value.copy(entries = list)
                }
            }
        }
    }

    fun saveEntry(
        title: String,
        content: String,
        mood: JournalMood,
        accomplishments: String,
        challenges: String,
        lessons: String,
        tomorrowGoals: String,
        allowAi: Boolean
    ) {
        viewModelScope.launch {
            val state = _uiState.value
            val today = repository.getTodayString()
            val entry = JournalEntity(
                id = state.editingEntry?.id ?: 0,
                date = state.editingEntry?.date ?: today,
                timestamp = System.currentTimeMillis(),
                title = title,
                content = content,
                mood = mood.name,
                accomplishments = accomplishments,
                challenges = challenges,
                lessonsLearned = lessons,
                tomorrowGoals = tomorrowGoals,
                allowAiInsights = allowAi
            )
            repository.saveJournalEntry(entry)
            _uiState.value = _uiState.value.copy(showAddEditDialog = false, editingEntry = null)
        }
    }

    fun deleteEntry(entry: JournalEntity) {
        viewModelScope.launch {
            repository.deleteJournalEntry(entry)
        }
    }

    fun setEditingEntry(entry: JournalEntity?) {
        _uiState.value = _uiState.value.copy(
            editingEntry = entry,
            showAddEditDialog = entry != null
        )
    }

    fun setShowAddDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(
            showAddEditDialog = show,
            editingEntry = if (!show) null else _uiState.value.editingEntry
        )
    }
}

