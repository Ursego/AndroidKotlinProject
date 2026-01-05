package ca.intfast.iftimer.appwide

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import ca.intfast.iftimer.util.CustomAppCompatActivity

fun beep(durationMs: Int) {
    val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, durationMs)
}

fun vibrate(context: Context, durationMs: Int) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    vibrator.vibrate(VibrationEffect.createOneShot(durationMs.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
}

fun beepAndVibrate(context: Context) {
    if (CustomAppCompatActivity.getBoolean(PrefKey.BEEP_ON_ALARM, context)) beep(1000)
    if (CustomAppCompatActivity.getBoolean(PrefKey.VIBRATE_ON_ALARM, context)) vibrate(context, 1000)
}