package com.jd.fitness.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jd.fitness.data.local.entity.PlannerTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannerDao {
    @Query("SELECT * FROM planner_tasks WHERE date = :date ORDER BY startTime ASC, orderIndex ASC")
    fun getTasksForDate(date: String): Flow<List<PlannerTaskEntity>>

    @Query("SELECT * FROM planner_tasks WHERE date = :date ORDER BY startTime ASC, orderIndex ASC")
    suspend fun getTasksForDateOnce(date: String): List<PlannerTaskEntity>

    @Query("SELECT * FROM planner_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): PlannerTaskEntity?

    @Query("SELECT * FROM planner_tasks ORDER BY date DESC, startTime ASC")
    fun getAllTasks(): Flow<List<PlannerTaskEntity>>

    @Query("SELECT * FROM planner_tasks ORDER BY date DESC, startTime ASC")
    suspend fun getAllTasksOnce(): List<PlannerTaskEntity>

    @Query("SELECT COUNT(*) FROM planner_tasks WHERE date = :date AND isCompleted = 1")
    suspend fun getCompletedCountForDate(date: String): Int

    @Query("SELECT COUNT(*) FROM planner_tasks WHERE date = :date")
    suspend fun getTotalCountForDate(date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: PlannerTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<PlannerTaskEntity>): List<Long>

    @Update
    suspend fun updateTask(task: PlannerTaskEntity)

    @Delete
    suspend fun deleteTask(task: PlannerTaskEntity)

    @Query("DELETE FROM planner_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE planner_tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, isCompleted: Boolean)
}

