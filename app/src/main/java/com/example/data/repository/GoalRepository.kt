package com.example.data.repository

import com.example.data.local.GoalCompletionEntity
import com.example.data.local.GoalDao
import com.example.data.local.GoalEntity
import com.example.data.local.GoalSubtaskEntity
import com.example.data.local.GoalWithDetails
import com.example.data.util.DateUtils
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class GoalRepository(private val goalDao: GoalDao) {

    val activeGoalsWithDetails: Flow<List<GoalWithDetails>> = goalDao.getActiveGoalsWithDetails()
    val allGoalsWithDetails: Flow<List<GoalWithDetails>> = goalDao.getAllGoalsWithDetails()
    val allCompletions: Flow<List<GoalCompletionEntity>> = goalDao.getAllCompletions()

    fun getGoalWithDetails(goalId: Long): Flow<GoalWithDetails?> = goalDao.getGoalWithDetailsById(goalId)

    suspend fun createGoal(
        title: String,
        description: String = "",
        category: String,
        categoryColorHex: String,
        categoryIconName: String,
        dailyTarget: Int = 1,
        targetUnit: String = "times",
        reminderTime: String? = null,
        subtasks: List<String> = emptyList()
    ): Long {
        val goal = GoalEntity(
            title = title.trim(),
            description = description.trim(),
            category = category,
            categoryColorHex = categoryColorHex,
            categoryIconName = categoryIconName,
            dailyTarget = dailyTarget,
            targetUnit = targetUnit,
            reminderTime = reminderTime,
            startDate = System.currentTimeMillis()
        )
        val goalId = goalDao.insertGoal(goal)

        if (subtasks.isNotEmpty()) {
            val subtaskEntities = subtasks.filter { it.isNotBlank() }.mapIndexed { index, subtaskTitle ->
                GoalSubtaskEntity(
                    goalId = goalId,
                    title = subtaskTitle.trim(),
                    isCompleted = false,
                    orderIndex = index
                )
            }
            goalDao.insertSubtasks(subtaskEntities)
        }
        return goalId
    }

    suspend fun updateGoal(goal: GoalEntity) {
        goalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(goalId: Long) {
        goalDao.deleteGoalById(goalId)
    }

    suspend fun archiveGoal(goalId: Long, isArchived: Boolean) {
        goalDao.setArchived(goalId, isArchived)
    }

    // Toggle today's completion for a goal
    suspend fun toggleGoalCompletion(goalId: Long, dateString: String = DateUtils.getTodayString(), isCompleted: Boolean) {
        if (isCompleted) {
            val completion = GoalCompletionEntity(
                goalId = goalId,
                dateString = dateString,
                completedAt = System.currentTimeMillis(),
                progressCount = 1,
                targetCount = 1
            )
            goalDao.insertOrUpdateCompletion(completion)
        } else {
            goalDao.deleteCompletion(goalId, dateString)
        }
    }

    // Subtasks
    suspend fun addSubtask(goalId: Long, title: String) {
        val subtask = GoalSubtaskEntity(
            goalId = goalId,
            title = title.trim(),
            isCompleted = false
        )
        goalDao.insertSubtask(subtask)
    }

    suspend fun toggleSubtask(subtask: GoalSubtaskEntity) {
        goalDao.updateSubtask(subtask.copy(isCompleted = !subtask.isCompleted))
    }

    suspend fun deleteSubtask(subtaskId: Long) {
        goalDao.deleteSubtaskById(subtaskId)
    }

    // Populate initial demo goals if DB is empty
    suspend fun populateSampleGoalsIfNeeded() {
        val sampleList = listOf(
            SampleGoal(
                title = "Read 20 minutes",
                description = "Focus on non-fiction or growth book",
                category = "Learning",
                color = "#8B5CF6",
                icon = "book",
                target = 20,
                unit = "minutes",
                reminder = "09:00 PM",
                subtasks = listOf("Pick today's chapter", "Take brief notes", "Review takeaways"),
                completedDaysAgo = listOf(0, 1, 2, 3, 5, 6, 8, 9, 10)
            ),
            SampleGoal(
                title = "Workout 30 minutes",
                description = "Full body resistance and cardio session",
                category = "Health",
                color = "#FF6B6B",
                icon = "fitness_center",
                target = 30,
                unit = "minutes",
                reminder = "07:30 AM",
                subtasks = listOf("10 minutes warm-up", "15 minutes exercise", "5 minutes stretching", "Cool down"),
                completedDaysAgo = listOf(1, 2, 3, 4, 6, 7)
            ),
            SampleGoal(
                title = "Drink 2L water",
                description = "Stay hydrated throughout the day",
                category = "Health",
                color = "#3B82F6",
                icon = "water_drop",
                target = 2000,
                unit = "ml",
                reminder = "10:00 AM",
                subtasks = listOf("500ml morning bottle", "500ml afternoon", "500ml workout", "500ml evening"),
                completedDaysAgo = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8)
            ),
            SampleGoal(
                title = "Write journal",
                description = "Reflect on accomplishments and daily thoughts",
                category = "Mindfulness",
                color = "#10B981",
                icon = "edit_note",
                target = 1,
                unit = "times",
                reminder = "10:30 PM",
                subtasks = listOf("List 3 things I'm grateful for", "Describe highlight of today", "Plan tomorrow's priority"),
                completedDaysAgo = listOf(1, 2, 4, 5)
            )
        )

        for (item in sampleList) {
            val goalId = createGoal(
                title = item.title,
                description = item.description,
                category = item.category,
                categoryColorHex = item.color,
                categoryIconName = item.icon,
                dailyTarget = item.target,
                targetUnit = item.unit,
                reminderTime = item.reminder,
                subtasks = item.subtasks
            )

            // Add history completions
            for (daysAgo in item.completedDaysAgo) {
                val dateStr = DateUtils.getDaysAgoString(daysAgo)
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                goalDao.insertOrUpdateCompletion(
                    GoalCompletionEntity(
                        goalId = goalId,
                        dateString = dateStr,
                        completedAt = cal.timeInMillis,
                        progressCount = 1,
                        targetCount = 1
                    )
                )
            }
        }
    }

    suspend fun clearAllData() {
        goalDao.clearAllGoals()
        goalDao.clearAllCompletions()
    }

    private data class SampleGoal(
        val title: String,
        val description: String,
        val category: String,
        val color: String,
        val icon: String,
        val target: Int,
        val unit: String,
        val reminder: String,
        val subtasks: List<String>,
        val completedDaysAgo: List<Int>
    )
}
