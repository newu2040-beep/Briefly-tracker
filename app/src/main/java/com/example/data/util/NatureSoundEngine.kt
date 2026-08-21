package com.example.data.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

enum class NatureSound(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String
) {
    RAIN("RAIN", "Gentle Rain", "Soothing raindrops & soft thunder rumble", "🌧️"),
    OCEAN("OCEAN", "Ocean Waves", "Rhythmic ocean surf & rolling tides", "🌊"),
    FOREST("FOREST", "Forest & Birds", "Calm woodland breeze & melodic birds", "🌲"),
    STREAM("STREAM", "Mountain Stream", "Flowing crystal water & bubbling ripples", "💧"),
    SINGING_BOWL("SINGING_BOWL", "Tibetan Singing Bowl", "Deep 432Hz meditative healing drone", "🧘"),
    CAMPFIRE("CAMPFIRE", "Campfire Warmth", "Cozy fireplace glow & crackling embers", "🔥"),
    BROWN_NOISE("BROWN_NOISE", "Deep Brown Noise", "Warm low-frequency grounding focus sound", "🌌"),
    NIGHT("NIGHT", "Peaceful Night", "Midnight crickets & gentle night breeze", "🌙");

    companion object {
        fun fromId(id: String): NatureSound {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: RAIN
        }
    }
}

enum class NotificationRingtone(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String
) {
    ZEN_BELL("ZEN_BELL", "Zen Bell Chime", "Clear resonant meditation bell", "notifications"),
    CRYSTAL_CHIME("CRYSTAL_CHIME", "Crystal Chime", "Sparkling high-tone harmonic flourish", "auto_awesome"),
    FOREST_BIRDS("FOREST_BIRDS", "Morning Songbird", "Melodic morning bird song sequence", "park"),
    SINGING_BOWL("SINGING_BOWL", "Tibetan Bowl Gong", "Warm deep resonance bell strike", "self_improvement"),
    MORNING_HARP("MORNING_HARP", "Celestial Harp", "Gentle rising pentatonic harp arpeggio", "music_note"),
    DEFAULT("DEFAULT", "System Standard", "Clean modern alert tone", "volume_up");

    companion object {
        fun fromId(id: String): NotificationRingtone {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: ZEN_BELL
        }
    }
}

object NatureSoundEngine {
    private const val SAMPLE_RATE = 44100
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile
    var isPlaying: Boolean = false
        private set

    @Volatile
    var currentSound: NatureSound = NatureSound.RAIN
        private set

    @Volatile
    var currentVolume: Float = 0.75f
        private set

