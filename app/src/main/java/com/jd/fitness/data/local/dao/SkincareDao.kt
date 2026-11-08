package com.jd.fitness.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jd.fitness.data.local.entity.SkincareLogEntity
import com.jd.fitness.data.local.entity.SkincareRoutineEntity
import com.jd.fitness.data.local.entity.SkincareRoutineType
import kotlinx.coroutines.flow.Flow

@Dao
interface SkincareDao {
    // --- Routine Steps ---
    @Query("SELECT * FROM skincare_routines WHERE routineType = :type AND isEnabled = 1 ORDER BY stepOrder ASC")
    fun getRoutinesByType(type: SkincareRoutineType): Flow<List<SkincareRoutineEntity>>

    @Query("SELECT * FROM skincare_routines ORDER BY routineType ASC, stepOrder ASC")
    suspend fun getAllRoutinesOnce(): List<SkincareRoutineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineStep(step: SkincareRoutineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineSteps(steps: List<SkincareRoutineEntity>): List<Long>

    @Update
    suspend fun updateRoutineStep(step: SkincareRoutineEntity)

    @Delete
    suspend fun deleteRoutineStep(step: SkincareRoutineEntity)

    // --- Daily Logs ---
    @Query("SELECT * FROM skincare_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<SkincareLogEntity>>

    @Query("SELECT * FROM skincare_logs WHERE date = :date AND routineType = :type LIMIT 1")
    fun getLogForDateAndType(date: String, type: SkincareRoutineType): Flow<SkincareLogEntity?>

    @Query("SELECT * FROM skincare_logs WHERE date = :date AND routineType = :type LIMIT 1")
    suspend fun getLogForDateAndTypeOnce(date: String, type: SkincareRoutineType): SkincareLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: SkincareLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLogs(logs: List<SkincareLogEntity>)

    @Query("SELECT * FROM skincare_logs WHERE routineType = :type AND isCompleted = 1 ORDER BY date DESC")
    suspend fun getCompletedLogsForType(type: SkincareRoutineType): List<SkincareLogEntity>

    @Query("SELECT * FROM skincare_logs WHERE date >= :startDate AND date <= :endDate")
    fun getLogsBetweenDates(startDate: String, endDate: String): Flow<List<SkincareLogEntity>>

    @Query("SELECT * FROM skincare_logs")
    suspend fun getAllLogs(): List<SkincareLogEntity>
}

