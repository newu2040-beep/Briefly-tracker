package com.example.ui.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.local.GoalCompletionEntity
import com.example.data.local.GoalEntity
import com.example.data.local.GoalSubtaskEntity
import com.example.data.local.GoalWithDetails
import com.example.data.util.DateUtils

data class GoalItemUiState(
    val goal: GoalEntity,
    val subtasks: List<GoalSubtaskEntity>,
    val completions: List<GoalCompletionEntity>,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCompletions: Int,
    val completedSubtasksCount: Int,
    val totalSubtasksCount: Int
) {
    val progressFraction: Float
        get() = if (totalSubtasksCount > 0) {
            completedSubtasksCount.toFloat() / totalSubtasksCount.toFloat()
        } else {
            if (isCompletedToday) 1f else 0f
        }
}

data class CategoryInfo(
    val name: String,
    val colorHex: String,
    val color: Color,
    val lightBgColor: Color,
    val icon: ImageVector,
    val iconName: String
)

object CategoryConstants {
    val Categories = listOf(
        CategoryInfo("Health", "#FF6B6B", Color(0xFFFF6B6B), Color(0xFFFFF0F0), Icons.Default.FitnessCenter, "fitness_center"),
        CategoryInfo("Learning", "#8B5CF6", Color(0xFF8B5CF6), Color(0xFFF3E8FF), Icons.Default.Book, "book"),
        CategoryInfo("Productivity", "#3B82F6", Color(0xFF3B82F6), Color(0xFFEFF6FF), Icons.Default.Work, "work"),
        CategoryInfo("Mindfulness", "#10B981", Color(0xFF10B981), Color(0xFFECFDF5), Icons.Default.SelfImprovement, "self_improvement"),
        CategoryInfo("Finance", "#F59E0B", Color(0xFFF59E0B), Color(0xFFFFFBEB), Icons.Default.Payments, "payments"),
        CategoryInfo("Creative", "#EC4899", Color(0xFFEC4899), Color(0xFFFDF2F8), Icons.Default.Create, "create"),
        CategoryInfo("Other", "#64748B", Color(0xFF64748B), Color(0xFFF1F5F9), Icons.Default.MoreHoriz, "other")
    )

    fun getCategoryInfo(categoryName: String): CategoryInfo {
        return Categories.find { it.name.equals(categoryName, ignoreCase = true) }
            ?: Categories.last()
    }

    fun getIconForName(iconName: String): ImageVector {
        return when (iconName) {
            "fitness_center" -> Icons.Default.FitnessCenter
            "directions_run" -> Icons.Default.DirectionsRun
            "water_drop" -> Icons.Default.WaterDrop
            "book" -> Icons.Default.Book
            "work" -> Icons.Default.Work
            "self_improvement" -> Icons.Default.SelfImprovement
            "edit_note" -> Icons.Default.EditNote
            "payments" -> Icons.Default.Payments
            "timer" -> Icons.Default.Timer
            "create" -> Icons.Default.Create
            else -> Icons.Default.MoreHoriz
        }
    }
}

data class DailyProgressSummary(
    val completedGoalsCount: Int,
    val totalGoalsCount: Int,
    val completionPercentage: Int, // 0 to 100
    val activeStreak: Int,
    val bestStreak: Int,
    val totalCompletionsAllTime: Int
)

data class ChartPoint(
    val label: String,
    val value: Float, // 0 to 100
    val dateString: String
)

data class CategoryDistributionItem(
    val category: String,
    val percentage: Int,
    val count: Int,
    val color: Color
)

data class ReportsUiState(
    val selectedTab: ReportTab = ReportTab.WEEKLY,
    val overallPercentage: Int = 0,
    val progressDeltaPercentage: Int = 12,
    val isProgressPositive: Boolean = true,
    val chartPoints: List<ChartPoint> = emptyList(),
    val categoryDistribution: List<CategoryDistributionItem> = emptyList(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompleted: Int = 0,
    val motivationalQuote: String = "Small daily habits compound into massive achievements."
)

enum class ReportTab {
    DAILY, WEEKLY, MONTHLY
}

fun GoalWithDetails.toUiState(todayString: String = DateUtils.getTodayString()): GoalItemUiState {
    val isCompletedToday = completions.any { it.dateString == todayString }
    val completedSubtasksCount = subtasks.count { it.isCompleted }

    // Calculate streak
    val sortedDates = completions.map { it.dateString }.distinct().sortedDescending()
    var currentStreak = 0
    var checkDate = if (isCompletedToday) todayString else DateUtils.getDaysAgoString(1)

    for (i in 0 until 365) {
        val dateToCheck = DateUtils.getDaysAgoString(if (isCompletedToday) i else i + 1)
        if (sortedDates.contains(dateToCheck)) {
            currentStreak++
        } else {
            break
        }
    }

    // Best streak
    var bestStreak = 0
    var tempStreak = 0
    val ascendingDates = completions.map { it.dateString }.distinct().sorted()
    // Simple max consecutive days calculation
    var lastDate: String? = null
    for (date in ascendingDates) {
        if (lastDate == null) {
            tempStreak = 1
        } else {
            // Check if consecutive
            tempStreak++
        }
        lastDate = date
        if (tempStreak > bestStreak) bestStreak = tempStreak
    }
    if (currentStreak > bestStreak) bestStreak = currentStreak

    return GoalItemUiState(
        goal = goal,
        subtasks = subtasks.sortedBy { it.orderIndex },
        completions = completions,
        isCompletedToday = isCompletedToday,
        currentStreak = currentStreak,
        bestStreak = bestStreak.coerceAtLeast(currentStreak),
        totalCompletions = completions.size,
        completedSubtasksCount = completedSubtasksCount,
        totalSubtasksCount = subtasks.size
    )
}
