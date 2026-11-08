package com.jd.fitness.data.sync

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

@Serializable
data class SyncPayload(
    @SerialName("version") val version: Int = 1,
    @SerialName("user_id") val userId: String = "JD",
    @SerialName("device_info") val deviceInfo: String = "POCO X7 Pro",
    @SerialName("updated_at") val updatedAt: Long = System.currentTimeMillis(),
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

