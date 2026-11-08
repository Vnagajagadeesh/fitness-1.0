package com.jd.fitness.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jd.fitness.data.local.entity.FastingEntity
import com.jd.fitness.data.local.entity.FastingMealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FastingDao {
    // --- Sessions ---
    @Query("SELECT * FROM fasting_sessions WHERE state = 'ACTIVE' ORDER BY startTime DESC LIMIT 1")
    fun getActiveSession(): Flow<FastingEntity?>

    @Query("SELECT * FROM fasting_sessions WHERE state = 'ACTIVE' ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSessionOnce(): FastingEntity?

    @Query("SELECT * FROM fasting_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FastingEntity>>

    @Query("SELECT * FROM fasting_sessions ORDER BY startTime DESC")
    suspend fun getAllSessionsOnce(): List<FastingEntity>

    @Query("SELECT * FROM fasting_sessions WHERE startTime >= :startMillis AND startTime <= :endMillis ORDER BY startTime DESC")
    fun getSessionsBetween(startMillis: Long, endMillis: Long): Flow<List<FastingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FastingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<FastingEntity>): List<Long>

    @Update
    suspend fun updateSession(session: FastingEntity)

    @Delete
    suspend fun deleteSession(session: FastingEntity)

    // --- Meals ---
    @Query("SELECT * FROM fasting_meals WHERE sessionId = :sessionId ORDER BY mealTime ASC")
    fun getMealsForSession(sessionId: Long): Flow<List<FastingMealEntity>>

    @Query("SELECT * FROM fasting_meals WHERE mealTime >= :startMillis AND mealTime <= :endMillis ORDER BY mealTime ASC")
    fun getMealsBetween(startMillis: Long, endMillis: Long): Flow<List<FastingMealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: FastingMealEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<FastingMealEntity>): List<Long>

    @Delete
    suspend fun deleteMeal(meal: FastingMealEntity)

    @Query("SELECT * FROM fasting_meals")
    suspend fun getAllMeals(): List<FastingMealEntity>
}

