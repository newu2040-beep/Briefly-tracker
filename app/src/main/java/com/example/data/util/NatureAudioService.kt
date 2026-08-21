package com.example.data.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class NatureAudioService : Service() {

    companion object {
        const val CHANNEL_NATURE_AUDIO = "briefly_nature_audio_channel"
        const val NOTIFICATION_ID_NATURE = 2001

        const val ACTION_START_AUDIO = "com.example.ACTION_START_AUDIO"
        const val ACTION_PAUSE_AUDIO = "com.example.ACTION_PAUSE_AUDIO"
        const val ACTION_RESUME_AUDIO = "com.example.ACTION_RESUME_AUDIO"
        const val ACTION_STOP_AUDIO = "com.example.ACTION_STOP_AUDIO"

        const val EXTRA_SOUND_ID = "extra_sound_id"
        const val EXTRA_VOLUME = "extra_volume"

        fun startAudio(context: Context, soundId: String, volume: Float) {
            val intent = Intent(context, NatureAudioService::class.java).apply {
                action = ACTION_START_AUDIO
                putExtra(EXTRA_SOUND_ID, soundId)
                putExtra(EXTRA_VOLUME, volume)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseAudio(context: Context) {
            val intent = Intent(context, NatureAudioService::class.java).apply {
                action = ACTION_PAUSE_AUDIO
            }
            context.startService(intent)
        }

        fun resumeAudio(context: Context) {
            val intent = Intent(context, NatureAudioService::class.java).apply {
                action = ACTION_RESUME_AUDIO
            }
            context.startService(intent)
        }

        fun stopAudio(context: Context) {
            val intent = Intent(context, NatureAudioService::class.java).apply {
                action = ACTION_STOP_AUDIO
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_AUDIO -> {
                val soundId = intent.getStringExtra(EXTRA_SOUND_ID) ?: NatureSound.RAIN.id
                val volume = intent.getFloatExtra(EXTRA_VOLUME, 0.75f)
                val sound = NatureSound.fromId(soundId)

                NatureSoundEngine.playNatureSound(sound, volume)
                startForeground(NOTIFICATION_ID_NATURE, buildNotification(sound, isPlaying = true))
            }
            ACTION_PAUSE_AUDIO -> {
                NatureSoundEngine.pauseNatureSound()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(
                    NOTIFICATION_ID_NATURE,
                    buildNotification(NatureSoundEngine.currentSound, isPlaying = false)
                )
            }
            ACTION_RESUME_AUDIO -> {
                NatureSoundEngine.resumeNatureSound()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(
                    NOTIFICATION_ID_NATURE,
                    buildNotification(NatureSoundEngine.currentSound, isPlaying = true)
                )
            }
            ACTION_STOP_AUDIO -> {
                NatureSoundEngine.stopNatureSound()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_NATURE_AUDIO,
                "Peaceful Nature Sound Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for background peaceful nature sounds"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(sound: NatureSound, isPlaying: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleAction = if (isPlaying) ACTION_PAUSE_AUDIO else ACTION_RESUME_AUDIO
        val toggleActionTitle = if (isPlaying) "Pause" else "Resume"
        val toggleIntent = Intent(this, NatureAudioService::class.java).apply {
            action = toggleAction
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, NatureAudioService::class.java).apply {
            action = ACTION_STOP_AUDIO
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_NATURE_AUDIO)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${sound.emoji} ${sound.title}")
            .setContentText(if (isPlaying) "Playing peaceful background sounds" else "Audio paused")
            .setContentIntent(openAppPendingIntent)
            .setOngoing(isPlaying)
            .addAction(android.R.drawable.ic_media_play, toggleActionTitle, togglePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    override fun onDestroy() {
        NatureSoundEngine.stopNatureSound()
        super.onDestroy()
    }
}
