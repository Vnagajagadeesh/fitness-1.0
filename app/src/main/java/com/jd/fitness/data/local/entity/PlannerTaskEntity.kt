package com.jd.fitness.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class TaskCategory {
    MORNING_ROUTINE,
    COLLEGE,
    STUDY,
    FITNESS,
    SKINCARE,
    MEALS,
    PERSONAL,
    EVENING_ROUTINE,
    SLEEP
}

@Serializable
@Entity(
    tableName = "planner_tasks",
    indices = [Index(value = ["date"])]
)
data class PlannerTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // yyyy-MM-dd
    val title: String,
    val category: String = TaskCategory.PERSONAL.name,
    val startTime: String = "08:00", // HH:mm
    val endTime: String = "08:30", // HH:mm
    val isCompleted: Boolean = false,
    val priority: Int = 2, // 1: Low, 2: Medium, 3: High
    val orderIndex: Int = 0,
    val reminderEnabled: Boolean = false,
    val notes: String = ""
)

