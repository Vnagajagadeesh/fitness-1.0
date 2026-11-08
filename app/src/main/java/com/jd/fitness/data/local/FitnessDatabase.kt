package com.jd.fitness.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jd.fitness.data.local.dao.ChallengeDao
import com.jd.fitness.data.local.dao.FastingDao
import com.jd.fitness.data.local.dao.HabitDao
import com.jd.fitness.data.local.dao.SkincareDao
import com.jd.fitness.data.local.dao.StepDao
import com.jd.fitness.data.local.dao.WeightDao
import com.jd.fitness.data.local.entity.ChallengeEntity
import com.jd.fitness.data.local.entity.FastingEntity
import com.jd.fitness.data.local.entity.FastingMealEntity
import com.jd.fitness.data.local.entity.HabitCategory
import com.jd.fitness.data.local.entity.HabitEntity
import com.jd.fitness.data.local.entity.HabitLogEntity
import com.jd.fitness.data.local.entity.SkincareLogEntity
import com.jd.fitness.data.local.entity.SkincareRoutineEntity
import com.jd.fitness.data.local.entity.SkincareRoutineType
import com.jd.fitness.data.local.entity.StepLogEntity
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
        StepLogEntity::class
    ],
    version = 1,
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

    companion object {
        @Volatile
        private var INSTANCE: FitnessDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): FitnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitnessDatabase::class.java,
                    "fitness_database.db"
                )
                .addCallback(FitnessDatabaseCallback(scope))
                .fallbackToDestructiveMigration()
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
                // Morning
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
                // Night
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
        }
    }
}

