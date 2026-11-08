package com.jd.fitness.data.repository

import com.jd.fitness.data.local.FitnessDatabase
import com.jd.fitness.data.local.entity.ChallengeEntity
import com.jd.fitness.data.local.entity.FastingEntity
import com.jd.fitness.data.local.entity.FastingMealEntity
import com.jd.fitness.data.local.entity.FastingState
import com.jd.fitness.data.local.entity.HabitCategory
import com.jd.fitness.data.local.entity.HabitEntity
import com.jd.fitness.data.local.entity.HabitLogEntity
import com.jd.fitness.data.local.entity.SkincareLogEntity
import com.jd.fitness.data.local.entity.SkincareRoutineEntity
import com.jd.fitness.data.local.entity.SkincareRoutineType
import com.jd.fitness.data.local.entity.StepLogEntity
import com.jd.fitness.data.local.entity.WeightEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class ChallengeProgress(
    val currentDay: Int,
    val totalDays: Int,
    val daysCompleted: Int,
    val daysRemaining: Int,
    val overallPercentage: Float,
    val currentMilestone: String,
    val nextMilestone: String,
    val isFinished: Boolean
)

data class DaySummary(
    val date: String,
    val totalHabits: Int,
    val completedHabits: Int,
    val percentage: Float,
    val isPerfect: Boolean
)

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val perfectDaysCount: Int,
    val totalCheckIns: Int
)

