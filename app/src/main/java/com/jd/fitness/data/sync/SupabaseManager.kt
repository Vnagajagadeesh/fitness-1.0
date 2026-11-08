package com.jd.fitness.data.sync

import com.jd.fitness.data.local.FitnessDatabase
import com.jd.fitness.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class SupabaseManager(
    private val database: FitnessDatabase,
    private val preferencesRepository: UserPreferencesRepository
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    /**
     * Checks if cloud sync credentials are configured.
     */
    suspend fun isConfigured(): Boolean {
        val prefs = preferencesRepository.preferencesFlow.first()
        return prefs.supabaseUrl.isNotBlank() && prefs.supabaseAnonKey.isNotBlank()
    }

    /**
     * Builds complete snapshot payload from local Room database.
     */
    suspend fun buildLocalPayload(userId: String): SyncPayload = withContext(Dispatchers.IO) {
        SyncPayload(
            version = 1,
            userId = userId,
            deviceInfo = "POCO X7 Pro - FITNESS 1.0",
            updatedAt = System.currentTimeMillis(),
            challenge = database.challengeDao().getChallengeConfigOnce(),
            habits = database.habitDao().getAllHabitsOnce(),
            habitLogs = database.habitDao().getAllLogs(),
            weightLogs = database.weightDao().getAllWeightLogsOnce(),
            skincareRoutines = database.skincareDao().getAllRoutinesOnce(),
            skincareLogs = database.skincareDao().getAllLogs(),
            fastingSessions = database.fastingDao().getAllSessionsOnce(),
            fastingMeals = database.fastingDao().getAllMeals(),
            stepLogs = database.stepDao().getAllSteps()
        )
    }

    /**
     * Uploads local progress to Supabase table `season10_progress`.
     * Preserves existing tables and never exposes secret keys.
     */
    suspend fun backupToCloud(): SyncResult = withContext(Dispatchers.IO) {
        val prefs = preferencesRepository.preferencesFlow.first()
        if (prefs.supabaseUrl.isBlank() || prefs.supabaseAnonKey.isBlank()) {
            return@withContext SyncResult.Error("Supabase URL or Anon Key is missing. Configure in Settings.")
        }

        val baseUrl = prefs.supabaseUrl.trimEnd('/')
        val anonKey = prefs.supabaseAnonKey.trim()
        val userId = prefs.userName.ifBlank { "JD" }

        try {
            val localPayload = buildLocalPayload(userId)
            val payloadJsonString = json.encodeToString(localPayload)

            val endpoint = "$baseUrl/rest/v1/season10_progress"
            val bodyJson = """[{"user_id":"$userId","data":${json.encodeToString(payloadJsonString)},"updated_at":"${System.currentTimeMillis()}"}]"""

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(bodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val now = System.currentTimeMillis()
                preferencesRepository.updateLastSyncTime(now)
                SyncResult.Success("Cloud backup successful to table season10_progress", now)
            } else {
                SyncResult.Error("Cloud error (${response.code}): ${response.message}")
            }
        } catch (e: Exception) {
            SyncResult.Error("Network error during sync: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Restores progress from Supabase table `season10_progress`.
     */
    suspend fun restoreFromCloud(): SyncResult = withContext(Dispatchers.IO) {
        val prefs = preferencesRepository.preferencesFlow.first()
        if (prefs.supabaseUrl.isBlank() || prefs.supabaseAnonKey.isBlank()) {
            return@withContext SyncResult.Error("Supabase URL or Anon Key is missing. Configure in Settings.")
        }

        val baseUrl = prefs.supabaseUrl.trimEnd('/')
        val anonKey = prefs.supabaseAnonKey.trim()
        val userId = prefs.userName.ifBlank { "JD" }

        try {
            val endpoint = "$baseUrl/rest/v1/season10_progress?user_id=eq.$userId&select=*&order=updated_at.desc&limit=1"
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Accept", "application/json")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext SyncResult.Error("Cloud fetch failed (${response.code}): ${response.message}")
            }

            val body = response.body?.string() ?: ""
            if (body.isBlank() || body == "[]") {
                return@withContext SyncResult.Error("No existing backup found on cloud for user $userId.")
            }

            val rows = json.decodeFromString<List<Season10CloudRow>>(body)
            val latestRow = rows.firstOrNull() ?: return@withContext SyncResult.Error("Empty record returned from cloud.")

            val payload = json.decodeFromString<SyncPayload>(latestRow.data)
            applyPayloadToDatabase(payload)

            val now = System.currentTimeMillis()
            preferencesRepository.updateLastSyncTime(now)
            SyncResult.Success("Successfully restored ${payload.habits.size} habits & ${payload.habitLogs.size} logs from cloud.", now)
        } catch (e: Exception) {
            SyncResult.Error("Error parsing cloud restore: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private suspend fun applyPayloadToDatabase(payload: SyncPayload) {
        payload.challenge?.let { database.challengeDao().insertOrUpdate(it) }
        if (payload.habits.isNotEmpty()) {
            database.habitDao().insertHabits(payload.habits)
        }
        if (payload.habitLogs.isNotEmpty()) {
            database.habitDao().insertOrUpdateLogs(payload.habitLogs)
        }
        if (payload.weightLogs.isNotEmpty()) {
            database.weightDao().insertWeights(payload.weightLogs)
        }
        if (payload.skincareRoutines.isNotEmpty()) {
            database.skincareDao().insertRoutineSteps(payload.skincareRoutines)
        }
        if (payload.skincareLogs.isNotEmpty()) {
            database.skincareDao().insertOrUpdateLogs(payload.skincareLogs)
        }
        if (payload.fastingSessions.isNotEmpty()) {
            database.fastingDao().insertSessions(payload.fastingSessions)
        }
        if (payload.fastingMeals.isNotEmpty()) {
            database.fastingDao().insertMeals(payload.fastingMeals)
        }
        if (payload.stepLogs.isNotEmpty()) {
            database.stepDao().insertOrUpdateSteps(payload.stepLogs)
        }
    }
}

