package com.jd.fitness

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class HabitStreakTest {

    @Test
    fun testConsecutiveDaysStreakCalculation() {
        fun calculateStreak(completedDates: Set<LocalDate>, today: LocalDate): Int {
            var streak = 0
            var checkDate = today
            while (completedDates.contains(checkDate)) {
                streak++
                checkDate = checkDate.minusDays(1)
            }
            return streak
        }

        val today = LocalDate.parse("2026-11-08")
        val continuousDates = setOf(
            LocalDate.parse("2026-11-08"),
            LocalDate.parse("2026-11-07"),
            LocalDate.parse("2026-11-06"),
            LocalDate.parse("2026-11-05"),
            LocalDate.parse("2026-11-04"),
            LocalDate.parse("2026-11-03")
        )

        assertEquals(6, calculateStreak(continuousDates, today))

        // Missed yesterday (Nov 7) -> streak is 1
        val brokenDates = setOf(
            LocalDate.parse("2026-11-08"),
            LocalDate.parse("2026-11-06"),
            LocalDate.parse("2026-11-05")
        )
        assertEquals(1, calculateStreak(brokenDates, today))

        // Missed today -> streak is 0
        val missedTodayDates = setOf(
            LocalDate.parse("2026-11-07"),
            LocalDate.parse("2026-11-06")
        )
        assertEquals(0, calculateStreak(missedTodayDates, today))
    }

    @Test
    fun testDailyDisciplineScore() {
        fun calculateDisciplineScore(completed: Int, total: Int): Int {
            if (total == 0) return 0
            return ((completed.toFloat() / total.toFloat()) * 100f).toInt()
        }

        // All 13 original habits completed
        assertEquals(100, calculateDisciplineScore(13, 13))

        // 10 out of 13 habits completed
        assertEquals(76, calculateDisciplineScore(10, 13))

        // 0 habits completed
        assertEquals(0, calculateDisciplineScore(0, 13))

        // Perfect day check
        val isPerfectDay = calculateDisciplineScore(13, 13) == 100
        assertEquals(true, isPerfectDay)
    }

    @Test
    fun testLongestStreakCalculation() {
        fun calculateLongestStreak(sortedDates: List<LocalDate>): Int {
            if (sortedDates.isEmpty()) return 0
            var maxStreak = 1
            var currentStreak = 1

            for (i in 1 until sortedDates.size) {
                if (sortedDates[i] == sortedDates[i - 1].plusDays(1)) {
                    currentStreak++
                    if (currentStreak > maxStreak) maxStreak = currentStreak
                } else if (sortedDates[i] != sortedDates[i - 1]) {
                    currentStreak = 1
                }
            }
            return maxStreak
        }

        // 3 days streak, gap, then 5 days streak
        val dates = listOf(
            LocalDate.parse("2026-11-01"),
            LocalDate.parse("2026-11-02"),
            LocalDate.parse("2026-11-03"),
            // Gap on Nov 4
            LocalDate.parse("2026-11-05"),
            LocalDate.parse("2026-11-06"),
            LocalDate.parse("2026-11-07"),
            LocalDate.parse("2026-11-08"),
            LocalDate.parse("2026-11-09")
        )

        assertEquals(5, calculateLongestStreak(dates))
    }
}

