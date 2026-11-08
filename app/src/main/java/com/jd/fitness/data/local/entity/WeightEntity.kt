package com.jd.fitness.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "weight_logs",
    indices = [
        Index(value = ["date"]),
        Index(value = ["timestamp"])
    ]
)
data class WeightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(), // exact time
    val weightKg: Double,
    val notes: String = ""
)

