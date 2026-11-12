package com.jd.fitness

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FastingCalculationTest {

    @Test
    fun testTimerFormatting() {
        fun formatTimer(seconds: Long): String {
            val h = seconds / 3600
            val m = (seconds % 3600) / 60
            val s = seconds % 60
            return String.format("%02d:%02d:%02d", h, m, s)
        }

        assertEquals("00:00:00", formatTimer(0))
        assertEquals("00:01:05", formatTimer(65))
        assertEquals("01:30:15", formatTimer(5415))
        assertEquals("16:00:00", formatTimer(16 * 3600))
        assertEquals("23:45:12", formatTimer(23 * 3600 + 45 * 60 + 12))
    }

    @Test
    fun testProgressPercentage() {
        val targetHours = 16
        val targetSeconds = targetHours * 3600L

        // 8 hours in = 50%
        val halfElapsed = 8 * 3600L
        val halfProgress = (halfElapsed.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)
        assertEquals(0.5f, halfProgress, 0.001f)

        // 16 hours in = 100%
        val fullElapsed = 16 * 3600L
        val fullProgress = (fullElapsed.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)
        assertEquals(1.0f, fullProgress, 0.001f)

        // Over target = coerced to 1.0f
        val overElapsed = 20 * 3600L
        val overProgress = (overElapsed.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)
        assertEquals(1.0f, overProgress, 0.001f)
    }

    @Test
    fun testEducationalFastingStages() {
        fun getEducationalStage(hours: Double): Int = when {
            hours < 4.0 -> 1 // Early Post-Meal Period
            hours < 8.0 -> 2 // Post-Absorptive Period
            hours < 12.0 -> 3 // Nutrient Utilization Transition
            hours < 16.0 -> 4 // Increasing Reliance on Stored Energy
            else -> 5 // Further Metabolic Adaptation
        }

        assertEquals(1, getEducationalStage(0.5))
        assertEquals(1, getEducationalStage(3.9))
        assertEquals(2, getEducationalStage(4.0))
        assertEquals(2, getEducationalStage(7.5))
        assertEquals(3, getEducationalStage(8.0))
        assertEquals(3, getEducationalStage(11.9))
        assertEquals(4, getEducationalStage(12.0))
        assertEquals(4, getEducationalStage(15.9))
        assertEquals(5, getEducationalStage(16.0))
        assertEquals(5, getEducationalStage(23.5))
    }

    @Test
    fun testCustomDurationAndOvernightFastCalculation() {
        val startMillis = 1700000000000L // arbitrary base
        val sixteenHoursMillis = startMillis + (16 * 3600 * 1000L)
        val durationHours = ((sixteenHoursMillis - startMillis) / 3600000.0).toInt()
        assertEquals(16, durationHours)

        val omadDurationMillis = startMillis + (23 * 3600 * 1000L)
        val omadHours = ((omadDurationMillis - startMillis) / 3600000.0).toInt()
        assertEquals(23, omadHours)

        val remainingMillis = (sixteenHoursMillis - (startMillis + 10 * 3600 * 1000L)).coerceAtLeast(0)
        assertEquals(6 * 3600 * 1000L, remainingMillis)
    }
}