class FitnessRepository(private val database: FitnessDatabase) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getTodayString(): String = LocalDate.now().format(dateFormatter)

    // --- Challenge ---
    fun getChallengeConfig(): Flow<ChallengeEntity?> = database.challengeDao().getChallengeConfig()

    suspend fun getChallengeConfigOnce(): ChallengeEntity? = database.challengeDao().getChallengeConfigOnce()

    suspend fun updateChallengeConfig(config: ChallengeEntity) = database.challengeDao().insertOrUpdate(config)

    fun getChallengeProgressFlow(): Flow<ChallengeProgress> {
        return database.challengeDao().getChallengeConfig().map { config ->
            val cfg = config ?: ChallengeEntity()
            val start = runCatching { LocalDate.parse(cfg.startDate, dateFormatter) }.getOrDefault(LocalDate.now())
            val end = runCatching { LocalDate.parse(cfg.endDate, dateFormatter) }.getOrDefault(LocalDate.now().plusDays(90))
            val today = LocalDate.now()

            val totalDays = (ChronoUnit.DAYS.between(start, end) + 1).toInt().coerceAtLeast(1)
            val daysSinceStart = (ChronoUnit.DAYS.between(start, today) + 1).toInt()

            val currentDay = when {
                daysSinceStart < 1 -> 1
                daysSinceStart > totalDays -> totalDays
                else -> daysSinceStart
            }

            val daysRemaining = (totalDays - currentDay).coerceAtLeast(0)
            val overallPercentage = ((currentDay.toFloat() / totalDays.toFloat()) * 100f).coerceIn(0f, 100f)

            val currentMilestone = when {
                currentDay >= 90 -> "TRANSFORMATION"
                currentDay >= 60 -> "MOMENTUM"
                currentDay >= 30 -> "FOUNDATION"
                else -> "KICKOFF"
            }

            val nextMilestone = when {
                currentDay < 30 -> "FOUNDATION (Day 30)"
                currentDay < 60 -> "MOMENTUM (Day 60)"
                currentDay < 90 -> "TRANSFORMATION (Day 90)"
                else -> "CHALLENGE COMPLETE"
            }

            ChallengeProgress(
                currentDay = currentDay,
                totalDays = totalDays,
                daysCompleted = currentDay - 1,
                daysRemaining = daysRemaining,
                overallPercentage = overallPercentage,
                currentMilestone = currentMilestone,
                nextMilestone = nextMilestone,
                isFinished = daysSinceStart > totalDays
            )
        }
    }

    // --- Habits ---
    fun getActiveHabits(): Flow<List<HabitEntity>> = database.habitDao().getActiveHabits()

    fun getAllHabits(): Flow<List<HabitEntity>> = database.habitDao().getAllHabits()

    suspend fun getHabitById(id: Long): HabitEntity? = database.habitDao().getHabitById(id)

    suspend fun saveHabit(habit: HabitEntity): Long {
        return if (habit.id == 0L) {
            database.habitDao().insertHabit(habit)
        } else {
            database.habitDao().updateHabit(habit)
            habit.id
        }
    }

    suspend fun deleteHabit(habit: HabitEntity) = database.habitDao().deleteHabit(habit)

    fun getHabitLogsForDate(date: String): Flow<List<HabitLogEntity>> =
        database.habitDao().getLogsForDate(date)

    suspend fun toggleHabitCompletion(date: String, habitId: Long, isCompleted: Boolean, notes: String = "") {
        val existing = database.habitDao().getLogForHabit(date, habitId)
        val updated = (existing ?: HabitLogEntity(date = date, habitId = habitId)).copy(
            isCompleted = isCompleted,
            completedAt = if (isCompleted) System.currentTimeMillis() else null,
            notes = notes
        )
        database.habitDao().insertOrUpdateLog(updated)
    }

    fun getDaySummary(date: String): Flow<DaySummary> {
        return combine(
            database.habitDao().getActiveHabits(),
            database.habitDao().getLogsForDate(date)
        ) { activeHabits, logs ->
            val activeIds = activeHabits.map { it.id }.toSet()
            val total = activeIds.size
            val completed = logs.count { it.isCompleted && activeIds.contains(it.habitId) }
            val percentage = if (total > 0) (completed.toFloat() / total.toFloat()) * 100f else 0f
            DaySummary(
                date = date,
                totalHabits = total,
                completedHabits = completed,
                percentage = percentage,
                isPerfect = total > 0 && completed == total
            )
        }
    }

    suspend fun calculateStreaks(): StreakInfo {
        val allCompletedDates = database.habitDao().getAllCompletedDates()
            .mapNotNull { runCatching { LocalDate.parse(it, dateFormatter) }.getOrNull() }
            .distinct()
            .sortedDescending()

        if (allCompletedDates.isEmpty()) {
            return StreakInfo(0, 0, 0, 0)
        }

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        // Calculate current streak
        var currentStreak = 0
        var checkDate = if (allCompletedDates.contains(today)) today else yesterday

        while (allCompletedDates.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        // Calculate longest streak
        var longestStreak = 0
        var tempStreak = 0
        var prevDate: LocalDate? = null

        val sortedAsc = allCompletedDates.sorted()
        for (date in sortedAsc) {
            if (prevDate == null || date == prevDate.plusDays(1)) {
                tempStreak++
            } else if (date != prevDate) {
                tempStreak = 1
            }
            if (tempStreak > longestStreak) {
                longestStreak = tempStreak
            }
            prevDate = date
        }

        // Count perfect days
        val activeHabits = database.habitDao().getActiveHabitsOnce()
        val totalActive = activeHabits.size
        var perfectDays = 0

        if (totalActive > 0) {
            for (date in sortedAsc) {
                val dateStr = date.format(dateFormatter)
                val logs = database.habitDao().getLogsForDateOnce(dateStr)
                val activeIds = activeHabits.map { it.id }.toSet()
                val completed = logs.count { it.isCompleted && activeIds.contains(it.habitId) }
                if (completed >= totalActive) {
                    perfectDays++
                }
            }
        }

        return StreakInfo(
            currentStreak = currentStreak,
            longestStreak = longestStreak.coerceAtLeast(currentStreak),
            perfectDaysCount = perfectDays,
            totalCheckIns = allCompletedDates.size
        )
    }

    // --- Weight ---
    fun getAllWeightLogs(): Flow<List<WeightEntity>> = database.weightDao().getAllWeightLogs()

    fun getLatestWeight(): Flow<WeightEntity?> = database.weightDao().getLatestWeight()

    fun getWeightLogsBetween(startDate: String, endDate: String): Flow<List<WeightEntity>> =
        database.weightDao().getWeightsBetweenDates(startDate, endDate)

    fun getAllTimeAverageWeight(): Flow<Double?> = database.weightDao().getAllTimeAverageWeight()

    suspend fun insertWeight(kg: Double, notes: String = "", date: String = getTodayString(), timestamp: Long = System.currentTimeMillis()) {
        database.weightDao().insertWeight(
            WeightEntity(
                date = date,
                timestamp = timestamp,
                weightKg = kg,
                notes = notes
            )
        )
    }

    suspend fun updateWeight(weight: WeightEntity) = database.weightDao().updateWeight(weight)

    suspend fun deleteWeight(weight: WeightEntity) = database.weightDao().deleteWeight(weight)

    suspend fun deleteWeightById(id: Long) = database.weightDao().deleteWeightById(id)

    // --- Skincare ---
    fun getSkincareRoutine(type: SkincareRoutineType): Flow<List<SkincareRoutineEntity>> =
        database.skincareDao().getRoutinesByType(type)

    fun getSkincareLog(date: String, type: SkincareRoutineType): Flow<SkincareLogEntity?> =
        database.skincareDao().getLogForDateAndType(date, type)

    suspend fun toggleSkincareCompletion(date: String, type: SkincareRoutineType, isCompleted: Boolean, completedSteps: String = "[]", notes: String = "") {
        val existing = database.skincareDao().getLogForDateAndTypeOnce(date, type)
        val updated = (existing ?: SkincareLogEntity(date = date, routineType = type)).copy(
            isCompleted = isCompleted,
            completedAt = if (isCompleted) System.currentTimeMillis() else null,
            completedStepsJson = completedSteps,
            notes = notes
        )
        database.skincareDao().insertOrUpdateLog(updated)

        // Also sync with daily habit checklist for Morning or Night skincare!
        val habitTitle = if (type == SkincareRoutineType.MORNING) "Morning skincare" else "Night skincare"
        val activeHabits = database.habitDao().getActiveHabitsOnce()
        val skincareHabit = activeHabits.firstOrNull { it.title.equals(habitTitle, ignoreCase = true) }
        skincareHabit?.let {
            toggleHabitCompletion(date, it.id, isCompleted)
        }
    }

    suspend fun saveSkincareStep(step: SkincareRoutineEntity): Long {
        return if (step.id == 0L) {
            database.skincareDao().insertRoutineStep(step)
        } else {
            database.skincareDao().updateRoutineStep(step)
            step.id
        }
    }

    suspend fun deleteSkincareStep(step: SkincareRoutineEntity) =
        database.skincareDao().deleteRoutineStep(step)

    // --- Fasting ---
    fun getActiveFastingSession(): Flow<FastingEntity?> = database.fastingDao().getActiveSession()

    fun getAllFastingSessions(): Flow<List<FastingEntity>> = database.fastingDao().getAllSessions()

    fun getFastingMealsForSession(sessionId: Long): Flow<List<FastingMealEntity>> =
        database.fastingDao().getMealsForSession(sessionId)

    suspend fun startFasting(targetHours: Int = 16, isOMAD: Boolean = false, notes: String = ""): Long {
        // End any previous active session
        val active = database.fastingDao().getActiveSessionOnce()
        if (active != null) {
            database.fastingDao().updateSession(active.copy(endTime = System.currentTimeMillis(), state = FastingState.COMPLETED))
        }

        return database.fastingDao().insertSession(
            FastingEntity(
                startTime = System.currentTimeMillis(),
                targetHours = targetHours,
                isOMAD = isOMAD,
                state = FastingState.ACTIVE,
                notes = notes
            )
        )
    }

    suspend fun endFasting(session: FastingEntity, notes: String = "") {
        database.fastingDao().updateSession(
            session.copy(
                endTime = System.currentTimeMillis(),
                state = FastingState.COMPLETED,
                notes = if (notes.isNotBlank()) notes else session.notes
            )
        )
    }

    suspend fun cancelFasting(session: FastingEntity) {
        database.fastingDao().updateSession(
            session.copy(
                endTime = System.currentTimeMillis(),
                state = FastingState.CANCELLED
            )
        )
    }

    suspend fun logMeal(mealTitle: String, isOMADMeal: Boolean = false, notes: String = "", sessionId: Long? = null): Long {
        return database.fastingDao().insertMeal(
            FastingMealEntity(
                sessionId = sessionId,
                mealTime = System.currentTimeMillis(),
                mealTitle = mealTitle,
                isOMADMeal = isOMADMeal,
                notes = notes
            )
        )
    }

    // --- Steps ---
    fun getStepsForDate(date: String): Flow<StepLogEntity?> = database.stepDao().getStepsForDate(date)

    fun getStepsBetween(startDate: String, endDate: String): Flow<List<StepLogEntity>> =
        database.stepDao().getStepsBetweenDates(startDate, endDate)

    suspend fun saveStepCount(date: String, count: Int, source: String = "HEALTH_CONNECT") {
        database.stepDao().insertOrUpdateStep(
            StepLogEntity(
                date = date,
                stepCount = count,
                source = source,
                lastUpdated = System.currentTimeMillis()
            )
        )

        // If >= 10,000 steps, also mark the 10,000 steps habit as completed!
        if (count >= 10000) {
            val activeHabits = database.habitDao().getActiveHabitsOnce()
            val stepHabit = activeHabits.firstOrNull { it.title.contains("10,000 steps", ignoreCase = true) }
            stepHabit?.let {
                toggleHabitCompletion(date, it.id, true)
            }
        }
    }
}

