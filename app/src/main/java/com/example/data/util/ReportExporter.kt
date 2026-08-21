package com.example.data.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.ui.models.GoalItemUiState
import com.example.ui.models.ReportsUiState
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    private fun getReportsDir(context: Context): File {
        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Generates a PDF Report document and returns its FileProvider content URI.
     */
    fun exportToPdf(
        context: Context,
        reportsState: ReportsUiState,
        goals: List<GoalItemUiState>,
        userName: String
    ): Pair<File, Uri> {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (595 x 842 pt)
        val page = pdfDoc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        canvas.drawColor(Color.WHITE)

        // Header Background Banner
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4F46E5") // Indigo
        }
        val headerRect = RectF(0f, 0f, 595f, 110f)
        canvas.drawRect(headerRect, headerPaint)

        // Header Text
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        canvas.drawText("Briefly — Goal & Progress Report", 30f, 48f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy · hh:mm a", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        canvas.drawText("Generated for $userName · $dateString", 30f, 74f, paint)
        canvas.drawText("Period: ${reportsState.selectedTab.name.lowercase().replaceFirstChar { it.uppercase() }} Breakdown", 30f, 92f, paint)

        // KPI Summary Cards
        var currentY = 140f

        paint.color = Color.parseColor("#1E1B4B")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        canvas.drawText("Executive Summary", 30f, currentY, paint)

        currentY += 16f

        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFC")
        }
        val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        // Draw 3 Summary Metric Boxes
        val boxWidth = 165f
        val boxHeight = 65f

        val kpiData = listOf(
            Triple("Completion Rate", "${reportsState.overallPercentage}%", "#10B981"),
            Triple("Active Streak", "${reportsState.currentStreak} Days", "#F59E0B"),
            Triple("Total Completed", "${reportsState.totalCompleted} Goals", "#6366F1")
        )

        kpiData.forEachIndexed { index, (title, value, colorHex) ->
            val boxLeft = 30f + index * (boxWidth + 20f)
            val rect = RectF(boxLeft, currentY, boxLeft + boxWidth, currentY + boxHeight)
            canvas.drawRoundRect(rect, 10f, 10f, cardBgPaint)
            canvas.drawRoundRect(rect, 10f, 10f, cardBorderPaint)

            paint.color = Color.parseColor("#64748B")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            canvas.drawText(title, boxLeft + 14f, currentY + 22f, paint)

            paint.color = Color.parseColor(colorHex)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 18f
            canvas.drawText(value, boxLeft + 14f, currentY + 48f, paint)
        }

        currentY += boxHeight + 32f

        // Active Goals Table
        paint.color = Color.parseColor("#1E1B4B")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        canvas.drawText("Daily Goals Breakdown (${goals.size} Total)", 30f, currentY, paint)

        currentY += 16f

        // Table Header
        val thPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F1F5F9")
        }
        val thRect = RectF(30f, currentY, 565f, currentY + 26f)
        canvas.drawRoundRect(thRect, 4f, 4f, thPaint)

        paint.color = Color.parseColor("#475569")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("GOAL TITLE", 40f, currentY + 17f, paint)
        canvas.drawText("CATEGORY", 240f, currentY + 17f, paint)
        canvas.drawText("STREAK", 350f, currentY + 17f, paint)
        canvas.drawText("SUBTASKS", 430f, currentY + 17f, paint)
        canvas.drawText("STATUS", 500f, currentY + 17f, paint)

        currentY += 28f

        // Table Rows
        val rowLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F1F5F9")
            strokeWidth = 1f
        }

        goals.take(12).forEachIndexed { i, item ->
            val rowY = currentY + 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 11f
            paint.color = Color.parseColor("#0F172A")

            // Title (truncate if long)
            val title = if (item.goal.title.length > 26) item.goal.title.take(24) + "..." else item.goal.title
            canvas.drawText(title, 40f, rowY, paint)

            // Category
            paint.color = Color.parseColor("#64748B")
            canvas.drawText(item.goal.category, 240f, rowY, paint)

            // Streak
            paint.color = Color.parseColor("#D97706")
            canvas.drawText("${item.currentStreak}d (best ${item.bestStreak})", 350f, rowY, paint)

            // Subtasks
            paint.color = Color.parseColor("#475569")
            val subtaskStr = if (item.totalSubtasksCount > 0) "${item.completedSubtasksCount}/${item.totalSubtasksCount}" else "-"
            canvas.drawText(subtaskStr, 430f, rowY, paint)

            // Status
            if (item.isCompletedToday) {
                paint.color = Color.parseColor("#10B981")
                canvas.drawText("✓ Completed", 500f, rowY, paint)
            } else {
                paint.color = Color.parseColor("#94A3B8")
                canvas.drawText("Pending", 500f, rowY, paint)
            }

            canvas.drawLine(30f, rowY + 8f, 565f, rowY + 8f, rowLinePaint)
            currentY += 26f
        }

        currentY += 16f

        // Category Distribution Section
        paint.color = Color.parseColor("#1E1B4B")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        canvas.drawText("Category Allocation", 30f, currentY, paint)
        currentY += 16f

        val catSummary = reportsState.categoryDistribution.take(5).joinToString("  •  ") {
            "${it.category}: ${it.percentage}% (${it.count})"
        }
        paint.color = Color.parseColor("#475569")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        canvas.drawText(catSummary, 30f, currentY, paint)

        // Footer
        val footerY = 810f
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 9f
        canvas.drawText("Generated with Briefly Goal Tracker · Keep your streak going!", 30f, footerY, paint)
        canvas.drawText("Page 1 of 1", 520f, footerY, paint)

        pdfDoc.finishPage(page)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(getReportsDir(context), "Briefly_Report_$timeStamp.pdf")
        val outputStream = FileOutputStream(file)
        pdfDoc.writeTo(outputStream)
        outputStream.close()
        pdfDoc.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Pair(file, uri)
    }

    /**
     * Generates a CSV Report document and returns its FileProvider content URI.
     */
    fun exportToCsv(
        context: Context,
        goals: List<GoalItemUiState>,
        userName: String
    ): Pair<File, Uri> {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(getReportsDir(context), "Briefly_Goals_$timeStamp.csv")

        val writer = FileWriter(file)
        writer.append("Goal ID,Goal Title,Description,Category,Daily Target,Target Unit,Completed Today,Current Streak (Days),Best Streak (Days),Completed Subtasks,Total Subtasks,Total All-Time Completions,Created Date\n")

        for (item in goals) {
            val titleClean = escapeCsv(item.goal.title)
            val descClean = escapeCsv(item.goal.description)
            val catClean = escapeCsv(item.goal.category)
            val unitClean = escapeCsv(item.goal.targetUnit)
            val isCompleted = if (item.isCompletedToday) "YES" else "NO"
            val createdDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(item.goal.createdAt))

            writer.append("${item.goal.id},$titleClean,$descClean,$catClean,${item.goal.dailyTarget},$unitClean,$isCompleted,${item.currentStreak},${item.bestStreak},${item.completedSubtasksCount},${item.totalSubtasksCount},${item.totalCompletions},$createdDate\n")
        }

        writer.flush()
        writer.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Pair(file, uri)
    }

    /**
     * Generates a formatted TXT Report document and returns its FileProvider content URI.
     */
    fun exportToTxt(
        context: Context,
        reportsState: ReportsUiState,
        goals: List<GoalItemUiState>,
        userName: String
    ): Pair<File, Uri> {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(getReportsDir(context), "Briefly_Summary_$timeStamp.txt")

        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy · hh:mm a", Locale.getDefault())
        val dateString = dateFormat.format(Date())

        val textContent = buildString {
            append("====================================================\n")
            append("           BRIEFLY — DAILY GOAL TRACKER REPORT       \n")
            append("====================================================\n\n")
            append("User: $userName\n")
            append("Report Date: $dateString\n")
            append("Period View: ${reportsState.selectedTab.name}\n\n")

            append("----------------------------------------------------\n")
            append(" 📊 PROGRESS OVERVIEW\n")
            append("----------------------------------------------------\n")
            append("• Overall Completion Rate : ${reportsState.overallPercentage}%\n")
            append("• Active Streak           : ${reportsState.currentStreak} Days\n")
            append("• Best Streak Record      : ${reportsState.bestStreak} Days\n")
            append("• Total Goal Completions  : ${reportsState.totalCompleted}\n")
            append("• Daily Trend             : +${reportsState.progressDeltaPercentage}% vs prior period\n\n")

            append("----------------------------------------------------\n")
            append(" 🎯 ACTIVE GOALS BREAKDOWN (${goals.size} Total)\n")
            append("----------------------------------------------------\n")
            goals.forEachIndexed { index, item ->
                val statusSymbol = if (item.isCompletedToday) "[✓ DONE]" else "[  TODO]"
                append("${index + 1}. $statusSymbol ${item.goal.title}\n")
                append("   Category : ${item.goal.category}\n")
                append("   Streak   : 🔥 ${item.currentStreak} days (Best: ${item.bestStreak} days)\n")
                if (item.totalSubtasksCount > 0) {
                    append("   Subtasks : ${item.completedSubtasksCount}/${item.totalSubtasksCount} completed\n")
                    item.subtasks.forEach { sub ->
                        val subStatus = if (sub.isCompleted) "  ✓" else "  ○"
                        append("     $subStatus ${sub.title}\n")
                    }
                }
                append("\n")
            }

            append("----------------------------------------------------\n")
            append(" 📂 CATEGORY DISTRIBUTION\n")
            append("----------------------------------------------------\n")
            reportsState.categoryDistribution.forEach { cat ->
                append("• ${cat.category.padEnd(14)} : ${cat.percentage}% (${cat.count} check-ins)\n")
            }

            append("\n----------------------------------------------------\n")
            append(" 💡 MOTIVATIONAL INSIGHT\n")
            append("----------------------------------------------------\n")
            append("\"${reportsState.motivationalQuote}\"\n\n")
            append("Generated by Briefly — Small Goals. Big Progress.\n")
        }

        val writer = FileWriter(file)
        writer.write(textContent)
        writer.flush()
        writer.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Pair(file, uri)
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
