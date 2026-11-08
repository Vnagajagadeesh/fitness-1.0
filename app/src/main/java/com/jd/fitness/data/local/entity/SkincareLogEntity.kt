package com.jd.fitness.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "skincare_logs",
    indices = [
        Index(value = ["date", "routineType"], unique = true),
        Index(value = ["date"])
    ]
)
data class SkincareLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val routineType: SkincareRoutineType,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val completedStepsJson: String = "[]", // List of step titles completed
    val notes: String = ""
)

