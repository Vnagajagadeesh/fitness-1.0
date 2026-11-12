package com.jd.fitness

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklyReportCalculationTest {

    data class HabitCompletionSummary(
        val habitTitle: String,
        val completionsThisWeek: Int
    )

    private fun calculateWeeklyMetrics(
        habits: List<HabitCompletionSummary>,
        dailySteps: List<Int>,
        workoutCount: Int,
        restDaysCount: Int
    ): Map<String, Any> {
        val totalHabits = habits.size
        val totalPossibleCompletions = totalHabits * 7
        val totalActualCompletions = habits.sumOf { it.completionsThisWeek }

        val complianceRate = if (totalPossibleCompletions > 0) {
            (totalActualCompletions.toFloat() / totalPossibleCompletions * 100).coerceIn(0f, 100f)
        } else {
            0f
        }

        val bestHabit = habits.maxByOrNull { it.completionsThisWeek }?.habitTitle ?: "N/A"
        val worstHabit = habits.minByOrNull { it.completionsThisWeek }?.habitTitle ?: "N/A"
        val avgSteps = if (dailySteps.isNotEmpty()) dailySteps.average().toInt() else 0

        val suggestions = mutableListOf<String>()
        if (complianceRate < 60f) {
            suggestions.add("Focus on 2 core habits next week to rebuild momentum without feeling overwhelmed.")
        } else if (complianceRate >= 85f) {
            suggestions.add("Outstanding habit discipline! Maintain this standard and consider raising targets.")
        }

        if (workoutCount >= 5 && restDaysCount == 0) {
            suggestions.add("Schedule at least 1-2 dedicated recovery days to allow muscle repair and avoid fatigue.")
        }

        if (avgSteps < 6000) {
            suggestions.add("Add a 15-minute daily brisk walk to boost daily active step average above 7,000.")
        }

        return mapOf(
            "complianceRate" to complianceRate,
            "bestHabit" to bestHabit,
            "worstHabit" to worstHabit,
            "avgSteps" to avgSteps,
            "suggestions" to suggestions
        )
    }

    @Test
    fun testComplianceRateCalculation() {
        val habits = listOf(
            HabitCompletionSummary("Morning Pushups", 7),
            HabitCompletionSummary("Read 20 pages", 5),
            HabitCompletionSummary("Skincare Routine", 6),
            HabitCompletionSummary("Coding Practice", 3)
        )
        // Total possible = 4 * 7 = 28
        // Total actual = 7 + 5 + 6 + 3 = 21
        // Compliance = 21 / 28 = 75.0%

        val metrics = calculateWeeklyMetrics(
            habits = habits,
            dailySteps = listOf(8000, 9500, 7000, 8200, 6500, 11000, 7800),
            workoutCount = 4,
            restDaysCount = 1
        )

        val compliance = metrics["complianceRate"] as Float
        assertEquals(75.0f, compliance, 0.01f)
        assertEquals("Morning Pushups", metrics["bestHabit"])
        assertEquals("Coding Practice", metrics["worstHabit"])
        assertEquals(8285, metrics["avgSteps"])
    }

    @Test
    fun testSuggestionsForOvertrainingWithoutRest() {
        val habits = listOf(HabitCompletionSummary("Gym", 6))
        val metrics = calculateWeeklyMetrics(
            habits = habits,
            dailySteps = listOf(10000, 10000, 10000),
            workoutCount = 6,
            restDaysCount = 0
        )

        @Suppress("UNCHECKED_CAST")
        val suggestions = metrics["suggestions"] as List<String>
        assertTrue(suggestions.any { it.contains("dedicated recovery days") })
    }

    @Test
    fun testSuggestionsForLowStepCount() {
        val habits = listOf(HabitCompletionSummary("Study", 7))
        val metrics = calculateWeeklyMetrics(
            habits = habits,
            dailySteps = listOf(3000, 4200, 2800),
            workoutCount = 2,
            restDaysCount = 2
        )

        @Suppress("UNCHECKED_CAST")
        val suggestions = metrics["suggestions"] as List<String>
        assertTrue(suggestions.any { it.contains("15-minute daily brisk walk") })
    }
}

