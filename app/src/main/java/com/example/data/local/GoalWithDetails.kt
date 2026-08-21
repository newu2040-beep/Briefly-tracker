package com.example.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class GoalWithDetails(
    @Embedded
    val goal: GoalEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "goalId"
    )
    val subtasks: List<GoalSubtaskEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "goalId"
    )
    val completions: List<GoalCompletionEntity>
)
