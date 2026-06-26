package com.tasbeeh.app.presentation.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.sin

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun playClick() {
        Thread {
            val sampleRate = 44100
            val durationMs = 90
            val samples = sampleRate * durationMs / 1000
            val buffer = ShortArray(samples)

            for (i in 0 until samples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / samples
                // Quick attack, slow decay envelope for a "ding" feel
                val envelope = if (progress < 0.05) progress / 0.05 else 1.0 - (progress - 0.05) / 0.95
                // 880 Hz with slight harmonic at 1760 Hz
                val wave = sin(2 * PI * 880 * t) * 0.7 + sin(2 * PI * 1760 * t) * 0.3
                buffer[i] = (Short.MAX_VALUE * 0.55 * wave * envelope).toInt().toShort()
            }

            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufSize = maxOf(samples * 2, minBufSize)

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, samples)
            audioTrack.setVolume(0.75f)
            audioTrack.play()
            Thread.sleep((durationMs + 30).toLong())
            audioTrack.stop()
            audioTrack.release()
        }.start()
    }
}
