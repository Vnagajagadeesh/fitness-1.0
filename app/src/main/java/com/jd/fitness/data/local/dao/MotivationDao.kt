package com.jd.fitness.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jd.fitness.data.local.entity.MotivationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MotivationDao {
    @Query("SELECT * FROM saved_motivation ORDER BY savedAt DESC")
    fun getAllQuotes(): Flow<List<MotivationEntity>>

    @Query("SELECT * FROM saved_motivation ORDER BY savedAt DESC")
    suspend fun getAllQuotesOnce(): List<MotivationEntity>

    @Query("SELECT * FROM saved_motivation WHERE isFavorite = 1 ORDER BY savedAt DESC")
    fun getFavoriteQuotes(): Flow<List<MotivationEntity>>

    @Query("SELECT * FROM saved_motivation WHERE category = :category ORDER BY savedAt DESC")
    fun getQuotesByCategory(category: String): Flow<List<MotivationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: MotivationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<MotivationEntity>): List<Long>

    @Update
    suspend fun updateQuote(quote: MotivationEntity)

    @Delete
    suspend fun deleteQuote(quote: MotivationEntity)

    @Query("UPDATE saved_motivation SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)
}

