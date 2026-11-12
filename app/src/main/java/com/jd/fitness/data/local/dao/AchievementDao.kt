package com.jd.fitness.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jd.fitness.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievement_records ORDER BY isUnlocked DESC, category ASC, id ASC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievement_records ORDER BY isUnlocked DESC, category ASC, id ASC")
    suspend fun getAllAchievementsOnce(): List<AchievementEntity>

    @Query("SELECT * FROM achievement_records WHERE id = :id LIMIT 1")
    suspend fun getAchievementById(id: String): AchievementEntity?

    @Query("SELECT COUNT(*) FROM achievement_records WHERE isUnlocked = 1")
    fun getUnlockedCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM achievement_records WHERE isUnlocked = 1")
    suspend fun getUnlockedCountOnce(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultAchievements(achievements: List<AchievementEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(achievement: AchievementEntity)

    @Update
    suspend fun update(achievement: AchievementEntity)
}

