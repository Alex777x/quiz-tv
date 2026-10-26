package com.clean.quiztv.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * High-performance, zero-asset sound and melody synthesizer for Android TV.
 * Generates arcade-style audio effects and famous recognizable tunes in PCM memory.
 */
object TvSoundManager {
    private val scope = CoroutineScope(Dispatchers.Default)

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
    private var currentMelodyTrack: AudioTrack? = null

    init {
        generateAllSounds()
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

    fun playMelody(tuneId: String) {
        stopMelody()
        scope.launch {
            val buffer = generateMelodyBuffer(tuneId) ?: return@launch
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
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
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                currentMelodyTrack = track
                track.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopMelody() {
        try {
            currentMelodyTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) {} finally {
            currentMelodyTrack = null
        }
    }

    fun release() {
        stopMelody()
        for (track in trackPool.values) {
            try {
                track.release()
            } catch (_: Exception) {}
        }
        trackPool.clear()
    }

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

    // --- Iconic Melody Note Synthesizer ---
    private fun generateMelodyBuffer(tuneId: String): ShortArray? {
        val E3 = 164.81; val G3 = 196.00; val A3 = 220.00; val B3 = 246.94
        val C4 = 261.63; val Cs4 = 277.18; val D4 = 293.66; val Ds4 = 311.13; val E4 = 329.63
        val F4 = 349.23; val Fs4 = 369.99; val G4 = 392.00; val A4 = 440.00; val Bb4 = 466.16; val B4 = 493.88
        val C5 = 523.25; val D5 = 587.33; val E5 = 659.25; val Fs5 = 739.99
        val G5 = 783.99; val A5 = 880.00; val B5 = 987.77
        val REST = 0.0

        val notes: List<Pair<Double, Int>> = when (tuneId.lowercase()) {
            "star_wars" -> listOf(
                G4 to 450, G4 to 450, G4 to 450,
                Ds4 to 320, Bb4 to 140, G4 to 450,
                Ds4 to 320, Bb4 to 140, G4 to 800
            )
            "mario" -> listOf(
                E5 to 130, E5 to 130, REST to 100,
                E5 to 130, REST to 100, C5 to 130,
                E5 to 220, G5 to 350, REST to 200, G4 to 350
            )
            "pink_panther" -> listOf(
                Ds4 to 160, E4 to 280, Fs4 to 160, G4 to 280,
                Ds4 to 160, E4 to 160, Fs4 to 160, G4 to 160,
                C5 to 260, B4 to 260, E4 to 160, G4 to 160, B4 to 260, Bb4 to 450
            )
            "beethoven" -> listOf(
                G4 to 190, G4 to 190, G4 to 190, Ds4 to 750,
                REST to 250,
                F4 to 190, F4 to 190, F4 to 190, D4 to 850
            )
            "queen" -> listOf(
                E3 to 180, E3 to 180, E3 to 180, REST to 120,
                E3 to 180, E3 to 180, G3 to 220, E3 to 180, A3 to 280, G3 to 400
            )
            "tetris" -> listOf(
                E5 to 300, B4 to 150, C5 to 150, D5 to 300, C5 to 150, B4 to 150,
                A4 to 300, A4 to 150, C5 to 150, E5 to 300, D5 to 150, C5 to 150,
                B4 to 450
            )
            "got" -> listOf(
                G4 to 350, C4 to 350, Ds4 to 180, F4 to 180,
                G4 to 350, C4 to 350, Ds4 to 180, F4 to 180, D4 to 600
            )
            "james_bond" -> listOf(
                B3 to 240, C4 to 320, Cs4 to 240, C4 to 320,
                B3 to 240, C4 to 320, Cs4 to 240, C4 to 320
            )
            "mission_impossible" -> listOf(
                G4 to 320, G4 to 320, Bb4 to 220, C5 to 220,
                G4 to 320, G4 to 320, F4 to 220, Fs4 to 220,
                G4 to 320, G4 to 320, Bb4 to 220, C5 to 220
            )
            "harry_potter" -> listOf(
                B4 to 280, E5 to 400, G5 to 220, Fs5 to 220, E5 to 420,
                B5 to 280, A5 to 600, Fs5 to 600
            )
            "godfather" -> listOf(
                E4 to 240, A4 to 240, C5 to 240, B4 to 240, A4 to 240,
                C5 to 240, A4 to 240, B4 to 240, A4 to 240, F4 to 240, G4 to 240, E4 to 500
            )
            "indiana_jones" -> listOf(
                E4 to 180, F4 to 180, G4 to 360, C5 to 650,
                D4 to 180, E4 to 180, F4 to 700
            )
            "titanic" -> listOf(
                F4 to 300, G4 to 300, A4 to 550, G4 to 300, F4 to 300,
                G4 to 300, C5 to 700, Bb4 to 400, A4 to 400, F4 to 700
            )
            "rickroll" -> listOf(
                F4 to 160, G4 to 160, Bb4 to 160, G4 to 160, D5 to 350, D5 to 350, C5 to 550,
                F4 to 160, G4 to 160, Bb4 to 160, G4 to 160, C5 to 350, C5 to 350, Bb4 to 350, A4 to 250, G4 to 350
            )
            else -> return null
        }

        val totalSamples = notes.sumOf { (SAMPLE_RATE * (it.second / 1000.0)).toInt() }
        val buffer = ShortArray(totalSamples)

        var offset = 0
        for ((freq, durationMs) in notes) {
            val count = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
            if (freq <= 0.0) {
                // Rest silence
                offset += count
                continue
            }
            for (i in 0 until count) {
                val t = i.toDouble() / SAMPLE_RATE
                val progress = i.toDouble() / count
                // Smooth bell envelope for pleasant acoustic timbre
                val env = (1.0 - progress * 0.4) * (1.0 - Math.exp(-progress * 25.0))
                val fundamental = sin(2.0 * PI * freq * t)
                val overtone = sin(2.0 * PI * (freq * 2.0) * t) * 0.35
                val sample = ((fundamental + overtone) * env * 22000).toInt().coerceIn(-32767, 32767)
                buffer[offset + i] = sample.toShort()
            }
            offset += count
        }
        return buffer
    }
}
