package com.jd.fitness.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "weekly_reports",
    indices = [Index(value = ["weekStartDate"], unique = true)]
)
data class WeeklyReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekStartDate: String, // yyyy-MM-dd
    val weekEndDate: String, // yyyy-MM-dd
    val generatedAt: Long = System.currentTimeMillis(),
    val overallHabitPercentage: Float = 0f,
    val bestHabitTitle: String = "",
    val worstHabitTitle: String = "",
    val averageDailySteps: Int = 0,
    val totalWorkouts: Int = 0,
    val weightTrendKg: Double? = null,
    val skincareRate: Float = 0f,
    val fastingHoursTotal: Float = 0f,
    val restDaysCount: Int = 0,
    val achievementsUnlockedCount: Int = 0,
    val summaryJson: String = "{}"
)