    fun playNatureSound(sound: NatureSound, volume: Float = currentVolume) {
        currentSound = sound
        currentVolume = volume.coerceIn(0f, 1f)
        stopNatureSound()

        isPlaying = true

        val bufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ) * 4

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        } catch (e: Exception) {
            isPlaying = false
            return
        }

        playbackJob = scope.launch {
            val random = Random()
            val chunk = ShortArray(2048)
            var phase = 0.0
            var wavePhase = 0.0
            var cricketPhase = 0.0
            var birdTimer = 0
            var birdFreq = 2200.0
            var lastNoise = 0.0

            while (isActive && isPlaying) {
                for (i in chunk.indices) {
                    val sample: Double = when (currentSound) {
                        NatureSound.RAIN -> {
                            // Pink noise filter + droplet clicks
                            val white = (random.nextDouble() * 2.0 - 1.0)
                            lastNoise = (lastNoise * 0.92) + (white * 0.08)
                            var rainSample = lastNoise * 0.7
                            if (random.nextInt(1200) == 0) {
                                rainSample += (random.nextDouble() - 0.5) * 0.9
                            }
                            rainSample
                        }
                        NatureSound.OCEAN -> {
                            // Modulated surf noise
                            wavePhase += (2.0 * PI * 0.12) / SAMPLE_RATE
                            if (wavePhase > 2.0 * PI) wavePhase -= 2.0 * PI
                            val waveEnvelope = (sin(wavePhase) + 1.0) * 0.45 + 0.1
                            val white = (random.nextDouble() * 2.0 - 1.0)
                            lastNoise = (lastNoise * 0.88) + (white * 0.12)
                            lastNoise * waveEnvelope
                        }
                        NatureSound.FOREST -> {
                            // Gentle wind with occasional bird chirps
                            val white = (random.nextDouble() * 2.0 - 1.0)
                            lastNoise = (lastNoise * 0.95) + (white * 0.05)
                            var s = lastNoise * 0.35

                            birdTimer++
                            if (birdTimer > SAMPLE_RATE * 3 && random.nextInt(1000) == 0) {
                                birdTimer = 0
                                birdFreq = 1800.0 + random.nextInt(800)
                            }
                            if (birdTimer < SAMPLE_RATE / 4) {
                                phase += (2.0 * PI * birdFreq) / SAMPLE_RATE
                                val birdEnvelope = sin((birdTimer.toDouble() / (SAMPLE_RATE / 4)) * PI)
                                s += sin(phase) * 0.4 * birdEnvelope
                            }
                            s
                        }
                        NatureSound.STREAM -> {
                            // Water bubbles and stream murmurs
                            phase += (2.0 * PI * 440.0) / SAMPLE_RATE
                            val white = (random.nextDouble() * 2.0 - 1.0)
                            lastNoise = (lastNoise * 0.85) + (white * 0.15)
                            var ripple = lastNoise * 0.6
                            if (random.nextInt(400) == 0) {
                                ripple += sin(phase * 2.5) * 0.3
                            }
                            ripple
                        }
                        NatureSound.SINGING_BOWL -> {
                            // 432Hz fundamental + harmonic warmth + tremolo
                            phase += (2.0 * PI * 432.0) / SAMPLE_RATE
                            wavePhase += (2.0 * PI * 0.8) / SAMPLE_RATE
                            val tremolo = 0.8 + 0.2 * sin(wavePhase)
                            val fund = sin(phase) * 0.6
                            val harm2 = sin(phase * 2.0) * 0.25
                            val harm3 = sin(phase * 3.0) * 0.15
                            (fund + harm2 + harm3) * tremolo
                        }
                        NatureSound.CAMPFIRE -> {
                            // Low warmth + crackle pops
                            val white = (random.nextDouble() * 2.0 - 1.0)
                            lastNoise = (lastNoise * 0.94) + (white * 0.06)
                            var fire = lastNoise * 0.4
                            if (random.nextInt(600) == 0) {
                                fire += (random.nextDouble() * 2.0 - 1.0) * 0.8
                            }
                            fire
                        }
                        NatureSound.BROWN_NOISE -> {
                            // Integrated noise (Brownian noise)
                            val white = (random.nextDouble() * 2.0 - 1.0)
                            lastNoise = (lastNoise * 0.985) + (white * 0.015)
                            lastNoise * 1.8
                        }
                        NatureSound.NIGHT -> {
                            // Crickets trills + soft midnight air
                            cricketPhase += (2.0 * PI * 4600.0) / SAMPLE_RATE
                            wavePhase += (2.0 * PI * 18.0) / SAMPLE_RATE
                            val pulse = if (sin(wavePhase) > 0.3) 1.0 else 0.05
                            val cricket = sin(cricketPhase) * 0.25 * pulse
                            val white = (random.nextDouble() * 2.0 - 1.0)
                            lastNoise = (lastNoise * 0.96) + (white * 0.04)
                            lastNoise * 0.2 + cricket
                        }
                    }

                    val clamped = (sample * currentVolume).coerceIn(-1.0, 1.0)
                    chunk[i] = (clamped * 32767.0).toInt().toShort()
                }

                try {
                    audioTrack?.write(chunk, 0, chunk.size)
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        try {
            audioTrack?.setVolume(currentVolume)
        } catch (_: Exception) {}
    }

    fun pauseNatureSound() {
        isPlaying = false
        playbackJob?.cancel()
        try {
            audioTrack?.pause()
        } catch (_: Exception) {}
    }

    fun resumeNatureSound() {
        if (!isPlaying) {
            playNatureSound(currentSound, currentVolume)
        }
    }

    fun stopNatureSound() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    // Play a preview of the custom notification ringtone/chime
    fun playRingtonePreview(ringtone: NotificationRingtone) {
        scope.launch {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(SAMPLE_RATE * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()

                val durationSeconds = when (ringtone) {
                    NotificationRingtone.CRYSTAL_CHIME, NotificationRingtone.MORNING_HARP -> 1.5
                    NotificationRingtone.FOREST_BIRDS -> 1.8
                    NotificationRingtone.SINGING_BOWL -> 2.2
                    else -> 1.2
                }

                val totalSamples = (SAMPLE_RATE * durationSeconds).toInt()
                val chunk = ShortArray(2048)
                var generated = 0

                val notes = when (ringtone) {
                    NotificationRingtone.ZEN_BELL -> doubleArrayOf(587.33, 1174.66)
                    NotificationRingtone.CRYSTAL_CHIME -> doubleArrayOf(1046.50, 1318.51, 1567.98, 1975.53, 2093.00)
                    NotificationRingtone.FOREST_BIRDS -> doubleArrayOf(2200.0, 2600.0, 2400.0, 2800.0)
                    NotificationRingtone.SINGING_BOWL -> doubleArrayOf(216.0, 432.0, 648.0)
                    NotificationRingtone.MORNING_HARP -> doubleArrayOf(659.25, 830.61, 987.77, 1318.51)
                    NotificationRingtone.DEFAULT -> doubleArrayOf(880.0, 1174.66)
                }

                var phase = 0.0
                while (generated < totalSamples) {
                    val samplesToGen = minOf(chunk.size, totalSamples - generated)
                    for (i in 0 until samplesToGen) {
                        val t = (generated + i).toDouble() / SAMPLE_RATE
                        val decay = kotlin.math.exp(-t * (if (ringtone == NotificationRingtone.SINGING_BOWL) 1.5 else 3.5))

                        var sample = 0.0
                        if (ringtone == NotificationRingtone.CRYSTAL_CHIME || ringtone == NotificationRingtone.MORNING_HARP) {
                            val noteIdx = (t * 6.0).toInt().coerceIn(0, notes.size - 1)
                            val noteFreq = notes[noteIdx]
                            phase += (2.0 * PI * noteFreq) / SAMPLE_RATE
                            sample = sin(phase) * decay
                        } else {
                            for (freq in notes) {
                                sample += sin(2.0 * PI * freq * t) * (0.8 / notes.size)
                            }
                            sample *= decay
                        }

                        chunk[i] = (sample.coerceIn(-1.0, 1.0) * 32767.0 * 0.85).toInt().toShort()
                    }
                    track.write(chunk, 0, samplesToGen)
                    generated += samplesToGen
                }

                delay((durationSeconds * 1000).toLong() + 200)
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }
}
