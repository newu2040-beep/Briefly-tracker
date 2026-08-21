package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.util.NotificationHelper
import com.example.ui.models.GoalItemUiState
import com.example.ui.theme.EmeraldMindfulness
import com.example.ui.theme.IndigoPrimary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveGoalTrackerBottomSheet(
    goalItem: GoalItemUiState,
    onCompleteGoal: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Preset durations in minutes: 5m, 15m, 25m, 30m, 45m, 60m, or Stopwatch (0 = open-ended)
    var selectedTargetMinutes by remember { mutableLongStateOf(25L) }
    var isRunning by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var isTimerMode by remember { mutableStateOf(true) } // true: countdown, false: stopwatch

    val totalSeconds = if (isTimerMode) selectedTargetMinutes * 60 else 0L

    // Ticking timer coroutine
    LaunchedEffect(isRunning, isTimerMode, selectedTargetMinutes) {
        while (isRunning) {
            delay(1000L)
            elapsedSeconds++

            // Update live notification
            NotificationHelper.updateLiveTrackingNotification(
                context = context,
                goalTitle = goalItem.goal.title,
                elapsedSeconds = elapsedSeconds,
                totalSeconds = totalSeconds,
                isPaused = !isRunning
            )

            // Check if countdown completed
            if (isTimerMode && totalSeconds > 0 && elapsedSeconds >= totalSeconds) {
                isRunning = false
                NotificationHelper.sendTestNotification(
                    context,
                    "🎯 Goal Session Finished!",
                    "You've completed $selectedTargetMinutes mins of tracking for '${goalItem.goal.title}'!"
                )
                Toast.makeText(context, "🎉 Tracking session complete!", Toast.LENGTH_LONG).show()
                break
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            NotificationHelper.cancelLiveTrackingNotification(context)
        }
    }

    val progressRatio: Float = if (isTimerMode && totalSeconds > 0) {
        (elapsedSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else {
        ((elapsedSeconds % 60).toFloat() / 60f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        label = "liveProgress"
    )

    val remainingSeconds = if (isTimerMode && totalSeconds > 0) {
        (totalSeconds - elapsedSeconds).coerceAtLeast(0L)
    } else {
        elapsedSeconds
    }

    val displayMinutes = remainingSeconds / 60
    val displaySecs = remainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", displayMinutes, displaySecs)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("live_goal_tracker_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = IndigoPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Real-Time Tracking",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = goalItem.goal.title,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Mode Selector (Countdown Timer vs Free Stopwatch)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isTimerMode) MaterialTheme.colorScheme.surface else Color.Transparent,
                        onClick = {
                            if (!isRunning) {
                                isTimerMode = true
                                elapsedSeconds = 0L
                            }
                        }
                    ) {
                        Text(
                            text = "⏱ Focus Timer",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isTimerMode) FontWeight.Bold else FontWeight.Medium,
                                color = if (isTimerMode) IndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isTimerMode) MaterialTheme.colorScheme.surface else Color.Transparent,
                        onClick = {
                            if (!isRunning) {
                                isTimerMode = false
                                elapsedSeconds = 0L
                            }
                        }
                    ) {
                        Text(
                            text = "⏲ Stopwatch",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (!isTimerMode) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isTimerMode) IndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Duration Presets (only when timer mode and not running)
            if (isTimerMode && !isRunning) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    listOf(5L, 15L, 25L, 30L, 45L).forEach { mins ->
                        val isSelected = selectedTargetMinutes == mins
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.clickable {
                                selectedTargetMinutes = mins
                                elapsedSeconds = 0L
                            }
                        ) {
                            Text(
                                text = "${mins}m",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Big Circular Timer Display
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(210.dp)
                    .padding(8.dp)
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(200.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 12.dp,
                )

                // Animated Progress Arc
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(200.dp),
                    color = if (isTimerMode) IndigoPrimary else EmeraldMindfulness,
                    strokeWidth = 12.dp,
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isRunning) "TRACKING IN PROGRESS" else if (elapsedSeconds > 0) "PAUSED" else "READY TO START",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isRunning) EmeraldMindfulness else MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Control Buttons: Start/Pause, Reset, Complete Goal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = {
                        isRunning = false
                        elapsedSeconds = 0L
                        NotificationHelper.cancelLiveTrackingNotification(context)
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Play / Pause Main Button
                Button(
                    onClick = {
                        isRunning = !isRunning
                        if (isRunning) {
                            NotificationHelper.sendTestNotification(
                                context,
                                "⏱ Live Tracking Started",
                                "Now tracking '${goalItem.goal.title}'"
                            )
                        } else {
                            NotificationHelper.cancelLiveTrackingNotification(context)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) Color(0xFFF59E0B) else IndigoPrimary
                    ),
                    modifier = Modifier
                        .height(54.dp)
                        .weight(1f)
                        .testTag("toggle_live_timer_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Start",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) "Pause Session" else "Start Session",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }

                // Finish and Complete Goal Button
                IconButton(
                    onClick = {
                        isRunning = false
                        NotificationHelper.cancelLiveTrackingNotification(context)
                        onCompleteGoal(goalItem.goal.id)
                        Toast.makeText(context, "🎉 Goal Marked Completed!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(EmeraldMindfulness.copy(alpha = 0.15f))
                        .testTag("finish_and_complete_goal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Complete Goal",
                        tint = EmeraldMindfulness,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtask check-off progress card inside tracker
            if (goalItem.totalSubtasksCount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Subtasks Done",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "${goalItem.completedSubtasksCount} of ${goalItem.totalSubtasksCount}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = IndigoPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
