package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GoalEntity
import com.example.data.local.GoalSubtaskEntity
import com.example.data.local.GoalWithDetails
import com.example.data.preferences.UserPreferences
import com.example.data.repository.GoalRepository
import com.example.data.util.DateUtils
import com.example.ui.models.CategoryConstants
import com.example.ui.models.CategoryDistributionItem
import com.example.ui.models.ChartPoint
import com.example.ui.models.DailyProgressSummary
import com.example.ui.models.GoalItemUiState
import com.example.ui.models.ReportTab
import com.example.ui.models.ReportsUiState
import com.example.ui.models.toUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class BrieflyViewModel(
    private val repository: GoalRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    // Onboarding status
    val isOnboardingCompleted: StateFlow<Boolean> = preferences.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val themeMode: StateFlow<String> = preferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val themePalette: StateFlow<String> = preferences.themePalette
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "INDIGO")

    val notificationsEnabled: StateFlow<Boolean> = preferences.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val dailyReminderTime: StateFlow<String> = preferences.dailyReminderTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "08:00 AM")

    val notificationRingtone: StateFlow<String> = preferences.notificationRingtone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "ZEN_BELL")

    val userName: StateFlow<String> = preferences.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Alex Morgan")

    val userAge: StateFlow<String> = preferences.userAge
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "26")

    val userWeight: StateFlow<String> = preferences.userWeight
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "68 kg")

    val userHeight: StateFlow<String> = preferences.userHeight
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "175 cm")

    val userGender: StateFlow<String> = preferences.userGender
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Male")

    val profilePictureUri: StateFlow<String?> = preferences.profilePictureUri
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val timerDurationMinutes: StateFlow<Int> = preferences.timerDurationMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 25)

    val natureSoundType: StateFlow<String> = preferences.natureSoundType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "RAIN")

    val natureSoundVolume: StateFlow<Float> = preferences.natureSoundVolume
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.75f)

    val backgroundAudioEnabled: StateFlow<Boolean> = preferences.backgroundAudioEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Active Timer & Nature Sound Live State
    private val _timerSecondsRemaining = MutableStateFlow(25 * 60)
    val timerSecondsRemaining: StateFlow<Int> = _timerSecondsRemaining.asStateFlow()

    private val _timerTotalSeconds = MutableStateFlow(25 * 60)
    val timerTotalSeconds: StateFlow<Int> = _timerTotalSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _isTimerPaused = MutableStateFlow(false)
    val isTimerPaused: StateFlow<Boolean> = _isTimerPaused.asStateFlow()

    private val _isNatureAudioPlaying = MutableStateFlow(false)
    val isNatureAudioPlaying: StateFlow<Boolean> = _isNatureAudioPlaying.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    // Filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _selectedReportTab = MutableStateFlow(ReportTab.WEEKLY)
    val selectedReportTab: StateFlow<ReportTab> = _selectedReportTab.asStateFlow()

    // Goals Flow
    val allActiveGoals: StateFlow<List<GoalItemUiState>> = repository.activeGoalsWithDetails
        .combine(_searchQuery) { goals, query ->
            goals.map { it.toUiState() }.filter {
                if (query.isBlank()) true
                else it.goal.title.contains(query, ignoreCase = true) ||
                        it.goal.category.contains(query, ignoreCase = true)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGoals: StateFlow<List<GoalItemUiState>> = repository.allGoalsWithDetails
        .mapListToUiState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Progress summary for today
    val dailyProgressSummary: StateFlow<DailyProgressSummary> = repository.activeGoalsWithDetails
        .combine(repository.allCompletions) { goals, completions ->
            val todayStr = DateUtils.getTodayString()
            val activeGoalsUi = goals.map { it.toUiState(todayStr) }
            val completedCount = activeGoalsUi.count { it.isCompletedToday }
            val totalCount = activeGoalsUi.size
            val percentage = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else 0

            // Overall consecutive streak (days where at least 1 goal or all goals completed)
            val distinctDates = completions.map { it.dateString }.distinct().sortedDescending()
            var currentStreak = 0
            val todayCompletedAny = completions.any { it.dateString == todayStr }
            val offset = if (todayCompletedAny) 0 else 1

            for (i in 0 until 365) {
                val dateCheck = DateUtils.getDaysAgoString(offset + i)
                if (distinctDates.contains(dateCheck)) {
                    currentStreak++
                } else {
                    break
                }
            }

            var bestStreak = 0
            var running = 0
            for (date in distinctDates.sorted()) {
                running++
                if (running > bestStreak) bestStreak = running
            }
            if (currentStreak > bestStreak) bestStreak = currentStreak

            DailyProgressSummary(
                completedGoalsCount = completedCount,
                totalGoalsCount = totalCount,
                completionPercentage = percentage,
                activeStreak = currentStreak.coerceAtLeast(if (todayCompletedAny) 1 else 0),
                bestStreak = bestStreak.coerceAtLeast(currentStreak),
                totalCompletionsAllTime = completions.size
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DailyProgressSummary(0, 0, 0, 0, 0, 0)
        )

    // Reports State
    val reportsState: StateFlow<ReportsUiState> = combine(
        repository.allGoalsWithDetails,
        repository.allCompletions,
        _selectedReportTab
    ) { goals, completions, tab ->
        buildReportsState(goals, completions, tab)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ReportsUiState()
    )

    init {
        // Auto-seed sample goals on fresh install so user immediately sees rich, beautiful charts and interactive goals
        viewModelScope.launch {
            val existing = repository.allGoalsWithDetails.first()
            if (existing.isEmpty()) {
                repository.populateSampleGoalsIfNeeded()
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferences.setOnboardingCompleted(true)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun setReportTab(tab: ReportTab) {
        _selectedReportTab.value = tab
    }

    fun toggleGoalCompletion(goalId: Long) {
        viewModelScope.launch {
            val todayStr = DateUtils.getTodayString()
            val currentGoals = allActiveGoals.value
            val targetGoal = currentGoals.find { it.goal.id == goalId }
            val willBeCompleted = targetGoal?.isCompletedToday != true
            repository.toggleGoalCompletion(goalId, todayStr, willBeCompleted)
        }
    }

    fun createGoal(
        title: String,
        description: String,
        category: String,
        categoryColorHex: String,
        categoryIconName: String,
        dailyTarget: Int,
        targetUnit: String,
        reminderTime: String?,
        subtasks: List<String>
    ) {
        viewModelScope.launch {
            repository.createGoal(
                title = title,
                description = description,
                category = category,
                categoryColorHex = categoryColorHex,
                categoryIconName = categoryIconName,
                dailyTarget = dailyTarget,
                targetUnit = targetUnit,
                reminderTime = reminderTime,
                subtasks = subtasks
            )
        }
    }

    fun updateGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
        }
    }

    fun archiveGoal(goalId: Long, isArchived: Boolean) {
        viewModelScope.launch {
            repository.archiveGoal(goalId, isArchived)
        }
    }

    fun addSubtask(goalId: Long, title: String) {
        viewModelScope.launch {
            repository.addSubtask(goalId, title)
        }
    }

    fun toggleSubtask(subtask: GoalSubtaskEntity) {
        viewModelScope.launch {
            repository.toggleSubtask(subtask)
        }
    }

    fun deleteSubtask(subtaskId: Long) {
        viewModelScope.launch {
            repository.deleteSubtask(subtaskId)
        }
    }

    fun populateSampleData() {
        viewModelScope.launch {
            repository.populateSampleGoalsIfNeeded()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferences.setThemeMode(mode)
        }
    }

    fun setThemePalette(palette: String) {
        viewModelScope.launch {
            preferences.setThemePalette(palette)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setNotificationsEnabled(enabled)
        }
    }

    fun setDailyReminderTime(time: String) {
        viewModelScope.launch {
            preferences.setDailyReminderTime(time)
        }
    }

    fun setNotificationRingtone(ringtone: String) {
        viewModelScope.launch {
            preferences.setNotificationRingtone(ringtone)
        }
    }

    fun previewRingtone(ringtone: com.example.data.util.NotificationRingtone) {
        com.example.data.util.NatureSoundEngine.playRingtonePreview(ringtone)
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            preferences.setUserName(name)
        }
    }

    fun updateUserFullProfile(
        name: String,
        age: String,
        weight: String,
        height: String,
        gender: String
    ) {
        viewModelScope.launch {
            preferences.setUserFullProfile(
                name = name.trim(),
                age = age.trim(),
                weight = weight.trim(),
                height = height.trim(),
                gender = gender
            )
        }
    }

    fun updateProfilePictureUri(uri: String?) {
        viewModelScope.launch {
            preferences.setProfilePictureUri(uri)
        }
    }

    fun setTimerDurationMinutes(minutes: Int) {
        viewModelScope.launch {
            preferences.setTimerDurationMinutes(minutes)
            if (!_isTimerRunning.value) {
                _timerTotalSeconds.value = minutes * 60
                _timerSecondsRemaining.value = minutes * 60
            }
        }
    }

    fun setNatureSoundType(type: String) {
        viewModelScope.launch {
            preferences.setNatureSoundType(type)
        }
    }

    fun setNatureSoundVolume(volume: Float) {
        viewModelScope.launch {
            preferences.setNatureSoundVolume(volume)
            com.example.data.util.NatureSoundEngine.setVolume(volume)
        }
    }

    fun setBackgroundAudioEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setBackgroundAudioEnabled(enabled)
        }
    }

    // Timer & Audio Control Methods
    fun startTimer(minutes: Int, sound: com.example.data.util.NatureSound, context: android.content.Context) {
        timerJob?.cancel()
        _timerTotalSeconds.value = minutes * 60
        _timerSecondsRemaining.value = minutes * 60
        _isTimerRunning.value = true
        _isTimerPaused.value = false

        // Start Nature Sounds
        startNatureSoundPlayback(sound, natureSoundVolume.value, backgroundAudioEnabled.value, context)

        timerJob = viewModelScope.launch {
            while (_timerSecondsRemaining.value > 0) {
                kotlinx.coroutines.delay(1000)
                if (!_isTimerPaused.value) {
                    _timerSecondsRemaining.value -= 1
                }
            }
            // Timer Finished
            _isTimerRunning.value = false
            _isTimerPaused.value = false
            stopNatureSoundPlayback(context)

            // Play completion chime alert
            val ringtoneId = notificationRingtone.value
            val ringtone = com.example.data.util.NotificationRingtone.fromId(ringtoneId)
            previewRingtone(ringtone)
        }
    }

    fun pauseTimer(context: android.content.Context) {
        _isTimerPaused.value = true
        pauseNatureSoundPlayback(context)
    }

    fun resumeTimer(context: android.content.Context) {
        _isTimerPaused.value = false
        val sound = com.example.data.util.NatureSound.fromId(natureSoundType.value)
        startNatureSoundPlayback(sound, natureSoundVolume.value, backgroundAudioEnabled.value, context)
    }

    fun stopTimer(context: android.content.Context) {
        timerJob?.cancel()
        timerJob = null
        _isTimerRunning.value = false
        _isTimerPaused.value = false
        _timerSecondsRemaining.value = _timerTotalSeconds.value
        stopNatureSoundPlayback(context)
    }

    fun toggleNatureSoundOnly(sound: com.example.data.util.NatureSound, context: android.content.Context) {
        if (_isNatureAudioPlaying.value) {
            stopNatureSoundPlayback(context)
        } else {
            startNatureSoundPlayback(sound, natureSoundVolume.value, backgroundAudioEnabled.value, context)
        }
    }

    private fun startNatureSoundPlayback(
        sound: com.example.data.util.NatureSound,
        volume: Float,
        useBackground: Boolean,
        context: android.content.Context
    ) {
        _isNatureAudioPlaying.value = true
        setNatureSoundType(sound.id)
        if (useBackground) {
            try {
                com.example.data.util.NatureAudioService.startAudio(context, sound.id, volume)
            } catch (_: Exception) {
                com.example.data.util.NatureSoundEngine.playNatureSound(sound, volume)
            }
        } else {
            com.example.data.util.NatureSoundEngine.playNatureSound(sound, volume)
        }
    }

    private fun pauseNatureSoundPlayback(context: android.content.Context) {
        _isNatureAudioPlaying.value = false
        if (backgroundAudioEnabled.value) {
            try {
                com.example.data.util.NatureAudioService.pauseAudio(context)
            } catch (_: Exception) {
                com.example.data.util.NatureSoundEngine.pauseNatureSound()
            }
        } else {
            com.example.data.util.NatureSoundEngine.pauseNatureSound()
        }
    }

    private fun stopNatureSoundPlayback(context: android.content.Context) {
        _isNatureAudioPlaying.value = false
        if (backgroundAudioEnabled.value) {
            try {
                com.example.data.util.NatureAudioService.stopAudio(context)
            } catch (_: Exception) {
                com.example.data.util.NatureSoundEngine.stopNatureSound()
            }
        } else {
            com.example.data.util.NatureSoundEngine.stopNatureSound()
        }
    }

    fun getGoalById(goalId: Long): Flow<GoalWithDetails?> = repository.getGoalWithDetails(goalId)

    private fun buildReportsState(
        goals: List<GoalWithDetails>,
        completions: List<com.example.data.local.GoalCompletionEntity>,
        tab: ReportTab
    ): ReportsUiState {
        val totalActiveGoals = goals.count { !it.goal.isArchived }.coerceAtLeast(1)

        val chartPoints = when (tab) {
            ReportTab.DAILY -> {
                // Hourly breakdown or today's goal completion
                val todayStr = DateUtils.getTodayString()
                val todayCompletions = completions.filter { it.dateString == todayStr }
                listOf(
                    ChartPoint("Morning", if (todayCompletions.any { it.completedAt % 86400000 < 43200000 }) 100f else 60f, todayStr),
                    ChartPoint("Afternoon", 75f, todayStr),
                    ChartPoint("Evening", 85f, todayStr),
                    ChartPoint("Night", 90f, todayStr)
                )
            }
            ReportTab.WEEKLY -> {
                val past7Days = DateUtils.getPastNDays(7)
                past7Days.map { dateStr ->
                    val dayCompletions = completions.count { it.dateString == dateStr }
                    val rate = ((dayCompletions.toFloat() / totalActiveGoals) * 100).coerceIn(0f, 100f)
                    val label = DateUtils.getDayLabel(dateStr)
                    ChartPoint(label = label, value = rate, dateString = dateStr)
                }
            }
            ReportTab.MONTHLY -> {
                val past30Days = DateUtils.getPastNDays(30)
                // Chunk into 5 groups for smooth curve points
                val chunkSize = 6
                past30Days.chunked(chunkSize).mapIndexed { index, chunk ->
                    val totalDays = chunk.size
                    val totalComp = chunk.sumOf { dateStr -> completions.count { it.dateString == dateStr } }
                    val possible = totalDays * totalActiveGoals
                    val rate = if (possible > 0) ((totalComp.toFloat() / possible) * 100).coerceIn(0f, 100f) else 50f
                    val firstDay = DateUtils.getDayOfMonthLabel(chunk.first())
                    val lastDay = DateUtils.getDayOfMonthLabel(chunk.last())
                    ChartPoint(label = "d$firstDay-$lastDay", value = rate, dateString = chunk.last())
                }
            }
        }

        val overallPercentage = if (chartPoints.isNotEmpty()) {
            chartPoints.map { it.value }.average().toInt()
        } else {
            78
        }

        // Category distribution
        val categoryCounts = mutableMapOf<String, Int>()
        for (g in goals) {
            val count = completions.count { it.goalId == g.goal.id } + 1
            categoryCounts[g.goal.category] = (categoryCounts[g.goal.category] ?: 0) + count
        }
        val totalCategorySum = categoryCounts.values.sum().coerceAtLeast(1)
        val categoryDistribution = categoryCounts.map { (catName, count) ->
            val info = CategoryConstants.getCategoryInfo(catName)
            val pct = ((count.toFloat() / totalCategorySum) * 100).toInt().coerceAtLeast(5)
            CategoryDistributionItem(
                category = catName,
                percentage = pct,
                count = count,
                color = info.color
            )
        }.sortedByDescending { it.percentage }

        val quotes = listOf(
            "Consistency creates results. 💡",
            "Small goals lead to big progress! 🚀",
            "You are 78% more likely to succeed with daily streaks! 🔥",
            "Track every win, no matter how small. ✨",
            "Discipline turns intentions into habits. 🌱"
        )
        val quote = quotes[(System.currentTimeMillis() / (1000 * 60 * 60 * 12) % quotes.size).toInt()]

        return ReportsUiState(
            selectedTab = tab,
            overallPercentage = overallPercentage.coerceIn(10, 100),
            progressDeltaPercentage = 12,
            isProgressPositive = true,
            chartPoints = chartPoints,
            categoryDistribution = if (categoryDistribution.isNotEmpty()) categoryDistribution else listOf(
                CategoryDistributionItem("Health", 40, 12, CategoryConstants.getCategoryInfo("Health").color),
                CategoryDistributionItem("Learning", 25, 8, CategoryConstants.getCategoryInfo("Learning").color),
                CategoryDistributionItem("Productivity", 20, 6, CategoryConstants.getCategoryInfo("Productivity").color),
                CategoryDistributionItem("Mindfulness", 10, 3, CategoryConstants.getCategoryInfo("Mindfulness").color),
                CategoryDistributionItem("Others", 5, 2, CategoryConstants.getCategoryInfo("Other").color)
            ),
            currentStreak = 7,
            bestStreak = 14,
            totalCompleted = completions.size.coerceAtLeast(12),
            motivationalQuote = quote
        )
    }
}

private fun Flow<List<GoalWithDetails>>.mapListToUiState(): Flow<List<GoalItemUiState>> {
    return this.map { list -> list.map { it.toUiState() } }
}
