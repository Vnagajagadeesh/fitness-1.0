package com.jd.fitness.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jd.fitness.data.local.entity.CoachInsightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachDao {
    @Query("SELECT * FROM coach_insights ORDER BY timestamp DESC LIMIT 1")
    fun getLatestInsight(): Flow<CoachInsightEntity?>

    @Query("SELECT * FROM coach_insights ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestInsightOnce(): CoachInsightEntity?

    @Query("SELECT * FROM coach_insights WHERE date = :date ORDER BY timestamp DESC LIMIT 1")
    suspend fun getInsightForDate(date: String): CoachInsightEntity?

    @Query("SELECT * FROM coach_insights ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentInsights(limit: Int = 14): Flow<List<CoachInsightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: CoachInsightEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsights(insights: List<CoachInsightEntity>): List<Long>

    @Query("DELETE FROM coach_insights WHERE date = :date")
    suspend fun deleteForDate(date: String)
}

