package com.jd.fitness.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class SkincareRoutineType {
    MORNING,
    NIGHT
}

@Serializable
@Entity(tableName = "skincare_routines")
data class SkincareRoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineType: SkincareRoutineType,
    val stepOrder: Int,
    val stepTitle: String,
    val defaultProduct: String = "",
    val isEnabled: Boolean = true
)

