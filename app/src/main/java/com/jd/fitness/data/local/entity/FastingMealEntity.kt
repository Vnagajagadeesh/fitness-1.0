package com.jd.fitness.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "fasting_meals",
    indices = [
        Index(value = ["mealTime"]),
        Index(value = ["sessionId"])
    ]
)
data class FastingMealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long? = null,
    val mealTime: Long = System.currentTimeMillis(),
    val mealTitle: String,
    val isOMADMeal: Boolean = false,
    val notes: String = ""
)

