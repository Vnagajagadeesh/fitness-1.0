package com.jd.fitness.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class CoachPersonalityState {
    HIGH_PERFORMANCE,
    STRUGGLING,
    INCONSISTENT,
    RECOVERY_NEEDED
}

@Serializable
@Entity(
    tableName = "coach_insights",
    indices = [Index(value = ["date"])]
)
data class CoachInsightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // yyyy-MM-dd
    val timestamp: Long = System.currentTimeMillis(),
    val personalityState: String = CoachPersonalityState.HIGH_PERFORMANCE.name,
    val guidanceText: String,
    val supportingDataSummary: String,
    val isAiGenerated: Boolean = false,
    val actionableGoals: String = "[]" // JSON string list of 1-3 actionable targets
)

