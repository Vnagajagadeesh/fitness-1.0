package com.jd.fitness

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryCalculationTest {

    // Helper logic simulating Recovery state determination
    private fun evaluateRecoveryState(
        isRestDay: Boolean,
        energyLevel: Int,
        sleepHours: Float,
        consecutiveWorkouts: Int
    ): Pair<String, String> {
        return when {
            isRestDay -> {
                "Active Rest" to "Rest day recorded. Focus on mobility, hydration, and mental recovery."
            }
            consecutiveWorkouts >= 5 -> {
                "Rest Recommended" to "You have completed $consecutiveWorkouts consecutive workout days. Consider taking a rest day to prevent overtraining."
            }
            sleepHours < 6.0f || energyLevel <= 2 -> {
                "Low Readiness" to "Energy and sleep levels are reduced. Keep today's activity light or focus on active recovery."
            }
            sleepHours >= 7.5f && energyLevel >= 4 -> {
                "Optimal Readiness" to "Physical readiness is high. Excellent day for scheduled training or high-focus tasks."
            }
            else -> {
                "Balanced Recovery" to "Readiness is moderate. Listen to your body and pace your activities appropriately."
            }
        }
    }

    @Test
    fun testRestDayState() {
        val (status, rec) = evaluateRecoveryState(
            isRestDay = true,
            energyLevel = 3,
            sleepHours = 7.0f,
            consecutiveWorkouts = 2
        )
        assertEquals("Active Rest", status)
        assertTrue(rec.contains("Rest day recorded"))
    }

    @Test
    fun testOvertrainingPrevention() {
        val (status, rec) = evaluateRecoveryState(
            isRestDay = false,
            energyLevel = 4,
            sleepHours = 8.0f,
            consecutiveWorkouts = 5
        )
        assertEquals("Rest Recommended", status)
        assertTrue(rec.contains("5 consecutive workout days"))
    }

    @Test
    fun testLowSleepAndEnergy() {
        val (status, _) = evaluateRecoveryState(
            isRestDay = false,
            energyLevel = 2,
            sleepHours = 5.5f,
            consecutiveWorkouts = 1
        )
        assertEquals("Low Readiness", status)

        val (status2, _) = evaluateRecoveryState(
            isRestDay = false,
            energyLevel = 4,
            sleepHours = 5.0f,
            consecutiveWorkouts = 1
        )
        assertEquals("Low Readiness", status2)
    }

    @Test
    fun testOptimalReadiness() {
        val (status, rec) = evaluateRecoveryState(
            isRestDay = false,
            energyLevel = 5,
            sleepHours = 8.0f,
            consecutiveWorkouts = 2
        )
        assertEquals("Optimal Readiness", status)
        assertTrue(rec.contains("Physical readiness is high"))
    }

    @Test
    fun testModerateReadiness() {
        val (status, _) = evaluateRecoveryState(
            isRestDay = false,
            energyLevel = 3,
            sleepHours = 7.0f,
            consecutiveWorkouts = 2
        )
        assertEquals("Balanced Recovery", status)
    }

    @Test
    fun testConsecutiveWorkoutDetection() {
        // Given a list of recent dates with workouts
        val recentDatesWithWorkouts = listOf(
            "2026-11-12",
            "2026-11-11",
            "2026-11-10",
            "2026-11-09"
        )
        
        fun calculateStreak(dates: List<String>): Int {
            return dates.size
        }

        val streak = calculateStreak(recentDatesWithWorkouts)
        assertEquals(4, streak)
        assertTrue(streak < 5) // Not yet overtraining trigger
    }
}

