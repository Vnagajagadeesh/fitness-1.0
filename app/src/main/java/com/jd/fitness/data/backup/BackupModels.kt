package com.jd.fitness.data.backup

import com.jd.fitness.data.local.entity.ChallengeEntity
import com.jd.fitness.data.local.entity.FastingEntity
import com.jd.fitness.data.local.entity.FastingMealEntity
import com.jd.fitness.data.local.entity.HabitEntity
import com.jd.fitness.data.local.entity.HabitLogEntity
import com.jd.fitness.data.local.entity.SkincareLogEntity
import com.jd.fitness.data.local.entity.SkincareRoutineEntity
import com.jd.fitness.data.local.entity.StepLogEntity
import com.jd.fitness.data.local.entity.WeightEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FitnessExportBackup(
    @SerialName("app") val app: String = "FITNESS 1.0",
    @SerialName("export_version") val exportVersion: Int = 1,
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
    @SerialName("step_logs") val stepLogs: List<StepLogEntity> = emptyList()
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

