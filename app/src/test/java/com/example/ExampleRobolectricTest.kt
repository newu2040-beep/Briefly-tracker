package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BrieflyDatabase
import com.example.data.local.GoalEntity
import com.example.data.preferences.UserPreferences
import com.example.data.repository.GoalRepository
import com.example.data.util.NotificationHelper
import com.example.data.util.ReportExporter
import com.example.ui.models.ReportsUiState
import com.example.ui.navigation.BrieflyApp
import com.example.ui.theme.BrieflyTheme
import com.example.ui.viewmodel.BrieflyViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Briefly", appName)
    }

    @Test
    fun `test database and repository operations`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = BrieflyDatabase.getDatabase(context)
        val repo = GoalRepository(db.goalDao())

        repo.createGoal(
            title = "Test Exercise",
            description = "Daily 30 min cardio",
            category = "Health",
            categoryColorHex = "#10B981",
            categoryIconName = "FitnessCenter",
            dailyTarget = 30,
            targetUnit = "mins",
            reminderTime = "07:00 AM",
            subtasks = listOf("Warmup", "Run")
        )

        val goals = repo.activeGoalsWithDetails.first()
        assertTrue(goals.isNotEmpty())
        val created = goals.find { it.goal.title == "Test Exercise" }
        assertNotNull(created)
        assertEquals("Health", created?.goal?.category)
        assertEquals(2, created?.subtasks?.size)
    }

    @Test
    fun `test export to txt and csv`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val (csvFile, csvUri) = ReportExporter.exportToCsv(
            context = context,
            goals = emptyList(),
            userName = "Test User"
        )
        assertTrue(csvFile.exists())
        assertNotNull(csvUri)

        val (txtFile, txtUri) = ReportExporter.exportToTxt(
            context = context,
            reportsState = ReportsUiState(),
            goals = emptyList(),
            userName = "Test User"
        )
        assertTrue(txtFile.exists())
        assertNotNull(txtUri)
    }

    @Test
    fun `test notification channel setup without crash`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NotificationHelper.createNotificationChannels(context)
    }

    @Test
    fun `test main composable hierarchy renders without crash`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = BrieflyDatabase.getDatabase(context)
        val repo = GoalRepository(db.goalDao())
        val prefs = UserPreferences(context)
        val viewModel = BrieflyViewModel(repo, prefs)

        composeTestRule.setContent {
            BrieflyTheme {
                BrieflyApp(viewModel = viewModel)
            }
        }
    }
}

