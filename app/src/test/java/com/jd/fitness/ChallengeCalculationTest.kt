package com.jd.fitness

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ChallengeCalculationTest {

    @Test
    fun testChallenge90DaysCalculation() {
        val start = LocalDate.parse("2026-11-03")
        val end = LocalDate.parse("2027-01-31")

        val totalDays = ChronoUnit.DAYS.between(start, end) + 1
        assertEquals(90L, totalDays)
    }

    @Test
    fun testMilestoneCalculation() {
        fun getMilestone(day: Int): String = when {
            day >= 90 -> "TRANSFORMATION"
            day >= 60 -> "MOMENTUM"
            day >= 30 -> "FOUNDATION"
            else -> "KICKOFF"
        }

        assertEquals("KICKOFF", getMilestone(1))
        assertEquals("FOUNDATION", getMilestone(30))
        assertEquals("FOUNDATION", getMilestone(45))
        assertEquals("MOMENTUM", getMilestone(60))
        assertEquals("MOMENTUM", getMilestone(75))
        assertEquals("TRANSFORMATION", getMilestone(90))
    }

    @Test
    fun testPercentageCalculation() {
        val currentDay = 45
        val totalDays = 90
        val percentage = (currentDay.toFloat() / totalDays.toFloat()) * 100f
        assertEquals(50f, percentage, 0.01f)
    }
}

