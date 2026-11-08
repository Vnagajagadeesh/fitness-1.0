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
    val lastSyncTimestamp: Long = 0L
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
            lastSyncTimestamp = prefs[PreferencesKeys.LAST_SYNC_TIME] ?: 0L
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

