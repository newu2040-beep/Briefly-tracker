package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "briefly_user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK"
        val KEY_THEME_PALETTE = stringPreferencesKey("theme_palette") // "INDIGO", "EMERALD", "OCEAN", "SUNSET", "AMETHYST", "ROSE", "MONOCHROME"
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_DAILY_REMINDER_TIME = stringPreferencesKey("daily_reminder_time")
        val KEY_NOTIFICATION_RINGTONE = stringPreferencesKey("notification_ringtone")
        
        // Full User Profile Information
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_AGE = stringPreferencesKey("user_age")
        val KEY_USER_WEIGHT = stringPreferencesKey("user_weight")
        val KEY_USER_HEIGHT = stringPreferencesKey("user_height")
        val KEY_USER_GENDER = stringPreferencesKey("user_gender") // "Male" or "Female"
        val KEY_PROFILE_PICTURE_URI = stringPreferencesKey("profile_picture_uri")
        val KEY_USER_MOTIVATION = stringPreferencesKey("user_motivation")

        // Nature Sound & Timer Preferences
        val KEY_TIMER_DURATION_MINUTES = intPreferencesKey("timer_duration_minutes")
        val KEY_NATURE_SOUND_TYPE = stringPreferencesKey("nature_sound_type")
        val KEY_NATURE_SOUND_VOLUME = floatPreferencesKey("nature_sound_volume")
        val KEY_BACKGROUND_AUDIO_ENABLED = booleanPreferencesKey("background_audio_enabled")
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: "SYSTEM"
    }

    val themePalette: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_PALETTE] ?: "INDIGO"
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    val dailyReminderTime: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_DAILY_REMINDER_TIME] ?: "08:00 AM"
    }

    val notificationRingtone: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATION_RINGTONE] ?: "ZEN_BELL"
    }

    val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_NAME] ?: "Alex Morgan"
    }

    val userAge: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_AGE] ?: "26"
    }

    val userWeight: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_WEIGHT] ?: "68 kg"
    }

    val userHeight: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_HEIGHT] ?: "175 cm"
    }

    val userGender: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_GENDER] ?: "Male"
    }

    val profilePictureUri: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_PROFILE_PICTURE_URI]
    }

    val timerDurationMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_TIMER_DURATION_MINUTES] ?: 25
    }

    val natureSoundType: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_NATURE_SOUND_TYPE] ?: "RAIN"
    }

    val natureSoundVolume: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[KEY_NATURE_SOUND_VOLUME] ?: 0.75f
    }

    val backgroundAudioEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_BACKGROUND_AUDIO_ENABLED] ?: true
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    suspend fun setThemePalette(palette: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_PALETTE] = palette
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setDailyReminderTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DAILY_REMINDER_TIME] = time
        }
    }

    suspend fun setNotificationRingtone(ringtone: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_RINGTONE] = ringtone
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = name
        }
    }

    suspend fun setUserFullProfile(
        name: String,
        age: String,
        weight: String,
        height: String,
        gender: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = name
            preferences[KEY_USER_AGE] = age
            preferences[KEY_USER_WEIGHT] = weight
            preferences[KEY_USER_HEIGHT] = height
            preferences[KEY_USER_GENDER] = gender
        }
    }

    suspend fun setProfilePictureUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri == null) {
                preferences.remove(KEY_PROFILE_PICTURE_URI)
            } else {
                preferences[KEY_PROFILE_PICTURE_URI] = uri
            }
        }
    }

    suspend fun setTimerDurationMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TIMER_DURATION_MINUTES] = minutes
        }
    }

    suspend fun setNatureSoundType(type: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NATURE_SOUND_TYPE] = type
        }
    }

    suspend fun setNatureSoundVolume(volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NATURE_SOUND_VOLUME] = volume
        }
    }

    suspend fun setBackgroundAudioEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BACKGROUND_AUDIO_ENABLED] = enabled
        }
    }
}
