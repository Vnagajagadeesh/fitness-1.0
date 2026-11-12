package com.jd.fitness

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachAdaptiveEngineTest {

    enum class PersonalityState {
        HIGH_PERFORMANCE,
        STRUGGLING,
        INCONSISTENT,
        RECOVERY_NEEDED
    }

    data class CoachEvaluation(
        val state: PersonalityState,
        val guidance: String,
        val evidenceSummary: String
    )

    private fun evaluateCoachState(
        weeklyCompliance: Float,
        workoutsThisWeek: Int,
        isRestDayToday: Boolean,
        consecutiveWorkouts: Int,
        energyLevel: Int
    ): CoachEvaluation {
        return when {
            isRestDayToday || consecutiveWorkouts >= 5 || energyLevel <= 2 -> {
                CoachEvaluation(
                    state = PersonalityState.RECOVERY_NEEDED,
                    guidance = "Your body requires scheduled recovery to adapt and grow stronger. Focus on light stretching, optimal nutrition, and deep sleep.",
                    evidenceSummary = "Rest day recorded: $isRestDayToday, Consecutive workout streak: $consecutiveWorkouts days, Energy level: $energyLevel/5."
                )
            }
            weeklyCompliance >= 80f && workoutsThisWeek >= 3 -> {
                CoachEvaluation(
                    state = PersonalityState.HIGH_PERFORMANCE,
                    guidance = "Phenomenal execution this week! Your consistency is establishing elite discipline. Keep raising the bar.",
                    evidenceSummary = "Weekly habit compliance is ${weeklyCompliance.toInt()}% with $workoutsThisWeek recorded workouts."
                )
            }
            weeklyCompliance < 40f -> {
                CoachEvaluation(
                    state = PersonalityState.STRUGGLING,
                    guidance = "Momentum is built one small win at a time. Forget yesterday's missed tasks and complete just 1 priority habit today.",
                    evidenceSummary = "Weekly habit compliance is currently ${weeklyCompliance.toInt()}%. Simplifying targets recommended."
                )
            }
            else -> {
                CoachEvaluation(
                    state = PersonalityState.INCONSISTENT,
                    guidance = "Your discipline shows promise, but consistency fluctuates across the week. Focus on setting firm time blocks for your core habits.",
                    evidenceSummary = "Weekly habit compliance is ${weeklyCompliance.toInt()}% with $workoutsThisWeek recorded workouts."
                )
            }
        }
    }

    @Test
    fun testHighPerformanceState() {
        val result = evaluateCoachState(
            weeklyCompliance = 88f,
            workoutsThisWeek = 4,
            isRestDayToday = false,
            consecutiveWorkouts = 2,
            energyLevel = 5
        )
        assertEquals(PersonalityState.HIGH_PERFORMANCE, result.state)
        assertTrue(result.guidance.contains("Phenomenal execution"))
        assertTrue(result.evidenceSummary.contains("88%"))
    }

    @Test
    fun testStrugglingState() {
        val result = evaluateCoachState(
            weeklyCompliance = 28f,
            workoutsThisWeek = 0,
            isRestDayToday = false,
            consecutiveWorkouts = 0,
            energyLevel = 3
        )
        assertEquals(PersonalityState.STRUGGLING, result.state)
        assertTrue(result.guidance.contains("Momentum is built one small win at a time"))
        assertTrue(result.evidenceSummary.contains("28%"))
    }

    @Test
    fun testRecoveryNeededDueToOvertraining() {
        val result = evaluateCoachState(
            weeklyCompliance = 90f,
            workoutsThisWeek = 5,
            isRestDayToday = false,
            consecutiveWorkouts = 6,
            energyLevel = 3
        )
        assertEquals(PersonalityState.RECOVERY_NEEDED, result.state)
        assertTrue(result.evidenceSummary.contains("streak: 6 days"))
    }

    @Test
    fun testRecoveryNeededDueToRestDay() {
        val result = evaluateCoachState(
            weeklyCompliance = 75f,
            workoutsThisWeek = 3,
            isRestDayToday = true,
            consecutiveWorkouts = 3,
            energyLevel = 4
        )
        assertEquals(PersonalityState.RECOVERY_NEEDED, result.state)
        assertTrue(result.evidenceSummary.contains("Rest day recorded: true"))
    }

    @Test
    fun testInconsistentState() {
        val result = evaluateCoachState(
            weeklyCompliance = 62f,
            workoutsThisWeek = 2,
            isRestDayToday = false,
            consecutiveWorkouts = 1,
            energyLevel = 4
        )
        assertEquals(PersonalityState.INCONSISTENT, result.state)
        assertTrue(result.guidance.contains("consistency fluctuates"))
    }
}

