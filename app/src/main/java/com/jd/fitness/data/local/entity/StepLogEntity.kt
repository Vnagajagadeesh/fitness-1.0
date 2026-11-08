package com.jd.fitness.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "step_logs",
    indices = [Index(value = ["date"], unique = true)]
)
data class StepLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val stepCount: Int,
    val source: String = "HEALTH_CONNECT", // HEALTH_CONNECT or MANUAL
    val lastUpdated: Long = System.currentTimeMillis()
)

