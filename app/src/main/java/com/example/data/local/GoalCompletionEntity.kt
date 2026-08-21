package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goal_completions",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["goalId"]),
        Index(value = ["dateString"]),
        Index(value = ["goalId", "dateString"], unique = true)
    ]
)
data class GoalCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    val dateString: String, // Format "yyyy-MM-dd" e.g. "2026-08-21"
    val completedAt: Long = System.currentTimeMillis(),
    val progressCount: Int = 1,
    val targetCount: Int = 1
)
