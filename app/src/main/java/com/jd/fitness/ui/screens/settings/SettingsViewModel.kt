package com.jd.fitness.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.backup.BackupManager
import com.jd.fitness.data.backup.LegacyMigrationManager
import com.jd.fitness.data.backup.MigrationSummary
import com.jd.fitness.data.local.entity.ChallengeEntity
import com.jd.fitness.data.preferences.AppUserPreferences
import com.jd.fitness.data.preferences.UserPreferencesRepository
import com.jd.fitness.data.repository.FitnessRepository
import com.jd.fitness.data.sync.SupabaseManager
import com.jd.fitness.data.sync.SyncResult
import com.jd.fitness.notification.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preferences: AppUserPreferences = AppUserPreferences(),
    val challengeConfig: ChallengeEntity = ChallengeEntity(),
    val syncResult: SyncResult = SyncResult.Idle,
    val exportedJson: String? = null,
    val showExportDialog: Boolean = false,
    val showImportDialog: Boolean = false,
    val migrationSummary: MigrationSummary? = null,
    val rawJsonToImport: String = "",
    val showResetConfirm: Boolean = false,
    val statusMessage: String? = null
)

class SettingsViewModel(
    private val repository: FitnessRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val supabaseManager: SupabaseManager,
    private val backupManager: BackupManager,
    private val legacyMigrationManager: LegacyMigrationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(preferences = prefs)
            }
        }

        viewModelScope.launch {
            repository.getChallengeConfig().collect { config ->
                config?.let { _uiState.value = _uiState.value.copy(challengeConfig = it) }
            }
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            preferencesRepository.updateUserName(name)
        }
    }

    fun updateChallengeDates(startDate: String, endDate: String) {
        viewModelScope.launch {
            val current = _uiState.value.challengeConfig
            repository.updateChallengeConfig(current.copy(startDate = startDate, endDate = endDate))
            _uiState.value = _uiState.value.copy(statusMessage = "Challenge dates updated.")
        }
    }

    fun updateStepGoal(goal: Int) {
        viewModelScope.launch {
            preferencesRepository.updateStepGoal(goal)
        }
    }

    fun toggleNotifications(enabled: Boolean, context: Context) {
        viewModelScope.launch {
            preferencesRepository.setNotificationsEnabled(enabled)
            if (enabled) {
                NotificationScheduler.rescheduleAll(context)
            } else {
                NotificationScheduler.cancelReminder(context, NotificationScheduler.ID_MORNING_SKINCARE)
                NotificationScheduler.cancelReminder(context, NotificationScheduler.ID_NIGHT_SKINCARE)
                NotificationScheduler.cancelReminder(context, NotificationScheduler.ID_STUDY)
                NotificationScheduler.cancelReminder(context, NotificationScheduler.ID_WORKOUT)
                NotificationScheduler.cancelReminder(context, NotificationScheduler.ID_DAILY_REVIEW)
                NotificationScheduler.cancelReminder(context, NotificationScheduler.ID_WEIGHT)
                NotificationScheduler.cancelReminder(context, NotificationScheduler.ID_STEP_GOAL)
            }
        }
    }

    fun updateReminderTime(key: String, timeHHmm: String, context: Context) {
        viewModelScope.launch {
            preferencesRepository.updateReminderTime(key, timeHHmm)
            NotificationScheduler.rescheduleAll(context)
        }
    }

    fun toggleNotificationSetting(key: String, enabled: Boolean, context: Context) {
        viewModelScope.launch {
            preferencesRepository.toggleNotificationSetting(key, enabled)
            NotificationScheduler.rescheduleAll(context)
        }
    }

    fun saveSupabaseConfig(url: String, anonKey: String) {
        viewModelScope.launch {
            preferencesRepository.updateSupabaseConfig(url, anonKey)
            _uiState.value = _uiState.value.copy(statusMessage = "Supabase configuration saved.")
        }
    }

    fun syncBackupToCloud() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(syncResult = SyncResult.Syncing)
            val result = supabaseManager.backupToCloud()
            _uiState.value = _uiState.value.copy(syncResult = result)
        }
    }

    fun syncRestoreFromCloud() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(syncResult = SyncResult.Syncing)
            val result = supabaseManager.restoreFromCloud()
            _uiState.value = _uiState.value.copy(syncResult = result)
        }
    }

    fun exportBackup() {
        viewModelScope.launch {
            val json = backupManager.generateBackupJson()
            _uiState.value = _uiState.value.copy(exportedJson = json, showExportDialog = true)
        }
    }

    fun closeExportDialog() {
        _uiState.value = _uiState.value.copy(showExportDialog = false)
    }

    fun openImportDialog() {
        _uiState.value = _uiState.value.copy(
            showImportDialog = true,
            migrationSummary = null,
            rawJsonToImport = ""
        )
    }

    fun closeImportDialog() {
        _uiState.value = _uiState.value.copy(showImportDialog = false, migrationSummary = null)
    }

    fun analyzeImportJson(rawJson: String) {
        val summaryResult = legacyMigrationManager.analyzeBackupJson(rawJson)
        summaryResult.onSuccess { summary ->
            _uiState.value = _uiState.value.copy(
                migrationSummary = summary,
                rawJsonToImport = rawJson
            )
        }.onFailure { err ->
            _uiState.value = _uiState.value.copy(statusMessage = "Invalid JSON backup: ${err.localizedMessage}")
        }
    }

    fun executeImport(rawJson: String, splitLegacySkincare: Boolean) {
        viewModelScope.launch {
            val res = legacyMigrationManager.executeMigration(rawJson, splitLegacySkincare)
            res.onSuccess { msg ->
                _uiState.value = _uiState.value.copy(
                    statusMessage = msg,
                    showImportDialog = false,
                    migrationSummary = null
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(statusMessage = "Migration failed: ${err.localizedMessage}")
            }
        }
    }

    fun promptResetData() {
        _uiState.value = _uiState.value.copy(showResetConfirm = true)
    }

    fun cancelResetData() {
        _uiState.value = _uiState.value.copy(showResetConfirm = false)
    }

    fun confirmResetData() {
        viewModelScope.launch {
            backupManager.resetAllData()
            _uiState.value = _uiState.value.copy(
                showResetConfirm = false,
                statusMessage = "All historical data reset completed."
            )
        }
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }
}

