package com.example.data.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.example.ACTION_DAILY_REMINDER" -> {
                val activeGoals = intent.getIntExtra("active_goals_count", 3)
                val completedGoals = intent.getIntExtra("completed_goals_count", 1)
                NotificationHelper.sendDailyReminder(context, activeGoals, completedGoals)
            }
            "com.example.ACTION_GOAL_TIMER_COMPLETE" -> {
                val goalTitle = intent.getStringExtra("goal_title") ?: "Your Goal"
                NotificationHelper.sendTestNotification(
                    context,
                    "🎯 Focus Session Complete!",
                    "You've completed your tracking session for '$goalTitle'!"
                )
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                NotificationHelper.createNotificationChannels(context)
            }
        }
    }
}
