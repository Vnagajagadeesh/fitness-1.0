package com.jd.fitness.data.backup

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
data class FitnessExportBackup(
    @SerialName("app") val app: String = "FITNESS 1.2",
    @SerialName("export_version") val exportVersion: Int = 2,
    @SerialName("exported_at") val exportedAt: Long = System.currentTimeMillis(),
    @SerialName("user_name") val userName: String = "JD",
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

data class MigrationSummary(
    val isLegacyFormat: Boolean,
    val detectedSource: String,
    val totalHabitsCount: Int,
    val totalLogsCount: Int,
    val totalWeightEntries: Int,
    val challengeStartDate: String?,
    val challengeEndDate: String?,
    val detectedUserName: String?,
    val legacySkincareDaysCount: Int,
    val notices: List<String>
)
