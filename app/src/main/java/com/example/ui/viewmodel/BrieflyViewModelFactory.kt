package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.BrieflyDatabase
import com.example.data.preferences.UserPreferences
import com.example.data.repository.GoalRepository

class BrieflyViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BrieflyViewModel::class.java)) {
            val database = BrieflyDatabase.getDatabase(context)
            val repository = GoalRepository(database.goalDao())
            val preferences = UserPreferences(context)
            return BrieflyViewModel(repository, preferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
