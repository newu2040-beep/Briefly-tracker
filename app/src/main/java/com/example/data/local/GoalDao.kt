package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Transaction
    @Query("SELECT * FROM goals WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getActiveGoalsWithDetails(): Flow<List<GoalWithDetails>>

    @Transaction
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAllGoalsWithDetails(): Flow<List<GoalWithDetails>>

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun getGoalWithDetailsById(goalId: Long): Flow<GoalWithDetails?>

    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: Long): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: Long)

    @Query("UPDATE goals SET isArchived = :isArchived WHERE id = :goalId")
    suspend fun setArchived(goalId: Long, isArchived: Boolean)

    // Subtasks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<GoalSubtaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: GoalSubtaskEntity): Long

    @Update
    suspend fun updateSubtask(subtask: GoalSubtaskEntity)

    @Query("DELETE FROM goal_subtasks WHERE id = :subtaskId")
    suspend fun deleteSubtaskById(subtaskId: Long)

    @Query("DELETE FROM goal_subtasks WHERE goalId = :goalId")
    suspend fun deleteSubtasksByGoalId(goalId: Long)

    // Completions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCompletion(completion: GoalCompletionEntity)

    @Query("DELETE FROM goal_completions WHERE goalId = :goalId AND dateString = :dateString")
    suspend fun deleteCompletion(goalId: Long, dateString: String)

    @Query("SELECT * FROM goal_completions WHERE dateString = :dateString")
    fun getCompletionsForDate(dateString: String): Flow<List<GoalCompletionEntity>>

    @Query("SELECT * FROM goal_completions")
    fun getAllCompletions(): Flow<List<GoalCompletionEntity>>

    @Query("SELECT COUNT(*) FROM goal_completions WHERE goalId = :goalId")
    suspend fun getCompletionCountForGoal(goalId: Long): Int

    @Query("DELETE FROM goals")
    suspend fun clearAllGoals()

    @Query("DELETE FROM goal_completions")
    suspend fun clearAllCompletions()
}
