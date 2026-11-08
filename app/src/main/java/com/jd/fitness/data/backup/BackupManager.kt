package com.jd.fitness.data.backup

import com.jd.fitness.data.local.FitnessDatabase
import com.jd.fitness.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BackupManager(
    private val database: FitnessDatabase,
    private val preferencesRepository: UserPreferencesRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Exports full database records into a portable, standard JSON string.
     */
    suspend fun generateBackupJson(): String = withContext(Dispatchers.IO) {
        val prefs = preferencesRepository.preferencesFlow.first()
        val backup = FitnessExportBackup(
            app = "FITNESS 1.0",
            exportVersion = 1,
            exportedAt = System.currentTimeMillis(),
            userName = prefs.userName,
            challenge = database.challengeDao().getChallengeConfigOnce(),
            habits = database.habitDao().getAllHabitsOnce(),
            habitLogs = database.habitDao().getAllLogs(),
            weightLogs = database.weightDao().getAllWeightLogsOnce(),
            skincareRoutines = database.skincareDao().getAllRoutinesOnce(),
            skincareLogs = database.skincareDao().getAllLogs(),
            fastingSessions = database.fastingDao().getAllSessionsOnce(),
            fastingMeals = database.fastingDao().getAllMeals(),
            stepLogs = database.stepDao().getAllSteps()
        )
        json.encodeToString(backup)
    }

    /**
     * Resets all user logs with strong confirmation, preserving default habit schemas.
     */
    suspend fun resetAllData(): Unit = withContext(Dispatchers.IO) {
        // Clear all logs
        val db = database
        db.runInTransaction {
            val sDb = db.openHelper.writableDatabase
            sDb.execSQL("DELETE FROM habit_logs")
            sDb.execSQL("DELETE FROM weight_logs")
            sDb.execSQL("DELETE FROM skincare_logs")
            sDb.execSQL("DELETE FROM fasting_sessions")
            sDb.execSQL("DELETE FROM fasting_meals")
            sDb.execSQL("DELETE FROM step_logs")
        }
    }
}

