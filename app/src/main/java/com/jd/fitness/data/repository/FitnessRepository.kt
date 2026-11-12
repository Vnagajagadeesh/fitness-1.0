package com.jd.fitness.data.repository

import com.jd.fitness.data.local.FitnessDatabase
import com.jd.fitness.data.local.entity.AchievementCategory
import com.jd.fitness.data.local.entity.AchievementEntity
import com.jd.fitness.data.local.entity.ChallengeEntity
import com.jd.fitness.data.local.entity.CoachInsightEntity
import com.jd.fitness.data.local.entity.CoachPersonalityState
import com.jd.fitness.data.local.entity.FastingEntity
import com.jd.fitness.data.local.entity.FastingMealEntity
import com.jd.fitness.data.local.entity.FastingScheduleEntity
import com.jd.fitness.data.local.entity.FastingState
import com.jd.fitness.data.local.entity.HabitCategory
import com.jd.fitness.data.local.entity.HabitEntity
import com.jd.fitness.data.local.entity.HabitLogEntity
import com.jd.fitness.data.local.entity.JournalEntity
import com.jd.fitness.data.local.entity.MotivationCategory
import com.jd.fitness.data.local.entity.MotivationEntity
import com.jd.fitness.data.local.entity.PerceivedEnergy
import com.jd.fitness.data.local.entity.PlannerTaskEntity
import com.jd.fitness.data.local.entity.RecoveryEntity
import com.jd.fitness.data.local.entity.SkincareLogEntity
import com.jd.fitness.data.local.entity.SkincareRoutineEntity
import com.jd.fitness.data.local.entity.SkincareRoutineType
import com.jd.fitness.data.local.entity.StepLogEntity
import com.jd.fitness.data.local.entity.TaskCategory
import com.jd.fitness.data.local.entity.WeeklyReportEntity
import com.jd.fitness.data.local.entity.WeightEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
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

    // ==========================================
    // --- FASTING 2.0 ADVANCED CAPABILITIES ---
    // ==========================================
    fun getAllFastingSchedules(): Flow<List<FastingScheduleEntity>> = database.fastingDao().getAllSchedules()

    suspend fun saveFastingSchedule(schedule: FastingScheduleEntity): Long {
        return if (schedule.id == 0L) {
            database.fastingDao().insertSchedule(schedule)
        } else {
            database.fastingDao().updateSchedule(schedule)
            schedule.id
        }
    }

    suspend fun deleteFastingSchedule(schedule: FastingScheduleEntity) = database.fastingDao().deleteSchedule(schedule)

    suspend fun startFastingWithCustomTimes(
        plannedStartTime: Long,
        plannedEndTime: Long?,
        targetHours: Int,
        isOMAD: Boolean = false,
        notes: String = ""
    ): Long {
        val active = database.fastingDao().getActiveSessionOnce()
        if (active != null) {
            database.fastingDao().updateSession(
                active.copy(
                    endTime = System.currentTimeMillis(),
                    state = FastingState.COMPLETED
                )
            )
        }
        return database.fastingDao().insertSession(
            FastingEntity(
                startTime = plannedStartTime,
                plannedStartTime = plannedStartTime,
                plannedEndTime = plannedEndTime,
                targetHours = targetHours,
                isOMAD = isOMAD,
                state = FastingState.ACTIVE,
                notes = notes
            )
        )
    }

    suspend fun updateFastingSession(session: FastingEntity) = database.fastingDao().updateSession(session)

    suspend fun deleteFastingSession(session: FastingEntity) = database.fastingDao().deleteSession(session)

    suspend fun deleteFastingSessionById(id: Long) = database.fastingDao().deleteSessionById(id)

    fun getRecentCompletedSessions(limit: Int = 20): Flow<List<FastingEntity>> =
        database.fastingDao().getRecentCompletedSessions(limit)

    // ==========================================
    // --- RECOVERY CENTRE ---
    // ==========================================
    fun getRecoveryForDate(date: String): Flow<RecoveryEntity?> = database.recoveryDao().getRecoveryForDate(date)

    suspend fun getRecoveryForDateOnce(date: String): RecoveryEntity? = database.recoveryDao().getRecoveryForDateOnce(date)

    fun getAllRecoveryEntries(): Flow<List<RecoveryEntity>> = database.recoveryDao().getAllRecoveryEntries()

    fun getRecoveryBetweenDates(startDate: String, endDate: String): Flow<List<RecoveryEntity>> =
        database.recoveryDao().getRecoveryBetweenDates(startDate, endDate)

    suspend fun saveRecoveryEntry(entry: RecoveryEntity): Long = database.recoveryDao().insertOrUpdate(entry)

    suspend fun deleteRecoveryForDate(date: String) = database.recoveryDao().deleteByDate(date)

    // ==========================================
    // --- MY DAILY PLAN ---
    // ==========================================
    fun getPlannerTasksForDate(date: String): Flow<List<PlannerTaskEntity>> = database.plannerDao().getTasksForDate(date)

    suspend fun savePlannerTask(task: PlannerTaskEntity): Long = database.plannerDao().insertTask(task)

    suspend fun updatePlannerTask(task: PlannerTaskEntity) = database.plannerDao().updateTask(task)

    suspend fun deletePlannerTask(task: PlannerTaskEntity) = database.plannerDao().deleteTask(task)

    suspend fun deletePlannerTaskById(id: Long) = database.plannerDao().deleteTaskById(id)

    suspend fun togglePlannerTask(id: Long, isCompleted: Boolean) = database.plannerDao().setTaskCompleted(id, isCompleted)

    suspend fun seedSuggestedPlannerIfEmpty(date: String) {
        val existing = database.plannerDao().getTasksForDateOnce(date)
        if (existing.isNotEmpty()) return

        val suggestions = listOf(
            PlannerTaskEntity(date = date, title = "Morning Hydration & Skincare", category = TaskCategory.MORNING_ROUTINE.name, startTime = "07:30", endTime = "08:00", priority = 3, orderIndex = 1),
            PlannerTaskEntity(date = date, title = "Focused Deep Study / Academic Work", category = TaskCategory.STUDY.name, startTime = "09:00", endTime = "11:00", priority = 3, orderIndex = 2),
            PlannerTaskEntity(date = date, title = "Nourishing Wholesome Lunch", category = TaskCategory.MEALS.name, startTime = "13:00", endTime = "13:45", priority = 2, orderIndex = 3),
            PlannerTaskEntity(date = date, title = "College & Skill Development", category = TaskCategory.COLLEGE.name, startTime = "15:00", endTime = "17:00", priority = 2, orderIndex = 4),
            PlannerTaskEntity(date = date, title = "Home Workout & 10,000 Steps", category = TaskCategory.FITNESS.name, startTime = "18:00", endTime = "19:15", priority = 3, orderIndex = 5),
            PlannerTaskEntity(date = date, title = "Night Skincare & Journal Reflection", category = TaskCategory.EVENING_ROUTINE.name, startTime = "21:30", endTime = "22:00", priority = 2, orderIndex = 6),
            PlannerTaskEntity(date = date, title = "Restorative Sleep & Recovery", category = TaskCategory.SLEEP.name, startTime = "23:00", endTime = "07:00", priority = 3, orderIndex = 7)
        )
        database.plannerDao().insertTasks(suggestions)
    }

    // ==========================================
    // --- DISCIPLINE MILESTONES ---
    // ==========================================
    fun getAllAchievements(): Flow<List<AchievementEntity>> = database.achievementDao().getAllAchievements()

    fun getUnlockedAchievementsCount(): Flow<Int> = database.achievementDao().getUnlockedCountFlow()

    suspend fun evaluateAndUnlockAchievements(): List<AchievementEntity> {
        val streakInfo = calculateStreaks()
        val habits = database.habitDao().getAllHabitsOnce()
        val allLogs = database.habitDao().getAllLogs()
        val allWeights = database.weightDao().getAllWeightLogsOnce()
        val allSteps = database.stepDao().getAllSteps()

        val achievementsMap = database.achievementDao().getAllAchievementsOnce().associateBy { it.id }.toMutableMap()
        val newlyUnlocked = mutableListOf<AchievementEntity>()
        val now = System.currentTimeMillis()

        suspend fun check(id: String, progress: Int, max: Int) {
            val ach = achievementsMap[id] ?: return
            val eligible = progress >= max
            val isNewlyUnlocked = !ach.isUnlocked && eligible
            val updated = ach.copy(
                currentProgress = progress.coerceAtMost(max),
                isUnlocked = ach.isUnlocked || eligible,
                unlockedAt = if (isNewlyUnlocked) now else ach.unlockedAt
            )
            if (isNewlyUnlocked) {
                newlyUnlocked.add(updated)
            }
            database.achievementDao().insertOrUpdate(updated)
        }

        // Habit streaks
        check("habit_first_day", if (streakInfo.totalCheckIns >= 1) 1 else 0, 1)
        check("habit_7d", streakInfo.longestStreak, 7)
        check("habit_14d", streakInfo.longestStreak, 14)
        check("habit_30d", streakInfo.longestStreak, 30)
        check("habit_60d", streakInfo.longestStreak, 60)
        check("habit_90d", streakInfo.longestStreak, 90)

        // Fitness achievements
        val fitnessHabits = habits.filter { it.category == HabitCategory.FITNESS }.map { it.id }.toSet()
        val totalWorkouts = allLogs.count { it.isCompleted && fitnessHabits.contains(it.habitId) }
        check("fit_first_workout", totalWorkouts, 1)
        check("fit_10_workouts", totalWorkouts, 10)

        val maxDailySteps = allSteps.maxOfOrNull { it.stepCount } ?: 0
        check("fit_10k_steps_day", maxDailySteps, 10000)

        val daysOver10k = allSteps.filter { it.stepCount >= 10000 }.map { it.date }.distinct().size
        check("fit_7d_steps", daysOver10k, 7)

        val uniqueWeightDates = allWeights.map { it.date }.distinct().size
        check("fit_weight_logged", uniqueWeightDates, 7)

        // Productivity
        val studyHabits = habits.filter { it.category == HabitCategory.STUDY }.map { it.id }.toSet()
        val studyCount = allLogs.count { it.isCompleted && studyHabits.contains(it.habitId) }
        check("prod_7_study", studyCount, 7)
        check("prod_30_study", studyCount, 30)

        val morningSkincareHabits = habits.filter { it.title.contains("morning skincare", ignoreCase = true) }.map { it.id }.toSet()
        val morningCount = allLogs.count { it.isCompleted && morningSkincareHabits.contains(it.habitId) }
        check("prod_morning_routine", morningCount, 7)

        val nightSkincareHabits = habits.filter { it.title.contains("night skincare", ignoreCase = true) }.map { it.id }.toSet()
        val nightCount = allLogs.count { it.isCompleted && nightSkincareHabits.contains(it.habitId) }
        check("prod_evening_routine", nightCount, 7)

        return newlyUnlocked
    }

    // ==========================================
    // --- MY PRIVATE JOURNAL ---
    // ==========================================
    fun getAllJournalEntries(): Flow<List<JournalEntity>> = database.journalDao().getAllEntries()

    fun getJournalEntriesForDate(date: String): Flow<List<JournalEntity>> = database.journalDao().getEntriesForDate(date)

    fun searchJournalEntries(query: String): Flow<List<JournalEntity>> = database.journalDao().searchEntries(query)

    suspend fun saveJournalEntry(entry: JournalEntity): Long = database.journalDao().insertEntry(entry)

    suspend fun deleteJournalEntry(entry: JournalEntity) = database.journalDao().deleteEntry(entry)

    suspend fun deleteJournalEntryById(id: Long) = database.journalDao().deleteEntryById(id)

    // ==========================================
    // --- MOTIVATION HUB ---
    // ==========================================
    fun getAllMotivationQuotes(): Flow<List<MotivationEntity>> = database.motivationDao().getAllQuotes()

    fun getFavoriteMotivationQuotes(): Flow<List<MotivationEntity>> = database.motivationDao().getFavoriteQuotes()

    suspend fun saveMotivationQuote(quote: MotivationEntity): Long = database.motivationDao().insertQuote(quote)

    suspend fun toggleFavoriteQuote(id: Long, isFavorite: Boolean) = database.motivationDao().toggleFavorite(id, isFavorite)

    suspend fun getRandomMotivationQuote(): MotivationEntity? {
        val quotes = database.motivationDao().getAllQuotesOnce()
        return if (quotes.isNotEmpty()) quotes.random() else null
    }

    // ==========================================
    // --- WEEKLY INTELLIGENCE REPORTS ---
    // ==========================================
    fun getLatestWeeklyReport(): Flow<WeeklyReportEntity?> = database.weeklyReportDao().getLatestReport()

    fun getAllWeeklyReports(): Flow<List<WeeklyReportEntity>> = database.weeklyReportDao().getAllReports()

    suspend fun generateWeeklyReport(weekStartDate: String, weekEndDate: String): WeeklyReportEntity {
        val existing = database.weeklyReportDao().getReportForWeek(weekStartDate)
        if (existing != null) return existing

        val start = LocalDate.parse(weekStartDate, dateFormatter)
        val end = LocalDate.parse(weekEndDate, dateFormatter)
        val daysCount = (ChronoUnit.DAYS.between(start, end) + 1).toInt().coerceAtLeast(1)

        val activeHabits = database.habitDao().getActiveHabitsOnce()
        val allLogs = mutableListOf<HabitLogEntity>()
        var cur = start
        while (!cur.isAfter(end)) {
            allLogs.addAll(database.habitDao().getLogsForDateOnce(cur.format(dateFormatter)))
            cur = cur.plusDays(1)
        }

        val totalPossible = activeHabits.size * daysCount
        val totalCompleted = allLogs.count { it.isCompleted && activeHabits.any { h -> h.id == it.habitId } }
        val overallHabitPct = if (totalPossible > 0) (totalCompleted.toFloat() / totalPossible.toFloat()) * 100f else 0f

        val habitPerformance = activeHabits.map { h ->
            val done = allLogs.count { it.habitId == h.id && it.isCompleted }
            h.title to done
        }.sortedByDescending { it.second }

        val bestHabit = habitPerformance.firstOrNull()?.first ?: "General Discipline"
        val worstHabit = habitPerformance.lastOrNull()?.first ?: "Consistency"

        val stepLogs = database.stepDao().getStepsBetweenDates(weekStartDate, weekEndDate).first()
        val avgSteps = if (stepLogs.isNotEmpty()) stepLogs.sumOf { it.stepCount } / stepLogs.size else 0

        val workoutHabits = activeHabits.filter { it.category == HabitCategory.FITNESS }.map { it.id }.toSet()
        val totalWorkouts = allLogs.count { it.isCompleted && workoutHabits.contains(it.habitId) }

        val weights = database.weightDao().getWeightsBetweenDates(weekStartDate, weekEndDate).first()
        val weightTrend = if (weights.size >= 2) weights.last().weightKg - weights.first().weightKg else null

        val skincareCount = allLogs.count { log ->
            val h = activeHabits.firstOrNull { it.id == log.habitId }
            h?.isSkincareSpecific == true && log.isCompleted
        }
        val skincareRate = if (daysCount > 0) ((skincareCount.toFloat() / (daysCount * 2f)) * 100f).coerceIn(0f, 100f) else 0f

        val startMillis = start.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = end.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val fastings = database.fastingDao().getSessionsBetweenOnce(startMillis, endMillis)
        val fastingHoursTotal = fastings.sumOf {
            if (it.endTime != null && it.endTime > it.startTime) {
                (it.endTime - it.startTime) / 3600000.0
            } else 0.0
        }.toFloat()

        val restDays = database.recoveryDao().countRestDaysBetween(weekStartDate, weekEndDate)
        val unlockedCount = database.achievementDao().getUnlockedCountOnce()

        val report = WeeklyReportEntity(
            weekStartDate = weekStartDate,
            weekEndDate = weekEndDate,
            generatedAt = System.currentTimeMillis(),
            overallHabitPercentage = overallHabitPct,
            bestHabitTitle = bestHabit,
            worstHabitTitle = worstHabit,
            averageDailySteps = avgSteps,
            totalWorkouts = totalWorkouts,
            weightTrendKg = weightTrend,
            skincareRate = skincareRate,
            fastingHoursTotal = fastingHoursTotal,
            restDaysCount = restDays,
            achievementsUnlockedCount = unlockedCount,
            summaryJson = """{"generated":true,"daysEvaluated":$daysCount}"""
        )
        database.weeklyReportDao().insertReport(report)
        return report
    }

    // ==========================================
    // --- FITNESS AI COACH ENGINE ---
    // ==========================================
    fun getLatestCoachInsight(): Flow<CoachInsightEntity?> = database.coachDao().getLatestInsight()

    fun getRecentCoachInsights(limit: Int = 14): Flow<List<CoachInsightEntity>> = database.coachDao().getRecentInsights(limit)

    suspend fun generateAdaptiveCoachInsight(allowJournal: Boolean = false): CoachInsightEntity {
        val today = getTodayString()
        val streaks = calculateStreaks()
        val daySummary = getDaySummary(today).first()
        val stepsLog = database.stepDao().getStepsForDate(today).first()
        val currentSteps = stepsLog?.stepCount ?: 0
        val recoveryToday = database.recoveryDao().getRecoveryForDateOnce(today)
        val activeFasting = database.fastingDao().getActiveSessionOnce()

        // 7-day habits history
        val last7Days = (0..6).map { LocalDate.now().minusDays(it.toLong()).format(dateFormatter) }
        val activeHabits = database.habitDao().getActiveHabitsOnce()
        var completedInLast7 = 0
        val totalPossibleInLast7 = (activeHabits.size * 7).coerceAtLeast(1)
        for (d in last7Days) {
            completedInLast7 += database.habitDao().getLogsForDateOnce(d).count { it.isCompleted && activeHabits.any { h -> h.id == it.habitId } }
        }
        val sevenDayHabitPct = (completedInLast7.toFloat() / totalPossibleInLast7.toFloat()) * 100f

        // Determine personality state
        val personalityState = when {
            recoveryToday?.perceivedEnergy == PerceivedEnergy.NEED_REST.name ||
            recoveryToday?.perceivedEnergy == PerceivedEnergy.TIRED.name ||
            (recoveryToday?.sleepHours ?: 7f) < 5.5f -> CoachPersonalityState.RECOVERY_NEEDED

            streaks.currentStreak >= 4 && daySummary.percentage >= 60f -> CoachPersonalityState.HIGH_PERFORMANCE

            sevenDayHabitPct < 40f && streaks.currentStreak <= 1 -> CoachPersonalityState.STRUGGLING

            else -> CoachPersonalityState.INCONSISTENT
        }

        val guidance = when (personalityState) {
            CoachPersonalityState.HIGH_PERFORMANCE ->
                "Outstanding consistency, JD. Your discipline is compounding. Keep up this momentum without overreaching."
            CoachPersonalityState.RECOVERY_NEEDED ->
                "Your recorded recovery metrics show your body needs rest. Prioritize sleep, light hydration, and reduced physical strain today."
            CoachPersonalityState.STRUGGLING ->
                "Discipline isn't lost in a single day. Focus on completing just one core habit today and build back momentum without guilt."
            CoachPersonalityState.INCONSISTENT ->
                "Your habits show fluctuation. Locking down fixed times for your morning and evening routines will stabilize your streak."
        }

        val journalNote = if (allowJournal) {
            val recentJournal = database.journalDao().getEntriesForDate(today).first().firstOrNull()
            if (recentJournal?.mood != null) ", journal mood: ${recentJournal.mood}" else ""
        } else ""

        val supportingData = "Supporting records: ${streaks.currentStreak}d streak, ${daySummary.completedHabits}/${daySummary.totalHabits} habits today (${daySummary.percentage.toInt()}%), ${currentSteps} steps, ${sevenDayHabitPct.toInt()}% 7-day completion${if (activeFasting != null) ", ${activeFasting.targetHours}h fast in progress" else ""}$journalNote."

        val goals = when (personalityState) {
            CoachPersonalityState.HIGH_PERFORMANCE -> listOf("Maintain 10,000 steps", "Finish evening skincare on time", "Log your recovery review")
            CoachPersonalityState.RECOVERY_NEEDED -> listOf("Sleep at least 7.5 hours tonight", "Perform 10 minutes light stretching", "Hydrate with 2+ liters of water")
            CoachPersonalityState.STRUGGLING -> listOf("Complete 1-hour study block", "Drink clean water, avoid sugar", "Hit at least 5,000 steps")
            CoachPersonalityState.INCONSISTENT -> listOf("Lock morning routine before 08:30", "Complete home workout session", "Check in all 13 habits tonight")
        }

        val goalsJson = "[${goals.joinToString(",") { "\"$it\"" }}]"

        val insight = CoachInsightEntity(
            date = today,
            timestamp = System.currentTimeMillis(),
            personalityState = personalityState.name,
            guidanceText = guidance,
            supportingDataSummary = supportingData,
            isAiGenerated = false,
            actionableGoals = goalsJson
        )

        database.coachDao().deleteForDate(today)
        database.coachDao().insertInsight(insight)
        return insight
    }
}


