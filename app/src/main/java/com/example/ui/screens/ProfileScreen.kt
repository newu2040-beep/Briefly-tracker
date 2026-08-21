package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.util.NotificationHelper
import com.example.data.util.ProfilePictureHelper
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.ExportAndShareBottomSheet
import com.example.ui.components.NotificationRingtoneCard
import com.example.ui.components.PermissionsManagementBottomSheet
import com.example.ui.components.ThemePaletteSelectorCard
import com.example.ui.components.TimerNatureSoundSection
import com.example.ui.theme.EmeraldMindfulness
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.BrieflyViewModel
import java.io.File

@Composable
fun ProfileScreen(
    viewModel: BrieflyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Profile State
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userAge by viewModel.userAge.collectAsStateWithLifecycle()
    val userWeight by viewModel.userWeight.collectAsStateWithLifecycle()
    val userHeight by viewModel.userHeight.collectAsStateWithLifecycle()
    val userGender by viewModel.userGender.collectAsStateWithLifecycle()
    val profilePictureUri by viewModel.profilePictureUri.collectAsStateWithLifecycle()

    // Themes & Sound State
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val themePalette by viewModel.themePalette.collectAsStateWithLifecycle()
    val notificationRingtone by viewModel.notificationRingtone.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val reminderTime by viewModel.dailyReminderTime.collectAsStateWithLifecycle()

    // Timer & Nature Sounds State
    val timerSecondsRemaining by viewModel.timerSecondsRemaining.collectAsStateWithLifecycle()
    val timerTotalSeconds by viewModel.timerTotalSeconds.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val isTimerPaused by viewModel.isTimerPaused.collectAsStateWithLifecycle()
    val isNatureAudioPlaying by viewModel.isNatureAudioPlaying.collectAsStateWithLifecycle()
    val natureSoundType by viewModel.natureSoundType.collectAsStateWithLifecycle()
    val natureSoundVolume by viewModel.natureSoundVolume.collectAsStateWithLifecycle()
    val backgroundAudioEnabled by viewModel.backgroundAudioEnabled.collectAsStateWithLifecycle()

    // Summary & Reports
    val summary by viewModel.dailyProgressSummary.collectAsStateWithLifecycle()
    val reportsState by viewModel.reportsState.collectAsStateWithLifecycle()
    val allGoals by viewModel.allActiveGoals.collectAsStateWithLifecycle()

    // Dialog & Sheet States
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showPermissionsSheet by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    var showPhotoOptionsDropdown by remember { mutableStateOf(false) }

    // Modern Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ProfilePictureHelper.saveImageToInternalStorage(context, uri)
            if (savedPath != null) {
                viewModel.updateProfilePictureUri(savedPath)
                Toast.makeText(context, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Legacy Storage / Gallery Permission Launcher
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            Toast.makeText(context, "Gallery permission is required to select photos", Toast.LENGTH_LONG).show()
        }
    }

    fun openGalleryWithPermissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            // Android 12 and below
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Profile & Settings",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            // Edit Profile Quick Button
            OutlinedButton(
                onClick = { showEditProfileDialog = true },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("edit_full_profile_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Full User Profile Card (Avatar, Photo Upload, Age, Weight, Height, Gender)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar with Gallery Upload Overlay Badge
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .clickable { showPhotoOptionsDropdown = true }
                                    .testTag("profile_avatar_image"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!profilePictureUri.isNullOrBlank() && File(profilePictureUri!!).exists()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(File(profilePictureUri!!))
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "User profile photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(76.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    // Default Logo / Initials
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_briefly_logo),
                                        contentDescription = "Default Avatar",
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                    )
                                }

                                // Camera / Upload Badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .clickable { openGalleryWithPermissionCheck() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Upload photo from gallery",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                // Photo Dropdown Menu
                                DropdownMenu(
                                    expanded = showPhotoOptionsDropdown,
                                    onDismissRequest = { showPhotoOptionsDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Choose from Gallery") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.PhotoLibrary,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showPhotoOptionsDropdown = false
                                            openGalleryWithPermissionCheck()
                                        },
                                        modifier = Modifier.testTag("choose_photo_from_gallery_menu")
                                    )
                                    if (!profilePictureUri.isNullOrBlank()) {
                                        DropdownMenuItem(
                                            text = { Text("Remove Custom Photo") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            onClick = {
                                                showPhotoOptionsDropdown = false
                                                viewModel.updateProfilePictureUri(null)
                                                ProfilePictureHelper.clearSavedProfilePictures(context)
                                                Toast.makeText(context, "Profile picture reset", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = userName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Gender Badge (Male / Female)
                                val isMale = userGender.equals("Male", ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isMale) IndigoPrimary.copy(alpha = 0.12f) else Color(0xFFEC4899).copy(alpha = 0.12f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isMale) Icons.Default.Male else Icons.Default.Female,
                                            contentDescription = userGender,
                                            tint = if (isMale) IndigoPrimary else Color(0xFFEC4899),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = userGender,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isMale) IndigoPrimary else Color(0xFFEC4899),
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // User Full Bio Stats Row (Age, Weight, Height)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            UserBioMetricPill(
                                label = "Age",
                                value = "$userAge yrs",
                                icon = Icons.Default.Numbers,
                                modifier = Modifier.weight(1f)
                            )
                            UserBioMetricPill(
                                label = "Weight",
                                value = userWeight,
                                icon = Icons.Default.FitnessCenter,
                                modifier = Modifier.weight(1f)
                            )
                            UserBioMetricPill(
                                label = "Height",
                                value = userHeight,
                                icon = Icons.Default.Height,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Streak & Completions Stats Summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBox(
                        title = "Streak",
                        value = "${summary.activeStreak}d",
                        icon = Icons.Default.LocalFireDepartment,
                        iconColor = Color(0xFFFF8E3C),
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Completions",
                        value = "${summary.totalCompletionsAllTime}",
                        icon = Icons.Default.CheckCircle,
                        iconColor = EmeraldMindfulness,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Best Run",
                        value = "${summary.bestStreak}d",
                        icon = Icons.Default.LocalFireDepartment,
                        iconColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Interactive Timer & Built-In Peaceful Nature Sounds Section
            item {
                TimerNatureSoundSection(
                    secondsRemaining = timerSecondsRemaining,
                    totalSeconds = timerTotalSeconds,
                    isTimerRunning = isTimerRunning,
                    isTimerPaused = isTimerPaused,
                    isNatureAudioPlaying = isNatureAudioPlaying,
                    selectedSoundId = natureSoundType,
                    volume = natureSoundVolume,
                    isBackgroundAudioEnabled = backgroundAudioEnabled,
                    onStartTimer = { mins, sound, ctx -> viewModel.startTimer(mins, sound, ctx) },
                    onPauseTimer = { ctx -> viewModel.pauseTimer(ctx) },
                    onResumeTimer = { ctx -> viewModel.resumeTimer(ctx) },
                    onStopTimer = { ctx -> viewModel.stopTimer(ctx) },
                    onToggleSoundOnly = { sound, ctx -> viewModel.toggleNatureSoundOnly(sound, ctx) },
                    onSelectSoundType = { soundId -> viewModel.setNatureSoundType(soundId) },
                    onVolumeChange = { vol -> viewModel.setNatureSoundVolume(vol) },
                    onToggleBackgroundAudio = { bg -> viewModel.setBackgroundAudioEnabled(bg) },
                    onSelectDurationMinutes = { mins -> viewModel.setTimerDurationMinutes(mins) }
                )
            }

            // Preferences Header
            item {
                Text(
                    text = "Customization & Theme Palettes",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Theme Mode (System / Light / Dark) Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Display Mode",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeOptionButton(
                                title = "System",
                                isSelected = themeMode == "SYSTEM",
                                onClick = { viewModel.setThemeMode("SYSTEM") },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeOptionButton(
                                title = "Light",
                                isSelected = themeMode == "LIGHT",
                                onClick = { viewModel.setThemeMode("LIGHT") },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeOptionButton(
                                title = "Dark",
                                isSelected = themeMode == "DARK",
                                onClick = { viewModel.setThemeMode("DARK") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 7 Color Theme Palettes
            item {
                ThemePaletteSelectorCard(
                    currentPaletteId = themePalette,
                    onSelectPalette = { paletteId -> viewModel.setThemePalette(paletteId) }
                )
            }

            // Notification Ringtone Chimes & Alerts Section
            item {
                NotificationRingtoneCard(
                    currentRingtoneId = notificationRingtone,
                    onSelectRingtone = { ringtoneId -> viewModel.setNotificationRingtone(ringtoneId) },
                    onPreviewSound = { ringtone -> viewModel.previewRingtone(ringtone) }
                )
            }

            // Notifications Status & Schedule Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Goal Daily Reminders",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = if (notificationsEnabled) "Active at $reminderTime" else "Disabled",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                                modifier = Modifier.testTag("notifications_toggle_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Advanced Permissions & Notification Center Button
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showPermissionsSheet = true }
                                .testTag("profile_permissions_hub_button"),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "System Permissions & Alerts Hub",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        Text(
                                            text = "Manage notifications, exact alarms & photo access",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Export & Sharing Section
            item {
                Text(
                    text = "Reports & Sharing",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { showExportSheet = true }
                        .testTag("profile_export_and_share_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Export & Share",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = "Export Reports & Share",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = "Generate PDF, CSV, TXT or share streaks",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldMindfulness.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "PDF • CSV",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldMindfulness,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Data & Storage Header
            item {
                Text(
                    text = "Data & Privacy",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Demo Data & Reset Data Buttons
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Load Sample Data Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.populateSampleData()
                                    Toast.makeText(context, "Sample goals & streaks loaded!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Load Sample Goals & History",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Populate example goals to explore reports and streaks",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Reset Database Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showResetConfirmDialog = true }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Clear All Goals & History",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                )
                                Text(
                                    text = "Permanently reset all tracked data",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // About Briefly & Privacy
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Privacy & Local Storage",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All goals, personal info, and nature sound preferences remain stored locally on your device.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }

    // Edit Full Profile Dialog (Name, Age, Weight, Height, Gender)
    if (showEditProfileDialog) {
        EditProfileDialog(
            initialName = userName,
            initialAge = userAge,
            initialWeight = userWeight,
            initialHeight = userHeight,
            initialGender = userGender,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, age, weight, height, gender ->
                viewModel.updateUserFullProfile(name, age, weight, height, gender)
                showEditProfileDialog = false
                Toast.makeText(context, "Profile information saved!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Clear All Data?") },
            text = { Text("This will erase all your goals, subtasks, and completion logs. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "All data erased", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPermissionsSheet) {
        PermissionsManagementBottomSheet(
            onDismiss = { showPermissionsSheet = false }
        )
    }

    if (showExportSheet) {
        ExportAndShareBottomSheet(
            reportsState = reportsState,
            goals = allGoals,
            userName = userName,
            onDismiss = { showExportSheet = false }
        )
    }
}

@Composable
private fun UserBioMetricPill(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp
                )
            )
        }
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun ThemeOptionButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
