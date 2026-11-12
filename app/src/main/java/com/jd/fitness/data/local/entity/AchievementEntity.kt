package com.jd.fitness.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class AchievementCategory {
    HABIT,
    FITNESS,
    PRODUCTIVITY
}

@Serializable
@Entity(tableName = "achievement_records")
data class AchievementEntity(
    @PrimaryKey val id: String, // e.g. "habit_first_day", "habit_7d", etc.
    val category: String = AchievementCategory.HABIT.name,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val currentProgress: Int = 0,
    val maxProgress: Int = 1
)

