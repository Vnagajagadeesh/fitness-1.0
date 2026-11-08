package com.jd.fitness.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jd.fitness.data.local.entity.WeightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_logs ORDER BY timestamp DESC")
    fun getAllWeightLogs(): Flow<List<WeightEntity>>

    @Query("SELECT * FROM weight_logs ORDER BY timestamp DESC")
    suspend fun getAllWeightLogsOnce(): List<WeightEntity>

    @Query("SELECT * FROM weight_logs ORDER BY timestamp DESC LIMIT 1")
    fun getLatestWeight(): Flow<WeightEntity?>

    @Query("SELECT * FROM weight_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestWeightOnce(): WeightEntity?

    @Query("SELECT * FROM weight_logs WHERE date = :date ORDER BY timestamp ASC")
    fun getWeightsForDate(date: String): Flow<List<WeightEntity>>

    @Query("SELECT * FROM weight_logs WHERE date >= :startDate AND date <= :endDate ORDER BY timestamp ASC")
    fun getWeightsBetweenDates(startDate: String, endDate: String): Flow<List<WeightEntity>>

    @Query("SELECT * FROM weight_logs WHERE date >= :startDate AND date <= :endDate ORDER BY timestamp ASC")
    suspend fun getWeightsBetweenDatesOnce(startDate: String, endDate: String): List<WeightEntity>

    @Query("SELECT AVG(weightKg) FROM weight_logs")
    fun getAllTimeAverageWeight(): Flow<Double?>

    @Query("SELECT AVG(weightKg) FROM weight_logs WHERE date >= :startDate")
    fun getAverageWeightSince(startDate: String): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(weight: WeightEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeights(weights: List<WeightEntity>): List<Long>

    @Update
    suspend fun updateWeight(weight: WeightEntity)

    @Delete
    suspend fun deleteWeight(weight: WeightEntity)

    @Query("DELETE FROM weight_logs WHERE id = :id")
    suspend fun deleteWeightById(id: Long)
}

