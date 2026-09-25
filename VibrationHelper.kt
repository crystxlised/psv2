package com.example

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object VibrationHelper {
    fun vibrate(context: Context, mode: String) {
        if (mode.equals("none", ignoreCase = true)) return
        
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return

            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (mode.lowercase()) {
                    "light" -> VibrationEffect.createOneShot(12, 70)
                    "medium" -> VibrationEffect.createOneShot(20, 140)
                    "strong" -> VibrationEffect.createOneShot(40, 255)
                    else -> return
                }
                vibrator.vibrate(effect)
            } else {
                val duration = when (mode.lowercase()) {
                    "light" -> 12L
                    "medium" -> 20L
                    "strong" -> 40L
                    else -> return
                }
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
