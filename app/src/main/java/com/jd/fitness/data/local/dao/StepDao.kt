package com.jd.fitness.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jd.fitness.data.local.entity.StepLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM step_logs WHERE date = :date LIMIT 1")
    fun getStepsForDate(date: String): Flow<StepLogEntity?>

    @Query("SELECT * FROM step_logs WHERE date = :date LIMIT 1")
    suspend fun getStepsForDateOnce(date: String): StepLogEntity?

    @Query("SELECT * FROM step_logs WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getStepsBetweenDates(startDate: String, endDate: String): Flow<List<StepLogEntity>>

    @Query("SELECT * FROM step_logs WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    suspend fun getStepsBetweenDatesOnce(startDate: String, endDate: String): List<StepLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStep(log: StepLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSteps(logs: List<StepLogEntity>)

    @Query("SELECT * FROM step_logs ORDER BY date DESC")
    suspend fun getAllSteps(): List<StepLogEntity>
}

