package com.example.data.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.ui.models.GoalItemUiState
import com.example.ui.models.ReportsUiState

object SocialShareHelper {

    enum class SocialPlatform(
        val displayName: String,
        val packageName: String?,
        val iconResId: String
    ) {
        WHATSAPP("WhatsApp", "com.whatsapp", "whatsapp"),
        TWITTER("X / Twitter", "com.twitter.android", "twitter"),
        TELEGRAM("Telegram", "org.telegram.messenger", "telegram"),
        LINKEDIN("LinkedIn", "com.linkedin.android", "linkedin"),
        FACEBOOK("Facebook", "com.facebook.katana", "facebook"),
        INSTAGRAM("Instagram", "com.instagram.android", "instagram"),
        GENERAL("More Apps", null, "share")
    }

    /**
     * Creates an engaging summary text for social sharing.
     */
    fun createShareMessage(
        reportsState: ReportsUiState,
        goals: List<GoalItemUiState>,
        userName: String
    ): String {
        val completedCount = goals.count { it.isCompletedToday }
        val totalCount = goals.size
        val topStreak = goals.maxOfOrNull { it.currentStreak } ?: reportsState.currentStreak

        return """
            🚀 My Daily Goal Progress on Briefly!

            🎯 Today's Goals: $completedCount/$totalCount Completed (${reportsState.overallPercentage}%)
            🔥 Active Streak: $topStreak Days in a row!
            🏆 Total Completed: ${reportsState.totalCompleted} milestones
            
            💡 "${reportsState.motivationalQuote}"
            
            #BrieflyGoals #DailyStreak #Productivity #HabitTracker #SelfImprovement
        """.trimIndent()
    }

    /**
     * Shares report to a specific platform or opens system chooser.
     */
    fun shareReport(
        context: Context,
        platform: SocialPlatform,
        message: String,
        fileUri: Uri? = null,
        mimeType: String = "text/plain"
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (fileUri != null) mimeType else "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "My Daily Goal Progress — Briefly")
            putExtra(Intent.EXTRA_TEXT, message)
            if (fileUri != null) {
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        if (platform.packageName != null) {
            intent.setPackage(platform.packageName)
            try {
                context.startActivity(intent)
                return
            } catch (e: ActivityNotFoundException) {
                // If specific app not installed, fallback to web/general share
                if (platform == SocialPlatform.TWITTER) {
                    val tweetUrl = "https://twitter.com/intent/tweet?text=" + Uri.encode(message)
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl))
                    try {
                        context.startActivity(webIntent)
                        return
                    } catch (e2: Exception) {
                        // Fallback below
                    }
                }
                Toast.makeText(context, "${platform.displayName} is not installed. Opening share sheet...", Toast.LENGTH_SHORT).show()
            }
        }

        // Open general chooser
        val chooser = Intent.createChooser(intent, "Share Progress Report via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open share sheet", Toast.LENGTH_SHORT).show()
        }
    }
}
