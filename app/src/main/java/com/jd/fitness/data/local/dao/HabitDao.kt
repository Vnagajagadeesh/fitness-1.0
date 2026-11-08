package com.jd.fitness.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jd.fitness.data.local.entity.HabitEntity
import com.jd.fitness.data.local.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    // --- Habit Definitions ---
    @Query("SELECT * FROM habits ORDER BY orderIndex ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isEnabled = 1 ORDER BY orderIndex ASC")
    fun getActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isEnabled = 1 ORDER BY orderIndex ASC")
    suspend fun getActiveHabitsOnce(): List<HabitEntity>

    @Query("SELECT * FROM habits ORDER BY orderIndex ASC")
    suspend fun getAllHabitsOnce(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>): List<Long>

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)

    // --- Habit Logs (Daily check-ins) ---
    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    suspend fun getLogsForDateOnce(date: String): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE date = :date AND habitId = :habitId LIMIT 1")
    suspend fun getLogForHabit(date: String, habitId: Long): HabitLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: HabitLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLogs(logs: List<HabitLogEntity>)

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND isCompleted = 1 ORDER BY date DESC")
    suspend fun getCompletedLogsForHabit(habitId: Long): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE date >= :startDate AND date <= :endDate")
    fun getLogsBetweenDates(startDate: String, endDate: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE date >= :startDate AND date <= :endDate")
    suspend fun getLogsBetweenDatesOnce(startDate: String, endDate: String): List<HabitLogEntity>

    @Query("SELECT COUNT(*) FROM habit_logs WHERE date = :date AND isCompleted = 1")
    fun getCompletedCountForDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM habit_logs WHERE isCompleted = 1")
    fun getTotalCompletedCount(): Flow<Int>

    @Query("SELECT DISTINCT date FROM habit_logs WHERE isCompleted = 1 ORDER BY date DESC")
    suspend fun getAllCompletedDates(): List<String>

    @Query("SELECT * FROM habit_logs")
    suspend fun getAllLogs(): List<HabitLogEntity>
}

