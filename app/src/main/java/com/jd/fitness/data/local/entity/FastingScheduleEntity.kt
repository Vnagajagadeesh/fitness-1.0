package com.jd.fitness.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "fasting_schedules")
data class FastingScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val fastingHours: Int,
    val eatingHours: Int,
    val defaultStartTime: String = "20:00",
    val isDefault: Boolean = false
)

