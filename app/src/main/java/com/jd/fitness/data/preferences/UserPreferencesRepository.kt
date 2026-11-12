package com.jd.fitness.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class AppUserPreferences(
    val userName: String = "JD",
    val stepGoal: Int = 10000,
    val isNotificationsEnabled: Boolean = true,
    val morningSkincareReminder: String = "08:00",
    val nightSkincareReminder: String = "21:30",
    val studyReminder: String = "17:00",
    val workoutReminder: String = "18:30",
    val dailyReviewReminder: String = "22:00",
    val weightReminder: String = "07:30",
    val stepGoalReminder: String = "20:00",
    val defaultFastingHours: Int = 16,
    val isUnderageNoticeAccepted: Boolean = false,
    val supabaseUrl: String = "",
    val supabaseAnonKey: String = "",
    val lastSyncTimestamp: Long = 0L,

    // Centralized Smart Notification Intelligence preferences
    val isMorningMotivationEnabled: Boolean = true,
    val morningMotivationTime: String = "07:00",
    val isAfternoonMotivationEnabled: Boolean = false,
    val afternoonMotivationTime: String = "13:30",
    val isEveningMotivationEnabled: Boolean = true,
    val eveningMotivationTime: String = "19:00",
    val isQuietHoursEnabled: Boolean = true,
    val quietHoursStart: String = "23:00",
    val quietHoursEnd: String = "06:30",
    val isWeeklyReportNotificationEnabled: Boolean = true,
    val isJournalReminderEnabled: Boolean = true,
    val journalReminderTime: String = "21:45",
    val isRecoveryReminderEnabled: Boolean = true,
    val recoveryReminderTime: String = "22:15"
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_display_name")
        val STEP_GOAL = intPreferencesKey("step_goal")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val REMINDER_MORNING_SKINCARE = stringPreferencesKey("reminder_morning_skincare")
        val REMINDER_NIGHT_SKINCARE = stringPreferencesKey("reminder_night_skincare")
        val REMINDER_STUDY = stringPreferencesKey("reminder_study")
        val REMINDER_WORKOUT = stringPreferencesKey("reminder_workout")
        val REMINDER_DAILY_REVIEW = stringPreferencesKey("reminder_daily_review")
        val REMINDER_WEIGHT = stringPreferencesKey("reminder_weight")
        val REMINDER_STEP_GOAL = stringPreferencesKey("reminder_step_goal")
        val FASTING_HOURS = intPreferencesKey("fasting_hours")
        val UNDERAGE_NOTICE = booleanPreferencesKey("underage_notice")
        val SUPABASE_URL = stringPreferencesKey("supabase_url")
        val SUPABASE_ANON_KEY = stringPreferencesKey("supabase_anon_key")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")

        val MORNING_MOTIVATION_ENABLED = booleanPreferencesKey("morning_motivation_enabled")
        val MORNING_MOTIVATION_TIME = stringPreferencesKey("morning_motivation_time")
        val AFTERNOON_MOTIVATION_ENABLED = booleanPreferencesKey("afternoon_motivation_enabled")
        val AFTERNOON_MOTIVATION_TIME = stringPreferencesKey("afternoon_motivation_time")
        val EVENING_MOTIVATION_ENABLED = booleanPreferencesKey("evening_motivation_enabled")
        val EVENING_MOTIVATION_TIME = stringPreferencesKey("evening_motivation_time")
        val QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        val QUIET_HOURS_START = stringPreferencesKey("quiet_hours_start")
        val QUIET_HOURS_END = stringPreferencesKey("quiet_hours_end")
        val WEEKLY_REPORT_ENABLED = booleanPreferencesKey("weekly_report_enabled")
        val JOURNAL_REMINDER_ENABLED = booleanPreferencesKey("journal_reminder_enabled")
        val JOURNAL_REMINDER_TIME = stringPreferencesKey("journal_reminder_time")
        val RECOVERY_REMINDER_ENABLED = booleanPreferencesKey("recovery_reminder_enabled")
        val RECOVERY_REMINDER_TIME = stringPreferencesKey("recovery_reminder_time")
    }

    val preferencesFlow: Flow<AppUserPreferences> = context.dataStore.data.map { prefs ->
        AppUserPreferences(
            userName = prefs[PreferencesKeys.USER_NAME] ?: "JD",
            stepGoal = prefs[PreferencesKeys.STEP_GOAL] ?: 10000,
            isNotificationsEnabled = prefs[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            morningSkincareReminder = prefs[PreferencesKeys.REMINDER_MORNING_SKINCARE] ?: "08:00",
            nightSkincareReminder = prefs[PreferencesKeys.REMINDER_NIGHT_SKINCARE] ?: "21:30",
            studyReminder = prefs[PreferencesKeys.REMINDER_STUDY] ?: "17:00",
            workoutReminder = prefs[PreferencesKeys.REMINDER_WORKOUT] ?: "18:30",
            dailyReviewReminder = prefs[PreferencesKeys.REMINDER_DAILY_REVIEW] ?: "22:00",
            weightReminder = prefs[PreferencesKeys.REMINDER_WEIGHT] ?: "07:30",
            stepGoalReminder = prefs[PreferencesKeys.REMINDER_STEP_GOAL] ?: "20:00",
            defaultFastingHours = prefs[PreferencesKeys.FASTING_HOURS] ?: 16,
            isUnderageNoticeAccepted = prefs[PreferencesKeys.UNDERAGE_NOTICE] ?: false,
            supabaseUrl = prefs[PreferencesKeys.SUPABASE_URL] ?: "",
            supabaseAnonKey = prefs[PreferencesKeys.SUPABASE_ANON_KEY] ?: "",
            lastSyncTimestamp = prefs[PreferencesKeys.LAST_SYNC_TIME] ?: 0L,

            isMorningMotivationEnabled = prefs[PreferencesKeys.MORNING_MOTIVATION_ENABLED] ?: true,
            morningMotivationTime = prefs[PreferencesKeys.MORNING_MOTIVATION_TIME] ?: "07:00",
            isAfternoonMotivationEnabled = prefs[PreferencesKeys.AFTERNOON_MOTIVATION_ENABLED] ?: false,
            afternoonMotivationTime = prefs[PreferencesKeys.AFTERNOON_MOTIVATION_TIME] ?: "13:30",
            isEveningMotivationEnabled = prefs[PreferencesKeys.EVENING_MOTIVATION_ENABLED] ?: true,
            eveningMotivationTime = prefs[PreferencesKeys.EVENING_MOTIVATION_TIME] ?: "19:00",
            isQuietHoursEnabled = prefs[PreferencesKeys.QUIET_HOURS_ENABLED] ?: true,
            quietHoursStart = prefs[PreferencesKeys.QUIET_HOURS_START] ?: "23:00",
            quietHoursEnd = prefs[PreferencesKeys.QUIET_HOURS_END] ?: "06:30",
            isWeeklyReportNotificationEnabled = prefs[PreferencesKeys.WEEKLY_REPORT_ENABLED] ?: true,
            isJournalReminderEnabled = prefs[PreferencesKeys.JOURNAL_REMINDER_ENABLED] ?: true,
            journalReminderTime = prefs[PreferencesKeys.JOURNAL_REMINDER_TIME] ?: "21:45",
            isRecoveryReminderEnabled = prefs[PreferencesKeys.RECOVERY_REMINDER_ENABLED] ?: true,
            recoveryReminderTime = prefs[PreferencesKeys.RECOVERY_REMINDER_TIME] ?: "22:15"
        )
    }

    suspend fun updateUserName(name: String) {
        context.dataStore.edit { it[PreferencesKeys.USER_NAME] = name }
    }

    suspend fun updateStepGoal(goal: Int) {
        context.dataStore.edit { it[PreferencesKeys.STEP_GOAL] = goal }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun updateReminderTime(keyName: String, time: String) {
        context.dataStore.edit { prefs ->
            when (keyName) {
                "morning_skincare" -> prefs[PreferencesKeys.REMINDER_MORNING_SKINCARE] = time
                "night_skincare" -> prefs[PreferencesKeys.REMINDER_NIGHT_SKINCARE] = time
                "study" -> prefs[PreferencesKeys.REMINDER_STUDY] = time
                "workout" -> prefs[PreferencesKeys.REMINDER_WORKOUT] = time
                "daily_review" -> prefs[PreferencesKeys.REMINDER_DAILY_REVIEW] = time
                "weight" -> prefs[PreferencesKeys.REMINDER_WEIGHT] = time
                "step_goal" -> prefs[PreferencesKeys.REMINDER_STEP_GOAL] = time
                "morning_motivation" -> prefs[PreferencesKeys.MORNING_MOTIVATION_TIME] = time
                "afternoon_motivation" -> prefs[PreferencesKeys.AFTERNOON_MOTIVATION_TIME] = time
                "evening_motivation" -> prefs[PreferencesKeys.EVENING_MOTIVATION_TIME] = time
                "journal_reminder" -> prefs[PreferencesKeys.JOURNAL_REMINDER_TIME] = time
                "recovery_reminder" -> prefs[PreferencesKeys.RECOVERY_REMINDER_TIME] = time
                "quiet_start" -> prefs[PreferencesKeys.QUIET_HOURS_START] = time
                "quiet_end" -> prefs[PreferencesKeys.QUIET_HOURS_END] = time
            }
        }
    }

    suspend fun toggleNotificationSetting(keyName: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            when (keyName) {
                "morning_motivation" -> prefs[PreferencesKeys.MORNING_MOTIVATION_ENABLED] = enabled
                "afternoon_motivation" -> prefs[PreferencesKeys.AFTERNOON_MOTIVATION_ENABLED] = enabled
                "evening_motivation" -> prefs[PreferencesKeys.EVENING_MOTIVATION_ENABLED] = enabled
                "quiet_hours" -> prefs[PreferencesKeys.QUIET_HOURS_ENABLED] = enabled
                "weekly_report" -> prefs[PreferencesKeys.WEEKLY_REPORT_ENABLED] = enabled
                "journal" -> prefs[PreferencesKeys.JOURNAL_REMINDER_ENABLED] = enabled
                "recovery" -> prefs[PreferencesKeys.RECOVERY_REMINDER_ENABLED] = enabled
            }
        }
    }

    suspend fun updateFastingHours(hours: Int) {
        context.dataStore.edit { it[PreferencesKeys.FASTING_HOURS] = hours }
    }

    suspend fun setUnderageNoticeAccepted(accepted: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.UNDERAGE_NOTICE] = accepted }
    }

    suspend fun updateSupabaseConfig(url: String, anonKey: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SUPABASE_URL] = url
            prefs[PreferencesKeys.SUPABASE_ANON_KEY] = anonKey
        }
    }

    suspend fun updateLastSyncTime(timestamp: Long) {
        context.dataStore.edit { it[PreferencesKeys.LAST_SYNC_TIME] = timestamp }
    }
}
