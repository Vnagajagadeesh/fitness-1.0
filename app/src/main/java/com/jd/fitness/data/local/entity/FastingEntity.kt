package com.jd.fitness.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class FastingState {
    ACTIVE,
    COMPLETED,
    CANCELLED
}

@Serializable
@Entity(tableName = "fasting_sessions")
data class FastingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long, // epoch millis
    val endTime: Long? = null, // epoch millis when completed/stopped
    val targetHours: Int = 16, // 16, 18, 20, 23 (OMAD) or custom
    val isOMAD: Boolean = false,
    val state: FastingState = FastingState.ACTIVE,
    val notes: String = ""
)

