package com.tasbeeh.app.presentation.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.tasbeeh.app.domain.model.VibrationIntensity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VibrationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    /** Short click feedback with configurable intensity. */
    fun vibrate(intensity: VibrationIntensity = VibrationIntensity.MEDIUM) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitude = when (intensity) {
                VibrationIntensity.LOW    -> 80
                VibrationIntensity.MEDIUM -> VibrationEffect.DEFAULT_AMPLITUDE
                VibrationIntensity.HIGH   -> 255
            }
            vibrator.vibrate(VibrationEffect.createOneShot(30, amplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    }

    /** Double-pulse feedback when the goal is reached. */
    fun vibrateGoalReached() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings    = longArrayOf(0, 200, 100, 200)
            val amplitudes = intArrayOf(
                0,
                VibrationEffect.DEFAULT_AMPLITUDE,
                0,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 200, 100, 200), -1)
        }
    }
}
