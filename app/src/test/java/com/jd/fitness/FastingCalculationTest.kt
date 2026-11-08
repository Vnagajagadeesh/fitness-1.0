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
    fun testMetabolicFastingStages() {
        fun getStage(hours: Float): String = when {
            hours >= 24f -> "AUTOPHAGY"
            hours >= 16f -> "KETOSIS"
            hours >= 12f -> "FAT_BURNING"
            hours >= 4f -> "CATABOLIC"
            else -> "ANABOLIC"
        }

        assertEquals("ANABOLIC", getStage(2f))
        assertEquals("CATABOLIC", getStage(6f))
        assertEquals("FAT_BURNING", getStage(14f))
        assertEquals("KETOSIS", getStage(18f))
        assertEquals("AUTOPHAGY", getStage(24f))
        assertEquals("AUTOPHAGY", getStage(36f))
    }
}
