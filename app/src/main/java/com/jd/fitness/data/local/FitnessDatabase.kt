package com.jd.fitness.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jd.fitness.data.local.dao.AchievementDao
import com.jd.fitness.data.local.dao.ChallengeDao
import com.jd.fitness.data.local.dao.CoachDao
import com.jd.fitness.data.local.dao.FastingDao
import com.jd.fitness.data.local.dao.HabitDao
import com.jd.fitness.data.local.dao.JournalDao
import com.jd.fitness.data.local.dao.MotivationDao
import com.jd.fitness.data.local.dao.PlannerDao
import com.jd.fitness.data.local.dao.RecoveryDao
import com.jd.fitness.data.local.dao.SkincareDao
import com.jd.fitness.data.local.dao.StepDao
import com.jd.fitness.data.local.dao.WeeklyReportDao
import com.jd.fitness.data.local.dao.WeightDao
import com.jd.fitness.data.local.entity.AchievementCategory
import com.jd.fitness.data.local.entity.AchievementEntity
import com.jd.fitness.data.local.entity.ChallengeEntity
import com.jd.fitness.data.local.entity.CoachInsightEntity
import com.jd.fitness.data.local.entity.FastingEntity
import com.jd.fitness.data.local.entity.FastingMealEntity
import com.jd.fitness.data.local.entity.FastingScheduleEntity
import com.jd.fitness.data.local.entity.HabitCategory
import com.jd.fitness.data.local.entity.HabitEntity
import com.jd.fitness.data.local.entity.HabitLogEntity
import com.jd.fitness.data.local.entity.JournalEntity
import com.jd.fitness.data.local.entity.MotivationCategory
import com.jd.fitness.data.local.entity.MotivationEntity
import com.jd.fitness.data.local.entity.PlannerTaskEntity
import com.jd.fitness.data.local.entity.RecoveryEntity
import com.jd.fitness.data.local.entity.SkincareLogEntity
import com.jd.fitness.data.local.entity.SkincareRoutineEntity
import com.jd.fitness.data.local.entity.SkincareRoutineType
import com.jd.fitness.data.local.entity.StepLogEntity
import com.jd.fitness.data.local.entity.WeeklyReportEntity
import com.jd.fitness.data.local.entity.WeightEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChallengeEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        WeightEntity::class,
        SkincareRoutineEntity::class,
        SkincareLogEntity::class,
        FastingEntity::class,
        FastingMealEntity::class,
        FastingScheduleEntity::class,
        StepLogEntity::class,
        RecoveryEntity::class,
        PlannerTaskEntity::class,
        AchievementEntity::class,
        WeeklyReportEntity::class,
        JournalEntity::class,
        CoachInsightEntity::class,
        MotivationEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FitnessDatabase : RoomDatabase() {

    abstract fun challengeDao(): ChallengeDao
    abstract fun habitDao(): HabitDao
    abstract fun weightDao(): WeightDao
    abstract fun skincareDao(): SkincareDao
    abstract fun fastingDao(): FastingDao
    abstract fun stepDao(): StepDao
    abstract fun recoveryDao(): RecoveryDao
    abstract fun plannerDao(): PlannerDao
    abstract fun achievementDao(): AchievementDao
    abstract fun weeklyReportDao(): WeeklyReportDao
    abstract fun journalDao(): JournalDao
    abstract fun coachDao(): CoachDao
    abstract fun motivationDao(): MotivationDao

    companion object {
        @Volatile
        private var INSTANCE: FitnessDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Safe migration preserving all existing tables & data

                // 1. Alter fasting_sessions
                db.execSQL("ALTER TABLE fasting_sessions ADD COLUMN plannedStartTime INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE fasting_sessions ADD COLUMN plannedEndTime INTEGER")

                // 2. Create fasting_schedules
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS fasting_schedules (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        fastingHours INTEGER NOT NULL,
                        eatingHours INTEGER NOT NULL,
                        defaultStartTime TEXT NOT NULL,
                        isDefault INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                // 3. Create recovery_entries
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS recovery_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        isRestDay INTEGER NOT NULL DEFAULT 0,
                        sleepHours REAL NOT NULL DEFAULT 7.0,
                        perceivedEnergy TEXT NOT NULL DEFAULT 'NORMAL',
                        workoutDone INTEGER NOT NULL DEFAULT 0,
                        workoutType TEXT NOT NULL DEFAULT 'REST',
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_recovery_entries_date ON recovery_entries(date)")

                // 4. Create planner_tasks
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS planner_tasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        title TEXT NOT NULL,
                        category TEXT NOT NULL,
                        startTime TEXT NOT NULL,
                        endTime TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        priority INTEGER NOT NULL DEFAULT 2,
                        orderIndex INTEGER NOT NULL DEFAULT 0,
                        reminderEnabled INTEGER NOT NULL DEFAULT 0,
                        notes TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_planner_tasks_date ON planner_tasks(date)")

                // 5. Create achievement_records
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS achievement_records (
                        id TEXT PRIMARY KEY NOT NULL,
                        category TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        isUnlocked INTEGER NOT NULL DEFAULT 0,
                        unlockedAt INTEGER,
                        currentProgress INTEGER NOT NULL DEFAULT 0,
                        maxProgress INTEGER NOT NULL DEFAULT 1
                    )
                """.trimIndent())

                // 6. Create weekly_reports
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS weekly_reports (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        weekStartDate TEXT NOT NULL,
                        weekEndDate TEXT NOT NULL,
                        generatedAt INTEGER NOT NULL,
                        overallHabitPercentage REAL NOT NULL,
                        bestHabitTitle TEXT NOT NULL,
                        worstHabitTitle TEXT NOT NULL,
                        averageDailySteps INTEGER NOT NULL,
                        totalWorkouts INTEGER NOT NULL,
                        weightTrendKg REAL,
                        skincareRate REAL NOT NULL,
                        fastingHoursTotal REAL NOT NULL,
                        restDaysCount INTEGER NOT NULL,
                        achievementsUnlockedCount INTEGER NOT NULL,
                        summaryJson TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_weekly_reports_weekStartDate ON weekly_reports(weekStartDate)")

                // 7. Create journal_entries
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS journal_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        mood TEXT NOT NULL,
                        accomplishments TEXT NOT NULL DEFAULT '',
                        challenges TEXT NOT NULL DEFAULT '',
                        lessonsLearned TEXT NOT NULL DEFAULT '',
                        tomorrowGoals TEXT NOT NULL DEFAULT '',
                        allowAiInsights INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_date ON journal_entries(date)")

                // 8. Create coach_insights
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS coach_insights (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        personalityState TEXT NOT NULL,
                        guidanceText TEXT NOT NULL,
                        supportingDataSummary TEXT NOT NULL,
                        isAiGenerated INTEGER NOT NULL DEFAULT 0,
                        actionableGoals TEXT NOT NULL DEFAULT '[]'
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_coach_insights_date ON coach_insights(date)")

                // 9. Create saved_motivation
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS saved_motivation (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        quote TEXT NOT NULL,
                        category TEXT NOT NULL,
                        isFavorite INTEGER NOT NULL DEFAULT 0,
                        savedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): FitnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitnessDatabase::class.java,
                    "fitness_database.db"
                )
                .addMigrations(MIGRATION_1_2)
                .addCallback(FitnessDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class FitnessDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    prepopulateDatabase(database)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    seedDefaultSchedulesAndAchievementsIfEmpty(database)
                }
            }
        }

        private suspend fun prepopulateDatabase(database: FitnessDatabase) {
            // 1. Initial 90-Day Challenge Configuration
            database.challengeDao().insertOrUpdate(
                ChallengeEntity(
                    id = 1,
                    title = "FITNESS 1.0 — THE BEGINNING",
                    startDate = "2026-11-03",
                    endDate = "2027-01-31",
                    totalDays = 90,
                    milestone1Day = 30,
                    milestone1Title = "FOUNDATION",
                    milestone2Day = 60,
                    milestone2Title = "MOMENTUM",
                    milestone3Day = 90,
                    milestone3Title = "TRANSFORMATION",
                    brandInitials = "JD"
                )
            )

            // 2. The Original 13 Habits
            val originalHabits = listOf(
                HabitEntity(
                    id = 1,
                    title = "Brush teeth daily",
                    category = HabitCategory.PERSONAL_CARE,
                    description = "Maintain oral hygiene every morning and night",
                    orderIndex = 1,
                    isDefault = true
                ),
                HabitEntity(
                    id = 2,
                    title = "Bath daily",
                    category = HabitCategory.PERSONAL_CARE,
                    description = "Fresh daily cold/warm shower",
                    orderIndex = 2,
                    isDefault = true
                ),
                HabitEntity(
                    id = 3,
                    title = "Study at home — 1 hour",
                    category = HabitCategory.STUDY,
                    description = "Focused, distraction-free study session",
                    orderIndex = 3,
                    isDefault = true
                ),
                HabitEntity(
                    id = 4,
                    title = "Morning skincare",
                    category = HabitCategory.PERSONAL_CARE,
                    description = "Face wash, moisturizer, sunscreen",
                    orderIndex = 4,
                    isDefault = true,
                    isSkincareSpecific = true
                ),
                HabitEntity(
                    id = 5,
                    title = "Night skincare",
                    category = HabitCategory.PERSONAL_CARE,
                    description = "Cleanser, moisturizer, treatment routine",
                    orderIndex = 5,
                    isDefault = true,
                    isSkincareSpecific = true
                ),
                HabitEntity(
                    id = 6,
                    title = "Avoid junk and processed food",
                    category = HabitCategory.NUTRITION,
                    description = "Only wholesome whole foods, no fast food",
                    orderIndex = 6,
                    isDefault = true
                ),
                HabitEntity(
                    id = 7,
                    title = "Home workout — once",
                    category = HabitCategory.FITNESS,
                    description = "Calisthenics, weights or high-intensity bodyweight session",
                    orderIndex = 7,
                    isDefault = true
                ),
                HabitEntity(
                    id = 8,
                    title = "No added sugar",
                    category = HabitCategory.NUTRITION,
                    description = "Zero sugary drinks, desserts, or hidden sugars",
                    orderIndex = 8,
                    isDefault = true
                ),
                HabitEntity(
                    id = 9,
                    title = "10,000 steps",
                    category = HabitCategory.FITNESS,
                    description = "Daily movement tracked automatically via Health Connect",
                    orderIndex = 9,
                    isDefault = true
                ),
                HabitEntity(
                    id = 10,
                    title = "No pornography",
                    category = HabitCategory.MINDSET,
                    description = "Mental clarity, dopamine discipline and self-mastery",
                    orderIndex = 10,
                    isDefault = true
                ),
                HabitEntity(
                    id = 11,
                    title = "No smoking",
                    category = HabitCategory.MINDSET,
                    description = "Clean respiratory health and discipline",
                    orderIndex = 11,
                    isDefault = true
                ),
                HabitEntity(
                    id = 12,
                    title = "No alcohol",
                    category = HabitCategory.MINDSET,
                    description = "Zero alcohol consumption for optimal recovery",
                    orderIndex = 12,
                    isDefault = true
                ),
                HabitEntity(
                    id = 13,
                    title = "Personal discipline goal",
                    category = HabitCategory.MINDSET,
                    description = "Private customized rule or personal boundary",
                    orderIndex = 13,
                    isDefault = true,
                    isPrivate = true
                )
            )
            database.habitDao().insertHabits(originalHabits)

            // 3. Default Skincare Steps
            val skincareSteps = listOf(
                SkincareRoutineEntity(
                    routineType = SkincareRoutineType.MORNING,
                    stepOrder = 1,
                    stepTitle = "Face Wash",
                    defaultProduct = "Gentle Foaming Cleanser"
                ),
                SkincareRoutineEntity(
                    routineType = SkincareRoutineType.MORNING,
                    stepOrder = 2,
                    stepTitle = "Moisturizer",
                    defaultProduct = "Hydrating Gel Cream"
                ),
                SkincareRoutineEntity(
                    routineType = SkincareRoutineType.MORNING,
                    stepOrder = 3,
                    stepTitle = "Sunscreen",
                    defaultProduct = "SPF 50+ Broad Spectrum"
                ),
                SkincareRoutineEntity(
                    routineType = SkincareRoutineType.NIGHT,
                    stepOrder = 1,
                    stepTitle = "Face Wash",
                    defaultProduct = "Hydrating Cleanser"
                ),
                SkincareRoutineEntity(
                    routineType = SkincareRoutineType.NIGHT,
                    stepOrder = 2,
                    stepTitle = "Moisturizer",
                    defaultProduct = "Night Repair Cream"
                ),
                SkincareRoutineEntity(
                    routineType = SkincareRoutineType.NIGHT,
                    stepOrder = 3,
                    stepTitle = "Night Treatment",
                    defaultProduct = "Serum / Spot Treatment"
                )
            )
            database.skincareDao().insertRoutineSteps(skincareSteps)

            seedDefaultSchedulesAndAchievementsIfEmpty(database)
        }

        private suspend fun seedDefaultSchedulesAndAchievementsIfEmpty(database: FitnessDatabase) {
            // Seed default fasting schedules if none exist
            val existingSchedules = database.fastingDao().getAllSchedulesOnce()
            if (existingSchedules.isEmpty()) {
                val defaultSchedules = listOf(
                    FastingScheduleEntity(name = "16:8 Standard", fastingHours = 16, eatingHours = 8, defaultStartTime = "20:00", isDefault = true),
                    FastingScheduleEntity(name = "14:10 Gentle", fastingHours = 14, eatingHours = 10, defaultStartTime = "20:00", isDefault = false),
                    FastingScheduleEntity(name = "18:6 Deep Fast", fastingHours = 18, eatingHours = 6, defaultStartTime = "20:00", isDefault = false),
                    FastingScheduleEntity(name = "12:12 Circadian", fastingHours = 12, eatingHours = 12, defaultStartTime = "20:00", isDefault = false),
                    FastingScheduleEntity(name = "23:1 OMAD", fastingHours = 23, eatingHours = 1, defaultStartTime = "19:00", isDefault = false)
                )
                database.fastingDao().insertSchedules(defaultSchedules)
            }

            // Seed default achievements if none exist
            val existingAchievements = database.achievementDao().getAllAchievementsOnce()
            if (existingAchievements.isEmpty()) {
                val defaultAchievements = listOf(
                    // Habits
                    AchievementEntity(id = "habit_first_day", category = AchievementCategory.HABIT.name, title = "First Step", description = "Complete all habits for your first full day", maxProgress = 1),
                    AchievementEntity(id = "habit_7d", category = AchievementCategory.HABIT.name, title = "7-Day Discipline", description = "Maintain a 7-day continuous habit streak", maxProgress = 7),
                    AchievementEntity(id = "habit_14d", category = AchievementCategory.HABIT.name, title = "Fortnight Fortress", description = "Maintain a 14-day continuous habit streak", maxProgress = 14),
                    AchievementEntity(id = "habit_30d", category = AchievementCategory.HABIT.name, title = "Foundation Master", description = "Maintain a 30-day continuous habit streak", maxProgress = 30),
                    AchievementEntity(id = "habit_60d", category = AchievementCategory.HABIT.name, title = "Unstoppable Momentum", description = "Maintain a 60-day continuous habit streak", maxProgress = 60),
                    AchievementEntity(id = "habit_90d", category = AchievementCategory.HABIT.name, title = "Transformation Legend", description = "Complete the full 90-day challenge consistency", maxProgress = 90),

                    // Fitness
                    AchievementEntity(id = "fit_first_workout", category = AchievementCategory.FITNESS.name, title = "Iron Starter", description = "Complete and log your first workout", maxProgress = 1),
                    AchievementEntity(id = "fit_10_workouts", category = AchievementCategory.FITNESS.name, title = "Dedicated Athlete", description = "Log 10 completed workouts", maxProgress = 10),
                    AchievementEntity(id = "fit_10k_steps_day", category = AchievementCategory.FITNESS.name, title = "10K Club", description = "Reach 10,000 steps in a single day", maxProgress = 10000),
                    AchievementEntity(id = "fit_7d_steps", category = AchievementCategory.FITNESS.name, title = "Relentless Walker", description = "Hit 10,000 steps 7 days in a row", maxProgress = 7),
                    AchievementEntity(id = "fit_weight_logged", category = AchievementCategory.FITNESS.name, title = "Accountable Scale", description = "Log body weight consistently for 7 distinct days", maxProgress = 7),

                    // Productivity
                    AchievementEntity(id = "prod_7_study", category = AchievementCategory.PRODUCTIVITY.name, title = "Scholar's Focus", description = "Complete 7 focused study sessions", maxProgress = 7),
                    AchievementEntity(id = "prod_30_study", category = AchievementCategory.PRODUCTIVITY.name, title = "Deep Mastery", description = "Complete 30 focused study sessions", maxProgress = 30),
                    AchievementEntity(id = "prod_morning_routine", category = AchievementCategory.PRODUCTIVITY.name, title = "Dawn Commander", description = "Complete morning skincare and routine 7 days in a row", maxProgress = 7),
                    AchievementEntity(id = "prod_evening_routine", category = AchievementCategory.PRODUCTIVITY.name, title = "Night Guardian", description = "Complete night skincare and review 7 days in a row", maxProgress = 7)
                )
                database.achievementDao().insertDefaultAchievements(defaultAchievements)
            }

            // Seed initial motivation quotes if empty
            val existingQuotes = database.motivationDao().getAllQuotesOnce()
            if (existingQuotes.isEmpty()) {
                val defaultQuotes = listOf(
                    MotivationEntity(quote = "Discipline is doing what must be done, even when motivation has long gone.", category = MotivationCategory.DISCIPLINE.name),
                    MotivationEntity(quote = "Consistency isn't about perfection; it is about never giving up on the standard you set.", category = MotivationCategory.CONSISTENCY.name),
                    MotivationEntity(quote = "Your body can stand almost anything; it is your mind that you must convince.", category = MotivationCategory.FITNESS.name),
                    MotivationEntity(quote = "Self-control is strength. Right thought is mastery. Calmness is power.", category = MotivationCategory.SELF_CONTROL.name),
                    MotivationEntity(quote = "Confidence is not having all the answers. It is knowing you can endure finding them.", category = MotivationCategory.CONFIDENCE.name),
                    MotivationEntity(quote = "Small disciplines repeated with consistency every day lead to great achievements gained slowly over time.", category = MotivationCategory.PERSONAL_GROWTH.name),
                    MotivationEntity(quote = "Focus on the hour in front of you. Finish the task, preserve your energy, stay committed.", category = MotivationCategory.PRODUCTIVITY.name),
                    MotivationEntity(quote = "Strength does not come from physical capacity. It comes from an indomitable will.", category = MotivationCategory.MENTAL_STRENGTH.name)
                )
                database.motivationDao().insertQuotes(defaultQuotes)
            }
        }
    }
}
