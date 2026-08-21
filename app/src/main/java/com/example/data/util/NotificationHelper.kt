package com.example.data.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_DAILY_REMINDER = "briefly_daily_reminders"
    const val CHANNEL_LIVE_TRACKING = "briefly_live_tracking"
    const val CHANNEL_MILESTONES = "briefly_milestones"

    const val NOTIFICATION_ID_DAILY = 1001
    const val NOTIFICATION_ID_LIVE_TRACKER = 1002
    const val NOTIFICATION_ID_MILESTONE = 1003
    const val NOTIFICATION_ID_TEST = 1004

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_DAILY_REMINDER,
                "Daily Goal Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications to remind you to log and complete your daily goals"
                enableVibration(true)
                enableLights(true)
            }

            val liveTrackingChannel = NotificationChannel(
                CHANNEL_LIVE_TRACKING,
                "Real-Time Goal Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active timer and live progress updates"
            }

            val milestoneChannel = NotificationChannel(
                CHANNEL_MILESTONES,
                "Milestones & Streaks",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Celebrations when reaching streaks and goal completions"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(liveTrackingChannel)
            notificationManager.createNotificationChannel(milestoneChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendTestNotification(context: Context, title: String, message: String) {
        createNotificationChannels(context)
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_TEST, notification)
        } catch (e: SecurityException) {
            // Permission denied
        }
    }

    fun sendDailyReminder(context: Context, activeGoalCount: Int, completedCount: Int) {
        createNotificationChannels(context)
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val remaining = (activeGoalCount - completedCount).coerceAtLeast(0)
        val title = if (remaining == 0) "🎉 All Daily Goals Done!" else "⏰ Daily Goal Check-in"
        val message = if (remaining == 0) {
            "Amazing job! You finished all $activeGoalCount goals today. Keep the streak going!"
        } else {
            "You've completed $completedCount of $activeGoalCount goals. $remaining left to finish today!"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DAILY, notification)
        } catch (e: SecurityException) {
            // Permission denied
        }
    }

    fun sendMilestoneNotification(context: Context, streakCount: Int, goalTitle: String) {
        createNotificationChannels(context)
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MILESTONES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🔥 $streakCount-Day Streak on $goalTitle!")
            .setContentText("You're on fire! Consistency creates greatness.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_MILESTONE, notification)
        } catch (e: SecurityException) {
            // Permission denied
        }
    }

    fun updateLiveTrackingNotification(
        context: Context,
        goalTitle: String,
        elapsedSeconds: Long,
        totalSeconds: Long,
        isPaused: Boolean
    ) {
        createNotificationChannels(context)
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)
        val progress = if (totalSeconds > 0) ((elapsedSeconds.toFloat() / totalSeconds) * 100).toInt() else 0

        val builder = NotificationCompat.Builder(context, CHANNEL_LIVE_TRACKING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(if (isPaused) "⏸ Tracking Paused: $goalTitle" else "⏱ Live Tracking: $goalTitle")
            .setContentText("Elapsed: $timeString" + if (totalSeconds > 0) " (${progress}%)" else "")
            .setOngoing(!isPaused)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)

        if (totalSeconds > 0) {
            builder.setProgress(totalSeconds.toInt(), elapsedSeconds.toInt(), false)
        }

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_LIVE_TRACKER, builder.build())
        } catch (e: SecurityException) {
            // Permission denied
        }
    }

    fun cancelLiveTrackingNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_LIVE_TRACKER)
    }
}
