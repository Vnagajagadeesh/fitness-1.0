package com.jd.fitness

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MilestonesCalculationTest {

    data class MockAchievement(
        val id: String,
        val title: String,
        val target: Int,
        var progress: Int = 0,
        var isUnlocked: Boolean = false,
        var unlockedAt: Long? = null
    ) {
        fun evaluate(currentValue: Int, nowMillis: Long) {
            val clampedProgress = currentValue.coerceAtMost(target)
            if (clampedProgress > progress) {
                progress = clampedProgress
            }
            if (!isUnlocked && progress >= target) {
                isUnlocked = true
                unlockedAt = nowMillis
            }
        }
    }

    @Test
    fun testFirstWorkoutUnlock() {
        val achievement = MockAchievement(
            id = "fit_first_workout",
            title = "First Blood",
            target = 1
        )
        assertFalse(achievement.isUnlocked)
        assertEquals(0, achievement.progress)

        achievement.evaluate(currentValue = 1, nowMillis = 1700000000L)
        assertTrue(achievement.isUnlocked)
        assertEquals(1, achievement.progress)
        assertEquals(1700000000L, achievement.unlockedAt)
    }

    @Test
    fun testProgressClampingAtTarget() {
        val achievement = MockAchievement(
            id = "fit_10_workouts",
            title = "Iron Discipline",
            target = 10
        )
        // 5 workouts
        achievement.evaluate(currentValue = 5, nowMillis = 1700000000L)
        assertFalse(achievement.isUnlocked)
        assertEquals(5, achievement.progress)

        // 15 workouts (exceeds target)
        achievement.evaluate(currentValue = 15, nowMillis = 1700005000L)
        assertTrue(achievement.isUnlocked)
        assertEquals(10, achievement.progress) // Should be clamped at target 10
        assertEquals(1700005000L, achievement.unlockedAt)
    }

    @Test
    fun testUnlockDoesNotRegress() {
        val achievement = MockAchievement(
            id = "habit_7_days",
            title = "Week Champion",
            target = 7,
            progress = 7,
            isUnlocked = true,
            unlockedAt = 1600000000L
        )

        // Even if streak resets to 2, unlocked status must be preserved
        achievement.evaluate(currentValue = 2, nowMillis = 1700000000L)
        assertTrue(achievement.isUnlocked)
        assertEquals(7, achievement.progress)
        assertEquals(1600000000L, achievement.unlockedAt)
    }

    @Test
    fun testStepMilestones() {
        val achievement = MockAchievement(
            id = "fit_10k_steps",
            title = "10,000 Steps",
            target = 10000
        )

        achievement.evaluate(currentValue = 8500, nowMillis = 1700000000L)
        assertFalse(achievement.isUnlocked)
        assertEquals(8500, achievement.progress)

        achievement.evaluate(currentValue = 10500, nowMillis = 1700002000L)
        assertTrue(achievement.isUnlocked)
        assertEquals(10000, achievement.progress)
    }

    @Test
    fun testStudyProductivityMilestones() {
        val sevenStudy = MockAchievement(
            id = "prod_7_study",
            title = "Knowledge Seeker",
            target = 7
        )

        sevenStudy.evaluate(currentValue = 6, nowMillis = 1700000000L)
        assertFalse(sevenStudy.isUnlocked)

        sevenStudy.evaluate(currentValue = 7, nowMillis = 1700003000L)
        assertTrue(sevenStudy.isUnlocked)
    }
}

