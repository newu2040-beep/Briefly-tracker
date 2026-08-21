package com.example.data.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    private val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.US)
    private val dayOfMonthFormat = SimpleDateFormat("d", Locale.US)

    fun getTodayString(): String {
        return isoFormat.format(Date())
    }

    fun formatDate(timestamp: Long): String {
        return displayFormat.format(Date(timestamp))
    }

    fun formatDateString(dateString: String): String {
        return try {
            val date = isoFormat.parse(dateString)
            if (date != null) displayFormat.format(date) else dateString
        } catch (e: Exception) {
            dateString
        }
    }

    fun getDaysAgoString(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return isoFormat.format(cal.time)
    }

    fun getPastNDays(days: Int): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -(days - 1))
        for (i in 0 until days) {
            list.add(isoFormat.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    fun getDayLabel(dateString: String): String {
        return try {
            val date = isoFormat.parse(dateString)
            if (date != null) dayOfWeekFormat.format(date) else ""
        } catch (e: Exception) {
            ""
        }
    }

    fun getDayOfMonthLabel(dateString: String): String {
        return try {
            val date = isoFormat.parse(dateString)
            if (date != null) dayOfMonthFormat.format(date) else ""
        } catch (e: Exception) {
            ""
        }
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..11 -> "Good Morning!"
            in 12..16 -> "Good Afternoon!"
            in 17..21 -> "Good Evening!"
            else -> "Good Night!"
        }
    }

    fun getGreetingEmoji(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..11 -> "👋"
            in 12..16 -> "☀️"
            in 17..21 -> "🌆"
            else -> "🌙"
        }
    }
}
