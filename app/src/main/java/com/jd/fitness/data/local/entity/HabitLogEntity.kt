package com.jd.fitness.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "habit_logs",
    indices = [
        Index(value = ["date", "habitId"], unique = true),
        Index(value = ["date"])
    ]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val habitId: Long,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val notes: String = ""
)

