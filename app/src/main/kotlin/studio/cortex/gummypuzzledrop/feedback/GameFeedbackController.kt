package studio.cortex.gummypuzzledrop.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.PI
import kotlin.math.sin
import studio.cortex.gummypuzzledrop.data.PlayerProgress

class GameFeedbackController(context: Context) {
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java).defaultVibrator
    } else {
        legacyVibrator(context)
    }
    private val tones = ToneGenerator(AudioManager.STREAM_MUSIC, 52)
    private var settings = PlayerProgress()
    private var isForeground = false
    private var ambience: AudioTrack? = null

    fun updateSettings(value: PlayerProgress) {
        val musicChanged = settings.musicEnabled != value.musicEnabled
        settings = value
        if (musicChanged || ambience == null) updateAmbience()
    }

    fun setForeground(value: Boolean) {
        isForeground = value
        updateAmbience()
    }

    fun handle(event: FeedbackEvent) {
        if (!isForeground) return
        if (settings.soundEnabled) {
            val (tone, duration) = when (event.cue) {
                FeedbackCue.MOVE -> ToneGenerator.TONE_PROP_BEEP2 to 24
                FeedbackCue.ROTATE -> ToneGenerator.TONE_PROP_BEEP to 48
                FeedbackCue.HARD_DROP -> ToneGenerator.TONE_PROP_ACK to 85
                FeedbackCue.LAND -> ToneGenerator.TONE_PROP_ACK to 58
                FeedbackCue.POP -> ToneGenerator.TONE_PROP_PROMPT to 92
                FeedbackCue.COMBO -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD to 145
                FeedbackCue.BOMB -> ToneGenerator.TONE_CDMA_HIGH_L to 210
                FeedbackCue.BUTTON -> ToneGenerator.TONE_PROP_BEEP to 48
                FeedbackCue.GAME_OVER -> ToneGenerator.TONE_SUP_ERROR to 230
            }
            tones.startTone(tone, duration)
        }
        if (event.haptic != HapticStrength.NONE && settings.vibrationEnabled && vibrator.hasVibrator()) {
            val (duration, amplitude) = when (event.haptic) {
                HapticStrength.NONE -> 0L to 0
                HapticStrength.LIGHT -> 18L to 48
                HapticStrength.MEDIUM -> 32L to 105
                HapticStrength.STRONG -> 56L to 185
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        }
    }

    fun release() {
        ambience?.release()
        ambience = null
        tones.release()
    }

    private fun updateAmbience() {
        if (!isForeground || !settings.musicEnabled) {
            ambience?.pause()
            return
        }
        if (ambience == null) ambience = buildAmbience()
        runCatching { ambience?.play() }
    }

    private fun buildAmbience(): AudioTrack? = runCatching {
        val sampleRate = 11_025
        val seconds = 4
        val sampleCount = sampleRate * seconds
        val notes = doubleArrayOf(261.63, 329.63, 392.0, 523.25)
        val samples = ShortArray(sampleCount) { index ->
            val time = index.toDouble() / sampleRate
            val phrase = time.toInt().coerceIn(0, notes.lastIndex)
            val root = notes[phrase]
            val envelope = 0.62 + 0.38 * sin(PI * (time % 1.0))
            val shimmer = sin(2.0 * PI * root * time) * 0.52 +
                sin(2.0 * PI * root * 1.5 * time) * 0.22 +
                sin(2.0 * PI * root * 2.0 * time) * 0.12
            (shimmer * envelope * 720.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(sampleCount * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
            .also { track ->
                track.write(samples, 0, samples.size)
                track.setLoopPoints(0, samples.size, -1)
                track.setVolume(0.07f)
            }
    }.getOrNull()
}

@Suppress("DEPRECATION")
private fun legacyVibrator(context: Context): Vibrator =
    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
