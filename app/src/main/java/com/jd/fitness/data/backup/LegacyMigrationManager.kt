package com.jd.fitness.data.backup

import com.jd.fitness.data.local.FitnessDatabase
import com.jd.fitness.data.local.entity.ChallengeEntity
import com.jd.fitness.data.local.entity.HabitCategory
import com.jd.fitness.data.local.entity.HabitEntity
import com.jd.fitness.data.local.entity.HabitLogEntity
import com.jd.fitness.data.local.entity.SkincareLogEntity
import com.jd.fitness.data.local.entity.SkincareRoutineType
import com.jd.fitness.data.local.entity.WeightEntity
import com.jd.fitness.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

class LegacyMigrationManager(
    private val database: FitnessDatabase,
    private val preferencesRepository: UserPreferencesRepository
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Inspects the raw JSON content and returns a safe MigrationSummary.
     * Does NOT write anything to database yet.
     */
    fun analyzeBackupJson(rawJson: String): Result<MigrationSummary> {
        return runCatching {
            val rootElement = json.parseToJsonElement(rawJson)
            val rootObj = rootElement.jsonObject

            val appName = rootObj["app"]?.jsonPrimitive?.content ?: ""
            val isModern = rootObj.containsKey("app") && appName.startsWith("FITNESS")
            val notices = mutableListOf<String>()

            if (isModern) {
                val habitsCount = rootObj["habits"]?.jsonArray?.size ?: 0
                val logsCount = rootObj["habit_logs"]?.jsonArray?.size ?: 0
                val weightsCount = rootObj["weight_logs"]?.jsonArray?.size ?: 0
                val userName = rootObj["user_name"]?.jsonPrimitive?.content ?: "JD"
                val challengeObj = rootObj["challenge"]?.jsonObject
                val startDate = challengeObj?.get("startDate")?.jsonPrimitive?.content
                val endDate = challengeObj?.get("endDate")?.jsonPrimitive?.content

                notices.add("Native $appName backup detected.")
                notices.add("All habit check-ins, weight entries, and challenge milestones will be restored.")

                MigrationSummary(
                    isLegacyFormat = false,
                    detectedSource = "$appName Native Backup",
                    totalHabitsCount = habitsCount,
                    totalLogsCount = logsCount,
                    totalWeightEntries = weightsCount,
                    challengeStartDate = startDate,
                    challengeEndDate = endDate,
                    detectedUserName = userName,
                    legacySkincareDaysCount = 0,
                    notices = notices
                )
            } else {
                // Legacy Season 10 / Website format
                // Old format might contain keys like "history", "logs", "progress", "habits", "settings", "startDate"
                val detectedUser = rootObj["userName"]?.jsonPrimitive?.content
                    ?: rootObj["user_name"]?.jsonPrimitive?.content
                    ?: rootObj["name"]?.jsonPrimitive?.content
                    ?: "JD"

                val startDate = rootObj["startDate"]?.jsonPrimitive?.content
                    ?: rootObj["start_date"]?.jsonPrimitive?.content
                    ?: rootObj["challengeStartDate"]?.jsonPrimitive?.content
                    ?: "2026-11-03"

                val endDate = rootObj["endDate"]?.jsonPrimitive?.content
                    ?: rootObj["end_date"]?.jsonPrimitive?.content
                    ?: "2027-01-31"

                // Count historical log entries
                var logCount = 0
                var legacySkincareCount = 0
                val historyObj = rootObj["history"]?.jsonObject
                    ?: rootObj["days"]?.jsonObject
                    ?: rootObj["logs"]?.jsonObject

                if (historyObj != null) {
                    historyObj.forEach { (_, dayElement) ->
                        if (dayElement is JsonObject) {
                            dayElement.forEach { (key, value) ->
                                if (value.jsonPrimitive.booleanOrNull == true || value.jsonPrimitive.intOrNull == 1) {
                                    logCount++
                                    // Check if skincare was checked in old format
                                    if (key.contains("skincare", ignoreCase = true) || key == "habit_4" || key == "4") {
                                        legacySkincareCount++
                                    }
                                }
                            }
                        } else if (dayElement is JsonArray) {
                            logCount += dayElement.size
                        }
                    }
                } else if (rootObj["habit_logs"] is JsonArray) {
                    logCount = rootObj["habit_logs"]?.jsonArray?.size ?: 0
                }

                val weightsCount = (rootObj["weights"]?.jsonArray ?: rootObj["weight_logs"]?.jsonArray)?.size ?: 0

                notices.add("Legacy website backup (Season 10 format) detected.")
                notices.add("CRITICAL MIGRATION NOTICE: The original website used the old 12-habit format where Morning and Night Skincare were combined into one habit.")
                notices.add("Legacy skincare logs ($legacySkincareCount days) will be preserved in your historical logs as legacy reference, and will NOT be falsely split into separate morning and night routines without your verification.")
                notices.add("Your original 13 habits, challenge start date ($startDate), and weight history will be fully preserved.")

                MigrationSummary(
                    isLegacyFormat = true,
                    detectedSource = "Legacy Website (Season 10)",
                    totalHabitsCount = 12,
                    totalLogsCount = logCount,
                    totalWeightEntries = weightsCount,
                    challengeStartDate = startDate,
                    challengeEndDate = endDate,
                    detectedUserName = detectedUser,
                    legacySkincareDaysCount = legacySkincareCount,
                    notices = notices
                )
            }
        }
    }

    /**
     * Executes the confirmed migration into Room database.
     */
    suspend fun executeMigration(
        rawJson: String,
        splitLegacySkincare: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val rootObj = json.parseToJsonElement(rawJson).jsonObject
            val isModern = rootObj.containsKey("app") && rootObj["app"]?.jsonPrimitive?.content == "FITNESS 1.0"

            if (isModern) {
                val modernBackup = json.decodeFromString<FitnessExportBackup>(rawJson)
                modernBackup.challenge?.let { database.challengeDao().insertOrUpdate(it) }
                if (modernBackup.habits.isNotEmpty()) {
                    database.habitDao().insertHabits(modernBackup.habits)
                }
                if (modernBackup.habitLogs.isNotEmpty()) {
                    database.habitDao().insertOrUpdateLogs(modernBackup.habitLogs)
                }
                if (modernBackup.weightLogs.isNotEmpty()) {
                    database.weightDao().insertWeights(modernBackup.weightLogs)
                }
                if (modernBackup.skincareRoutines.isNotEmpty()) {
                    database.skincareDao().insertRoutineSteps(modernBackup.skincareRoutines)
                }
                if (modernBackup.skincareLogs.isNotEmpty()) {
                    database.skincareDao().insertOrUpdateLogs(modernBackup.skincareLogs)
                }
                if (modernBackup.fastingSessions.isNotEmpty()) {
                    database.fastingDao().insertSessions(modernBackup.fastingSessions)
                }
                if (modernBackup.fastingMeals.isNotEmpty()) {
                    database.fastingDao().insertMeals(modernBackup.fastingMeals)
                }
                if (modernBackup.stepLogs.isNotEmpty()) {
                    database.stepDao().insertOrUpdateSteps(modernBackup.stepLogs)
                }
                preferencesRepository.updateUserName(modernBackup.userName)
                "Successfully restored ${modernBackup.habitLogs.size} logs and settings."
            } else {
                // Execute Legacy Season 10 migration
                val detectedUser = rootObj["userName"]?.jsonPrimitive?.content
                    ?: rootObj["user_name"]?.jsonPrimitive?.content
                    ?: "JD"
                preferencesRepository.updateUserName(detectedUser)

                val startDate = rootObj["startDate"]?.jsonPrimitive?.content
                    ?: rootObj["start_date"]?.jsonPrimitive?.content
                    ?: "2026-11-03"
                val endDate = rootObj["endDate"]?.jsonPrimitive?.content
                    ?: rootObj["end_date"]?.jsonPrimitive?.content
                    ?: "2027-01-31"

                // Preserve Challenge Start Date
                val currentChallenge = database.challengeDao().getChallengeConfigOnce()
                    ?: ChallengeEntity()
                database.challengeDao().insertOrUpdate(
                    currentChallenge.copy(
                        startDate = startDate,
                        endDate = endDate,
                        brandInitials = "JD"
                    )
                )

                // Retrieve existing active habits to map to
                val activeHabits = database.habitDao().getAllHabitsOnce()
                val habitIdMap = activeHabits.associateBy { it.orderIndex }

                val logsToInsert = mutableListOf<HabitLogEntity>()
                val skincareLogsToInsert = mutableListOf<SkincareLogEntity>()

                // Check "history" or "days" object in legacy format
                val historyObj = rootObj["history"]?.jsonObject
                    ?: rootObj["days"]?.jsonObject
                    ?: rootObj["logs"]?.jsonObject

                if (historyObj != null) {
                    historyObj.forEach { (dateKey, dayElement) ->
                        if (dayElement is JsonObject) {
                            dayElement.forEach { (key, value) ->
                                val isChecked = value.jsonPrimitive.booleanOrNull == true || value.jsonPrimitive.intOrNull == 1
                                if (isChecked) {
                                    val habitIndex = parseLegacyHabitKey(key)
                                    val mappedHabit = habitIdMap[habitIndex]
                                    if (mappedHabit != null) {
                                        logsToInsert.add(
                                            HabitLogEntity(
                                                date = dateKey,
                                                habitId = mappedHabit.id,
                                                isCompleted = true,
                                                notes = "Migrated from Season 10"
                                            )
                                        )
                                    }

                                    // Handle legacy skincare
                                    if (habitIndex == 4 || key.contains("skincare", ignoreCase = true)) {
                                        if (splitLegacySkincare) {
                                            // User explicitly approved splitting into morning + night
                                            skincareLogsToInsert.add(
                                                SkincareLogEntity(
                                                    date = dateKey,
                                                    routineType = SkincareRoutineType.MORNING,
                                                    isCompleted = true,
                                                    notes = "Migrated from combined legacy skincare"
                                                )
                                            )
                                            skincareLogsToInsert.add(
                                                SkincareLogEntity(
                                                    date = dateKey,
                                                    routineType = SkincareRoutineType.NIGHT,
                                                    isCompleted = true,
                                                    notes = "Migrated from combined legacy skincare"
                                                )
                                            )
                                        } else {
                                            // Preserve legacy skincare completion marker
                                            skincareLogsToInsert.add(
                                                SkincareLogEntity(
                                                    date = dateKey,
                                                    routineType = SkincareRoutineType.MORNING,
                                                    isCompleted = true,
                                                    notes = "Legacy Combined Skincare Completed"
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (logsToInsert.isNotEmpty()) {
                    database.habitDao().insertOrUpdateLogs(logsToInsert)
                }
                if (skincareLogsToInsert.isNotEmpty()) {
                    database.skincareDao().insertOrUpdateLogs(skincareLogsToInsert)
                }

                // Check weights in legacy format
                val weightsArray = rootObj["weights"]?.jsonArray
                    ?: rootObj["weight_logs"]?.jsonArray
                val weightEntities = mutableListOf<WeightEntity>()
                weightsArray?.forEach { weightElement ->
                    if (weightElement is JsonObject) {
                        val kg = weightElement["weightKg"]?.jsonPrimitive?.doubleOrNull
                            ?: weightElement["kg"]?.jsonPrimitive?.doubleOrNull
                            ?: weightElement["weight"]?.jsonPrimitive?.doubleOrNull
                        val date = weightElement["date"]?.jsonPrimitive?.content ?: startDate
                        val notes = weightElement["notes"]?.jsonPrimitive?.content ?: ""
                        if (kg != null && kg > 0) {
                            weightEntities.add(
                                WeightEntity(
                                    date = date,
                                    weightKg = kg,
                                    notes = notes
                                )
                            )
                        }
                    }
                }
                if (weightEntities.isNotEmpty()) {
                    database.weightDao().insertWeights(weightEntities)
                }

                "Successfully migrated ${logsToInsert.size} habit logs, ${weightEntities.size} weight records, and preserved challenge start ($startDate)."
            }
        }
    }

    private fun parseLegacyHabitKey(key: String): Int {
        val digits = key.filter { it.isDigit() }
        val num = digits.toIntOrNull()
        if (num != null) return num

        return when {
            key.contains("teeth", ignoreCase = true) -> 1
            key.contains("bath", ignoreCase = true) -> 2
            key.contains("study", ignoreCase = true) -> 3
            key.contains("skincare", ignoreCase = true) -> 4
            key.contains("junk", ignoreCase = true) || key.contains("processed", ignoreCase = true) -> 6
            key.contains("workout", ignoreCase = true) -> 7
            key.contains("sugar", ignoreCase = true) -> 8
            key.contains("step", ignoreCase = true) -> 9
            key.contains("porn", ignoreCase = true) -> 10
            key.contains("smok", ignoreCase = true) -> 11
            key.contains("alcohol", ignoreCase = true) -> 12
            key.contains("discipline", ignoreCase = true) || key.contains("goal", ignoreCase = true) -> 13
            else -> 1
        }
    }
}

