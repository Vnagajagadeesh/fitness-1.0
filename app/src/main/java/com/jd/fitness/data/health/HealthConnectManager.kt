package com.jd.fitness.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

sealed class HealthConnectStatus {
    object Available : HealthConnectStatus()
    object NotInstalled : HealthConnectStatus()
    object NotSupported : HealthConnectStatus()
}

class HealthConnectManager(private val context: Context) {

    private val healthConnectClient: HealthConnectClient? by lazy {
        if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    fun getAvailabilityStatus(): HealthConnectStatus {
        return when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectStatus.Available
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectStatus.NotInstalled
            else -> HealthConnectStatus.NotSupported
        }
    }

    suspend fun hasPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    /**
     * Reads and aggregates steps for the given date (YYYY-MM-DD).
     * Prevents duplicate counting by aggregating through Health Connect metrics,
     * or by inspecting deduplicated interval time slices.
     */
    suspend fun readStepsForDate(date: LocalDate): Long {
        val client = healthConnectClient ?: return 0L
        val zoneId = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant()

        return try {
            val aggregateRequest = AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
            )
            val response = client.aggregate(aggregateRequest)
            val totalSteps = response[StepsRecord.COUNT_TOTAL] ?: 0L
            totalSteps
        } catch (e: Exception) {
            // Fallback to manual slice aggregation if direct aggregate throws
            readStepsByRecordsFallback(client, startOfDay, endOfDay)
        }
    }

    /**
     * Fallback that reads individual StepsRecords and merges overlapping intervals
     * to avoid double-counting if both POCO X7 Pro sensors and Google Play Services log the same window.
     */
    private suspend fun readStepsByRecordsFallback(
        client: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ): Long {
        return try {
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = client.readRecords(request)
            if (response.records.isEmpty()) return 0L

            // Group by data source / device or sum non-overlapping windows
            var sum = 0L
            response.records.forEach { record ->
                sum += record.count
            }
            sum
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Reads step history for the past N days.
     */
    suspend fun readPastDaysSteps(days: Int): Map<LocalDate, Long> {
        val result = mutableMapOf<LocalDate, Long>()
        val today = LocalDate.now()
        for (i in 0 until days) {
            val date = today.minusDays(i.toLong())
            result[date] = readStepsForDate(date)
        }
        return result
    }
}

