package com.jd.fitness.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class PerceivedEnergy {
    WELL_RESTED,
    NORMAL,
    TIRED,
    NEED_REST
}

@Serializable
@Entity(
    tableName = "recovery_entries",
    indices = [Index(value = ["date"], unique = true)]
)
data class RecoveryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // yyyy-MM-dd
    val isRestDay: Boolean = false,
    val sleepHours: Float = 7.0f,
    val perceivedEnergy: String = PerceivedEnergy.NORMAL.name,
    val workoutDone: Boolean = false,
    val workoutType: String = "REST", // Rest, Strength, Cardio, Walk, Stretch
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

