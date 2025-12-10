package ca.intfast.iftimer.appwide

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import ca.intfast.iftimer.util.AppPrefs

fun beep(durationMs: Int) {
    val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, durationMs)
}

fun vibrate(context: Context, durationMs: Int) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= 26) {
        vibrator.vibrate(VibrationEffect.createOneShot(durationMs.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(durationMs.toLong())
    }
}

fun beepAndVibrate(context: Context) {
    if (AppPrefs.getBoolean(PrefKey.BEEP_ON_ALARM, context)) beep(1000)
    if (AppPrefs.getBoolean(PrefKey.VIBRATE_ON_ALARM, context)) vibrate(context, 1000)
}