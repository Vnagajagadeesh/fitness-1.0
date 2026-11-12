package com.jd.fitness.data.sync

import com.jd.fitness.data.local.entity.AchievementEntity
import com.jd.fitness.data.local.entity.ChallengeEntity
import com.jd.fitness.data.local.entity.CoachInsightEntity
import com.jd.fitness.data.local.entity.FastingEntity
import com.jd.fitness.data.local.entity.FastingMealEntity
import com.jd.fitness.data.local.entity.FastingScheduleEntity
import com.jd.fitness.data.local.entity.HabitEntity
import com.jd.fitness.data.local.entity.HabitLogEntity
import com.jd.fitness.data.local.entity.JournalEntity
import com.jd.fitness.data.local.entity.MotivationEntity
import com.jd.fitness.data.local.entity.PlannerTaskEntity
import com.jd.fitness.data.local.entity.RecoveryEntity
import com.jd.fitness.data.local.entity.SkincareLogEntity
import com.jd.fitness.data.local.entity.SkincareRoutineEntity
import com.jd.fitness.data.local.entity.StepLogEntity
import com.jd.fitness.data.local.entity.WeeklyReportEntity
import com.jd.fitness.data.local.entity.WeightEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncPayload(
    @SerialName("version") val version: Int = 2,
    @SerialName("user_id") val userId: String = "JD",
    @SerialName("device_info") val deviceInfo: String = "POCO X7 Pro - FITNESS 1.2",
    @SerialName("updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @SerialName("challenge") val challenge: ChallengeEntity? = null,
    @SerialName("habits") val habits: List<HabitEntity> = emptyList(),
    @SerialName("habit_logs") val habitLogs: List<HabitLogEntity> = emptyList(),
    @SerialName("weight_logs") val weightLogs: List<WeightEntity> = emptyList(),
    @SerialName("skincare_routines") val skincareRoutines: List<SkincareRoutineEntity> = emptyList(),
    @SerialName("skincare_logs") val skincareLogs: List<SkincareLogEntity> = emptyList(),
    @SerialName("fasting_sessions") val fastingSessions: List<FastingEntity> = emptyList(),
    @SerialName("fasting_meals") val fastingMeals: List<FastingMealEntity> = emptyList(),
    @SerialName("fasting_schedules") val fastingSchedules: List<FastingScheduleEntity> = emptyList(),
    @SerialName("step_logs") val stepLogs: List<StepLogEntity> = emptyList(),
    @SerialName("recovery_entries") val recoveryEntries: List<RecoveryEntity> = emptyList(),
    @SerialName("planner_tasks") val plannerTasks: List<PlannerTaskEntity> = emptyList(),
    @SerialName("achievement_records") val achievementRecords: List<AchievementEntity> = emptyList(),
    @SerialName("weekly_reports") val weeklyReports: List<WeeklyReportEntity> = emptyList(),
    @SerialName("journal_entries") val journalEntries: List<JournalEntity> = emptyList(),
    @SerialName("coach_insights") val coachInsights: List<CoachInsightEntity> = emptyList(),
    @SerialName("saved_motivation") val savedMotivation: List<MotivationEntity> = emptyList()
)

@Serializable
data class Season10CloudRow(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("data") val data: String, // JSON string of SyncPayload or legacy progress
    @SerialName("updated_at") val updatedAt: String? = null
)

sealed class SyncResult {
    data class Success(val message: String, val syncedAt: Long) : SyncResult()
    data class Error(val errorMessage: String) : SyncResult()
    object Idle : SyncResult()
    object Syncing : SyncResult()
}
