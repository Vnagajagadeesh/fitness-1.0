package com.jd.fitness.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class HabitCategory {
    PERSONAL_CARE,
    STUDY,
    FITNESS,
    NUTRITION,
    MINDSET,
    CUSTOM;

    fun getDisplayName(): String = when(this) {
        PERSONAL_CARE -> "Personal Care"
        STUDY -> "Study"
        FITNESS -> "Fitness"
        NUTRITION -> "Nutrition"
        MINDSET -> "Mindset"
        CUSTOM -> "Custom"
    }
}

@Serializable
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: HabitCategory,
    val description: String = "",
    val orderIndex: Int = 0,
    val isEnabled: Boolean = true,
    val isDefault: Boolean = false,
    val isSkincareSpecific: Boolean = false,
    val isPrivate: Boolean = false
)

