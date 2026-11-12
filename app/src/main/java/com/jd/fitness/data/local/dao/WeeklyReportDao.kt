package com.jd.fitness.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jd.fitness.data.local.entity.WeeklyReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyReportDao {
    @Query("SELECT * FROM weekly_reports ORDER BY weekStartDate DESC")
    fun getAllReports(): Flow<List<WeeklyReportEntity>>

    @Query("SELECT * FROM weekly_reports ORDER BY weekStartDate DESC")
    suspend fun getAllReportsOnce(): List<WeeklyReportEntity>

    @Query("SELECT * FROM weekly_reports WHERE weekStartDate = :startDate LIMIT 1")
    suspend fun getReportForWeek(startDate: String): WeeklyReportEntity?

    @Query("SELECT * FROM weekly_reports ORDER BY weekStartDate DESC LIMIT 1")
    fun getLatestReport(): Flow<WeeklyReportEntity?>

    @Query("SELECT * FROM weekly_reports ORDER BY weekStartDate DESC LIMIT 1")
    suspend fun getLatestReportOnce(): WeeklyReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: WeeklyReportEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<WeeklyReportEntity>): List<Long>

    @Update
    suspend fun updateReport(report: WeeklyReportEntity)

    @Delete
    suspend fun deleteReport(report: WeeklyReportEntity)

    @Query("DELETE FROM weekly_reports WHERE id = :id")
    suspend fun deleteReportById(id: Long)
}

