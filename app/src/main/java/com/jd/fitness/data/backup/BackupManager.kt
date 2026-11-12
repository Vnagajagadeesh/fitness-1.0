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
            app = "FITNESS 1.2",
            exportVersion = 2,
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
            fastingSchedules = database.fastingDao().getAllSchedulesOnce(),
            stepLogs = database.stepDao().getAllSteps(),
            recoveryEntries = database.recoveryDao().getAllRecoveryEntriesOnce(),
            plannerTasks = database.plannerDao().getAllTasksOnce(),
            achievementRecords = database.achievementDao().getAllAchievementsOnce(),
            weeklyReports = database.weeklyReportDao().getAllReportsOnce(),
            journalEntries = database.journalDao().getAllEntriesOnce(),
            coachInsights = database.coachDao().getRecentInsights(30).first(),
            savedMotivation = database.motivationDao().getAllQuotesOnce()
        )
        json.encodeToString(backup)
    }

    /**
     * Safely restores a complete native backup JSON into the database.
     */
    suspend fun restoreNativeBackup(backupJson: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val backup = json.decodeFromString<FitnessExportBackup>(backupJson)
            val db = database
            db.runInTransaction {
                // Restore challenge
                backup.challenge?.let { db.openHelper.writableDatabase.execSQL("INSERT OR REPLACE INTO challenges (id, title, startDate, endDate, totalDays, milestone1Day, milestone1Title, milestone2Day, milestone2Title, milestone3Day, milestone3Title, brandInitials) VALUES (${it.id}, '${it.title}', '${it.startDate}', '${it.endDate}', ${it.totalDays}, ${it.milestone1Day}, '${it.milestone1Title}', ${it.milestone2Day}, '${it.milestone2Title}', ${it.milestone3Day}, '${it.milestone3Title}', '${it.brandInitials}')") }
            }
            if (backup.habits.isNotEmpty()) {
                db.habitDao().insertHabits(backup.habits)
            }
            if (backup.habitLogs.isNotEmpty()) {
                db.habitDao().insertOrUpdateLogs(backup.habitLogs)
            }
            if (backup.weightLogs.isNotEmpty()) {
                backup.weightLogs.forEach { db.weightDao().insertWeight(it) }
            }
            if (backup.skincareRoutines.isNotEmpty()) {
                db.skincareDao().insertRoutineSteps(backup.skincareRoutines)
            }
            if (backup.skincareLogs.isNotEmpty()) {
                backup.skincareLogs.forEach { db.skincareDao().insertOrUpdateLog(it) }
            }
            if (backup.fastingSessions.isNotEmpty()) {
                db.fastingDao().insertSessions(backup.fastingSessions)
            }
            if (backup.fastingMeals.isNotEmpty()) {
                db.fastingDao().insertMeals(backup.fastingMeals)
            }
            if (backup.fastingSchedules.isNotEmpty()) {
                db.fastingDao().insertSchedules(backup.fastingSchedules)
            }
            if (backup.stepLogs.isNotEmpty()) {
                backup.stepLogs.forEach { db.stepDao().insertOrUpdateStep(it) }
            }
            if (backup.recoveryEntries.isNotEmpty()) {
                db.recoveryDao().insertAll(backup.recoveryEntries)
            }
            if (backup.plannerTasks.isNotEmpty()) {
                db.plannerDao().insertTasks(backup.plannerTasks)
            }
            if (backup.achievementRecords.isNotEmpty()) {
                backup.achievementRecords.forEach { db.achievementDao().insertOrUpdate(it) }
            }
            if (backup.weeklyReports.isNotEmpty()) {
                db.weeklyReportDao().insertReports(backup.weeklyReports)
            }
            if (backup.journalEntries.isNotEmpty()) {
                db.journalDao().insertEntries(backup.journalEntries)
            }
            if (backup.coachInsights.isNotEmpty()) {
                db.coachDao().insertInsights(backup.coachInsights)
            }
            if (backup.savedMotivation.isNotEmpty()) {
                db.motivationDao().insertQuotes(backup.savedMotivation)
            }
            true
        }.getOrDefault(false)
    }

    /**
     * Resets all user logs with strong confirmation, preserving default habit schemas.
     */
    suspend fun resetAllData(): Unit = withContext(Dispatchers.IO) {
        val db = database
        db.runInTransaction {
            val sDb = db.openHelper.writableDatabase
            sDb.execSQL("DELETE FROM habit_logs")
            sDb.execSQL("DELETE FROM weight_logs")
            sDb.execSQL("DELETE FROM skincare_logs")
            sDb.execSQL("DELETE FROM fasting_sessions")
            sDb.execSQL("DELETE FROM fasting_meals")
            sDb.execSQL("DELETE FROM step_logs")
            sDb.execSQL("DELETE FROM recovery_entries")
            sDb.execSQL("DELETE FROM planner_tasks")
            sDb.execSQL("DELETE FROM weekly_reports")
            sDb.execSQL("DELETE FROM journal_entries")
            sDb.execSQL("DELETE FROM coach_insights")
            sDb.execSQL("UPDATE achievement_records SET isUnlocked = 0, unlockedAt = NULL, currentProgress = 0")
        }
    }
}
