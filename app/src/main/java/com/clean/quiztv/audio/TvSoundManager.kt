package com.clean.quiztv.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.math.PI
import kotlin.math.sin

/**
 * Sound manager for Android TV:
 * - Ultra-low-latency PCM synthesized arcade effects (clicks, timer ticks, buzzers, fanfare)
 * - Real audio playback (MP3 / M4A / AAC) for melody rounds via MediaPlayer with fade-in/out and replay.
 */
object TvSoundManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var appContext: Context? = null

    enum class SoundEffect {
        TICK,
        URGENT_TICK,
        CORRECT,
        WRONG,
        ROUND_START,
        FANFARE,
        FOCUS_MOVE
    }

    private const val SAMPLE_RATE = 22050
    private val trackPool = mutableMapOf<SoundEffect, AudioTrack>()

    private var mediaPlayer: MediaPlayer? = null
    private var fadeJob: Job? = null
    private var currentTunePath: String? = null
    private val mediaLock = Any()

    private var isInitialized = false

    fun init(context: Context) {
        appContext = context.applicationContext
        if (!isInitialized) {
            isInitialized = true
            scope.launch {
                generateAllSounds()
            }
        }
    }

    private fun generateAllSounds() {
        val sounds = mapOf(
            SoundEffect.TICK to generateTone(freq = 950.0, durationMs = 28, decay = true),
            SoundEffect.URGENT_TICK to generateDualTone(freq1 = 1350.0, freq2 = 1800.0, durationMs = 50),
            SoundEffect.CORRECT to generateArpeggio(listOf(523.25, 659.25, 783.99, 1046.50), noteMs = 70),
            SoundEffect.WRONG to generateBuzzer(freq = 150.0, durationMs = 280),
            SoundEffect.ROUND_START to generateSweep(startFreq = 300.0, endFreq = 1200.0, durationMs = 200),
            SoundEffect.FANFARE to generateFanfare(),
            SoundEffect.FOCUS_MOVE to generateTone(freq = 600.0, durationMs = 18, decay = true)
        )

        for ((effect, buffer) in sounds) {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
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
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                trackPool[effect] = track
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun play(effect: SoundEffect) {
        val track = trackPool[effect] ?: return
        try {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.stop()
            }
            track.reloadStaticData()
            track.play()
        } catch (_: Exception) {}
    }

    /**
     * Plays a real audio preview file from assets/audio/ (or direct asset path) with smooth fade-in.
     */
    fun playMelody(tunePath: String) {
        val ctx = appContext ?: return
        currentTunePath = tunePath

        synchronized(mediaLock) {
            stopMelodyInternal(releaseOnly = false)

            val afd: AssetFileDescriptor = resolveAssetFile(ctx, tunePath) ?: return

            try {
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    setVolume(0.0f, 0.0f)
                    isLooping = false
                    setOnCompletionListener { mp ->
                        synchronized(mediaLock) {
                            try {
                                mp.reset()
                                mp.release()
                            } catch (_: Exception) {}
                            if (mediaPlayer == mp) mediaPlayer = null
                        }
                    }
                    prepare()
                    start()
                }
                mediaPlayer = player

                // Smooth fade-in from 0.0 to 1.0 over ~500ms
                fadeJob?.cancel()
                fadeJob = scope.launch {
                    val steps = 10
                    for (i in 1..steps) {
                        delay(45L)
                        val vol = i / steps.toFloat()
                        synchronized(mediaLock) {
                            try {
                                mediaPlayer?.setVolume(vol, vol)
                            } catch (_: Exception) {}
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                try { afd.close() } catch (_: Exception) {}
            }
        }
    }

    fun replayMelody() {
        val tune = currentTunePath ?: return
        playMelody(tune)
    }

    fun stopMelody() {
        fadeJob?.cancel()
        fadeJob = scope.launch {
            // Fade-out over ~250ms
            val steps = 5
            for (i in (steps - 1) downTo 0) {
                delay(40L)
                val vol = i / steps.toFloat()
                synchronized(mediaLock) {
                    try {
                        mediaPlayer?.setVolume(vol, vol)
                    } catch (_: Exception) {}
                }
            }
            synchronized(mediaLock) {
                stopMelodyInternal(releaseOnly = true)
            }
        }
    }

    private fun stopMelodyInternal(releaseOnly: Boolean) {
        fadeJob?.cancel()
        fadeJob = null
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.reset()
                player.release()
            }
        } catch (_: Exception) {} finally {
            mediaPlayer = null
            if (releaseOnly) {
                // Keep currentTunePath for potential replay
            }
        }
    }

    private fun resolveAssetFile(ctx: Context, path: String): AssetFileDescriptor? {
        val candidates = listOf(
            path,
            "audio/$path",
            "audio/$path.m4a",
            "audio/$path.mp3",
            "audio/${path.lowercase()}.m4a",
            "audio/${path.lowercase()}.mp3"
        )
        for (c in candidates) {
            try {
                return ctx.assets.openFd(c)
            } catch (_: IOException) {
                // Try next
            }
        }
        return null
    }

    fun release() {
        synchronized(mediaLock) {
            stopMelodyInternal(releaseOnly = true)
        }
        for (track in trackPool.values) {
            try {
                track.release()
            } catch (_: Exception) {}
        }
        trackPool.clear()
    }

    // --- Arcade Sound FX Synthesizers ---
    private fun generateTone(freq: Double, durationMs: Int, decay: Boolean = false): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = if (decay) (1.0 - (i.toDouble() / numSamples)) else 1.0
            val sample = (sin(2.0 * PI * freq * t) * env * 24000).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    private fun generateDualTone(freq1: Double, freq2: Double, durationMs: Int): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = (1.0 - (i.toDouble() / numSamples))
            val s1 = sin(2.0 * PI * freq1 * t)
            val s2 = sin(2.0 * PI * freq2 * t)
            val sample = ((s1 + s2) * 0.5 * env * 26000).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    private fun generateBuzzer(freq: Double, durationMs: Int): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val sinVal = sin(2.0 * PI * freq * t)
            val square = if (sinVal > 0) 1.0 else -1.0
            val env = (1.0 - (i.toDouble() / numSamples))
            val sample = (square * env * 20000).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    private fun generateSweep(startFreq: Double, endFreq: Double, durationMs: Int): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = startFreq + (endFreq - startFreq) * progress
            val t = i.toDouble() / SAMPLE_RATE
            val env = sin(PI * progress)
            val sample = (sin(2.0 * PI * freq * t) * env * 22000).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    private fun generateArpeggio(freqs: List<Double>, noteMs: Int): ShortArray {
        val samplesPerNote = (SAMPLE_RATE * (noteMs / 1000.0)).toInt()
        val totalSamples = samplesPerNote * freqs.size
        val buffer = ShortArray(totalSamples)

        freqs.forEachIndexed { noteIdx, freq ->
            for (i in 0 until samplesPerNote) {
                val t = i.toDouble() / SAMPLE_RATE
                val env = 1.0 - (i.toDouble() / samplesPerNote * 0.4)
                val sample = (sin(2.0 * PI * freq * t) * env * 24000).toInt().coerceIn(-32767, 32767)
                buffer[noteIdx * samplesPerNote + i] = sample.toShort()
            }
        }
        return buffer
    }

    private fun generateFanfare(): ShortArray {
        val freqs = listOf(261.63, 392.00, 523.25, 659.25, 783.99)
        val noteMs = listOf(100, 100, 120, 150, 450)
        val totalSamples = noteMs.sumOf { (SAMPLE_RATE * (it / 1000.0)).toInt() }
        val buffer = ShortArray(totalSamples)

        var offset = 0
        freqs.forEachIndexed { idx, freq ->
            val count = (SAMPLE_RATE * (noteMs[idx] / 1000.0)).toInt()
            for (i in 0 until count) {
                val t = i.toDouble() / SAMPLE_RATE
                val env = 1.0 - (i.toDouble() / count * 0.5)
                val s1 = sin(2.0 * PI * freq * t)
                val s2 = sin(2.0 * PI * (freq * 2) * t) * 0.3
                val sample = ((s1 + s2) * env * 22000).toInt().coerceIn(-32767, 32767)
                buffer[offset + i] = sample.toShort()
            }
            offset += count
        }
        return buffer
    }
}
