package com.jd.fitness.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "challenge_config")
data class ChallengeEntity(
    @PrimaryKey val id: Int = 1,
    val title: String = "FITNESS 1.0 — THE BEGINNING",
    val startDate: String = "2026-11-03", // Original challenge start date
    val endDate: String = "2027-01-31",   // Original challenge end date
    val totalDays: Int = 90,
    val milestone1Day: Int = 30, // Day 30 — FOUNDATION
    val milestone1Title: String = "FOUNDATION",
    val milestone2Day: Int = 60, // Day 60 — MOMENTUM
    val milestone2Title: String = "MOMENTUM",
    val milestone3Day: Int = 90, // Day 90 — TRANSFORMATION
    val milestone3Title: String = "TRANSFORMATION",
    val brandInitials: String = "JD"
)

