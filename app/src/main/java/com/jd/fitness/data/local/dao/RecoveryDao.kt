package com.jd.fitness.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jd.fitness.data.local.entity.RecoveryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecoveryDao {
    @Query("SELECT * FROM recovery_entries WHERE date = :date LIMIT 1")
    fun getRecoveryForDate(date: String): Flow<RecoveryEntity?>

    @Query("SELECT * FROM recovery_entries WHERE date = :date LIMIT 1")
    suspend fun getRecoveryForDateOnce(date: String): RecoveryEntity?

    @Query("SELECT * FROM recovery_entries ORDER BY date DESC")
    fun getAllRecoveryEntries(): Flow<List<RecoveryEntity>>

    @Query("SELECT * FROM recovery_entries ORDER BY date DESC")
    suspend fun getAllRecoveryEntriesOnce(): List<RecoveryEntity>

    @Query("SELECT * FROM recovery_entries WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getRecoveryBetweenDates(startDate: String, endDate: String): Flow<List<RecoveryEntity>>

    @Query("SELECT * FROM recovery_entries WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    suspend fun getRecoveryBetweenDatesOnce(startDate: String, endDate: String): List<RecoveryEntity>

    @Query("SELECT COUNT(*) FROM recovery_entries WHERE isRestDay = 1 AND date >= :startDate AND date <= :endDate")
    suspend fun countRestDaysBetween(startDate: String, endDate: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: RecoveryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<RecoveryEntity>): List<Long>

    @Update
    suspend fun update(entry: RecoveryEntity)

    @Delete
    suspend fun delete(entry: RecoveryEntity)

    @Query("DELETE FROM recovery_entries WHERE date = :date")
    suspend fun deleteByDate(date: String)
}

