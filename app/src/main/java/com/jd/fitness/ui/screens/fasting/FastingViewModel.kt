package com.jd.fitness.ui.screens.fasting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.local.entity.FastingEntity
import com.jd.fitness.data.local.entity.FastingMealEntity
import com.jd.fitness.data.local.entity.FastingState
import com.jd.fitness.data.preferences.UserPreferencesRepository
import com.jd.fitness.data.repository.FitnessRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class FastingUiState(
    val activeSession: FastingEntity? = null,
    val elapsedSeconds: Long = 0L,
    val targetHours: Int = 16,
    val isOMAD: Boolean = false,
    val pastSessions: List<FastingEntity> = emptyList(),
    val sessionMeals: List<FastingMealEntity> = emptyList(),
    val isUnderageNoticeAccepted: Boolean = false,
    val showMealDialog: Boolean = false
)

class FastingViewModel(
    private val repository: FitnessRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FastingUiState())
    val uiState: StateFlow<FastingUiState> = _uiState.asStateFlow()

    init {
        loadData()
        startLiveTimer()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getActiveFastingSession().collect { session ->
                _uiState.value = _uiState.value.copy(activeSession = session)
                if (session != null) {
                    val elapsed = (System.currentTimeMillis() - session.startTime) / 1000
                    _uiState.value = _uiState.value.copy(
                        elapsedSeconds = elapsed.coerceAtLeast(0),
                        targetHours = session.targetHours,
                        isOMAD = session.isOMAD
                    )
                    loadMealsForSession(session.id)
                }
            }
        }

        viewModelScope.launch {
            repository.getAllFastingSessions().collect { sessions ->
                _uiState.value = _uiState.value.copy(pastSessions = sessions.filter { it.state != FastingState.ACTIVE })
            }
        }

        viewModelScope.launch {
            val prefs = preferencesRepository.preferencesFlow.first()
            _uiState.value = _uiState.value.copy(
                targetHours = prefs.defaultFastingHours,
                isUnderageNoticeAccepted = prefs.isUnderageNoticeAccepted
            )
        }
    }

    private fun loadMealsForSession(sessionId: Long) {
        viewModelScope.launch {
            repository.getFastingMealsForSession(sessionId).collect { meals ->
                _uiState.value = _uiState.value.copy(sessionMeals = meals)
            }
        }
    }

    private fun startLiveTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val session = _uiState.value.activeSession
                if (session != null) {
                    val elapsed = (System.currentTimeMillis() - session.startTime) / 1000
                    _uiState.value = _uiState.value.copy(elapsedSeconds = elapsed.coerceAtLeast(0))
                }
            }
        }
    }

    fun setTargetHours(hours: Int) {
        _uiState.value = _uiState.value.copy(targetHours = hours)
    }

    fun setOMAD(isOMAD: Boolean) {
        _uiState.value = _uiState.value.copy(
            isOMAD = isOMAD,
            targetHours = if (isOMAD) 23 else 16
        )
    }

    fun startFasting() {
        viewModelScope.launch {
            repository.startFasting(
                targetHours = _uiState.value.targetHours,
                isOMAD = _uiState.value.isOMAD
            )
        }
    }

    fun endFasting() {
        viewModelScope.launch {
            val session = _uiState.value.activeSession ?: return@launch
            repository.endFasting(session)
        }
    }

    fun cancelFasting() {
        viewModelScope.launch {
            val session = _uiState.value.activeSession ?: return@launch
            repository.cancelFasting(session)
        }
    }

    fun openLogMeal() {
        _uiState.value = _uiState.value.copy(showMealDialog = true)
    }

    fun closeMealDialog() {
        _uiState.value = _uiState.value.copy(showMealDialog = false)
    }

    fun logMeal(title: String, isOMAD: Boolean, notes: String) {
        viewModelScope.launch {
            val sessionId = _uiState.value.activeSession?.id
            repository.logMeal(title, isOMAD, notes, sessionId)
            closeMealDialog()
        }
    }

    fun acceptUnderageNotice() {
        viewModelScope.launch {
            preferencesRepository.setUnderageNoticeAccepted(true)
            _uiState.value = _uiState.value.copy(isUnderageNoticeAccepted = true)
        }
    }
}

