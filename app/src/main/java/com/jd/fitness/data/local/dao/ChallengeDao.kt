package com.jd.fitness.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jd.fitness.data.local.entity.ChallengeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenge_config WHERE id = 1 LIMIT 1")
    fun getChallengeConfig(): Flow<ChallengeEntity?>

    @Query("SELECT * FROM challenge_config WHERE id = 1 LIMIT 1")
    suspend fun getChallengeConfigOnce(): ChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: ChallengeEntity)

    @Update
    suspend fun update(config: ChallengeEntity)
}

