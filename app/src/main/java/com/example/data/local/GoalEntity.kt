package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Health", // Health, Learning, Productivity, Mindfulness, Fitness, Finance, Creative, Other
    val categoryColorHex: String = "#4F46E5",
    val categoryIconName: String = "fitness_center", // icon key
    val dailyTarget: Int = 1,
    val targetUnit: String = "times",
    val reminderTime: String? = null, // e.g. "08:00 AM"
    val startDate: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
