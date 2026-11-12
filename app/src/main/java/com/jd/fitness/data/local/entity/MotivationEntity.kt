package com.jd.fitness.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class MotivationCategory {
    DISCIPLINE,
    CONSISTENCY,
    FITNESS,
    SELF_CONTROL,
    CONFIDENCE,
    PERSONAL_GROWTH,
    PRODUCTIVITY,
    MENTAL_STRENGTH
}

@Serializable
@Entity(tableName = "saved_motivation")
data class MotivationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quote: String,
    val category: String = MotivationCategory.DISCIPLINE.name,
    val isFavorite: Boolean = false,
    val savedAt: Long = System.currentTimeMillis()
)

