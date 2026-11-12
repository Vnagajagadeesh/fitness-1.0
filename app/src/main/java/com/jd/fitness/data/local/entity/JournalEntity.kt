package com.jd.fitness.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class JournalMood {
    GREAT,
    GOOD,
    NEUTRAL,
    TIRED,
    STRESSED
}

@Serializable
@Entity(
    tableName = "journal_entries",
    indices = [Index(value = ["date"])]
)
data class JournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // yyyy-MM-dd
    val timestamp: Long = System.currentTimeMillis(),
    val title: String = "",
    val content: String = "",
    val mood: String = JournalMood.GOOD.name,
    val accomplishments: String = "",
    val challenges: String = "",
    val lessonsLearned: String = "",
    val tomorrowGoals: String = "",
    val allowAiInsights: Boolean = false // Strict privacy rule: default false
)

