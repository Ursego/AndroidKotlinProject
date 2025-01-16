package ca.intfast.iftimer.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.SystemClock
import android.widget.Chronometer
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// -----------------------------------------------------------------------------------------------------------------
// ------- Extend ContentValues: add putters for additional data types (http://code.intfast.ca/viewtopic.php?t=814):
// -----------------------------------------------------------------------------------------------------------------

fun ContentValues.put(key: String, value : LocalDate?) = this.put(key, Chronos.toString(value))
fun ContentValues.put(key: String, value : LocalTime?) = this.put(key, Chronos.toString(value))
fun ContentValues.put(key: String, value : LocalDateTime?) = this.put(key, Chronos.toString(value))

// -----------------------------------------------------------------------------------------------------------------
// ------- Extend Cursor: add getters for additional data types, and allow to get by column name
// ------- rather than column index (http://code.intfast.ca/viewtopic.php?t=814):
// -----------------------------------------------------------------------------------------------------------------

fun Cursor.getShort(columnName: String): Short? = this.getShort(this.getColumnIndex(columnName))
fun Cursor.getInt(columnName: String): Int? = this.getInt(this.getColumnIndex(columnName))
fun Cursor.getLong(columnName: String): Long? = this.getLong(this.getColumnIndex(columnName))
fun Cursor.getFloat(columnName: String): Float? = this.getFloat(this.getColumnIndex(columnName))
fun Cursor.getDouble(columnName: String): Double? = this.getDouble(this.getColumnIndex(columnName))
fun Cursor.getString(columnName: String): String? = this.getString(this.getColumnIndex(columnName))
fun Cursor.getBlob(columnName: String): ByteArray? = this.getBlob(this.getColumnIndex(columnName))

fun Cursor.getBoolean(columnName: String): Boolean {
    val i = this.getInt(this.getColumnIndex(columnName))
    when (i) {
        1 -> return true
        0 -> return false
    }
    throw Exception("Value of field $columnName is $i. To be treated as Boolean, it must be 0 or 1.")
}

fun Cursor.getLocalDate(columnName: String): LocalDate? {
    val s = this.getString(this.getColumnIndex(columnName)) ?: return null
    return Chronos.toLocalDate(s)
}

fun Cursor.getLocalTime(columnName: String): LocalTime? {
    val s = this.getString(this.getColumnIndex(columnName)) ?: return null
    return Chronos.toLocalTime(s)
}

fun Cursor.getLocalDateTime(columnName: String): LocalDateTime? {
    val s = this.getString(this.getColumnIndex(columnName)) ?: return null
    return Chronos.toLocalDateTime(s)
}

// -----------------------------------------------------------------------------------------------------------------
// ------- Extend Chronometer functions to allow persistence (http://code.intfast.ca/viewtopic.php?t=826):
// -----------------------------------------------------------------------------------------------------------------

// The key, by which the base time of the Chronometer (as a LocalDateTime) will be stored in SharedPreferences,
// to be then retrieved onResume() and used to calculate the new base to make the Chronometer keeping counting:
fun Chronometer.generateStartLdtPrefKey(instanceName: String?): String =
                                        "-+=|[Chronometer ${instanceName ?: ""} START LDT]|=+-" // LDT = LocalDateTime

//fun Chronometer.start(instanceName: String? = null /* omit if only one Chronometer in app */, context: Context) {
//    // Must be called instead of start(<no args>) when user clicks START button.
//    // Starts Chronometer and makes it counting from the curr. moment (now).
//    // To count form a provided moment, use another overload.
//
//    // Remember the curr. moment, so resume(), called from the Activity onResume(), will make the Chronometer counting from it:
//    val startLdtPrefKey = this.generateStartLdtPrefKey(instanceName ?: "")
//    AppPrefs.put(startLdtPrefKey, LocalDateTime.now()!!, context)
//
//    this.base = SystemClock.elapsedRealtime() // milliseconds since boot, including time spent in sleep
//    this.start()
//} // Chronometer.start(String, Context)

fun Chronometer.start(instanceName: String? = null /* omit if only one Chronometer in app */,
                      startLdt: LocalDateTime? /* null = "start from now" */,
                      context: Context) {
    // Starts Chronometer and makes it count from the provided moment (startLdt); if null, then from the curr. moment (now).
    // Call it (without startLdt) instead of start(<no args>) when user clicks Chronometer's START button.

    val startLdtPrefKey = generateStartLdtPrefKey(instanceName)

    if (startLdt == null) /* start from now */{
        // Remember this moment, so resume(), called from the Activity onResume(), will make the Chronometer counting from it:
        AppPrefs.put(startLdtPrefKey, LocalDateTime.now()!!, context)
        this.base = SystemClock.elapsedRealtime() // milliseconds since boot, including time spent in sleep
        this.start()
        return
    }

    // Count form the moment, provided in startLdt:

    // Remember startLdt, so resume(), called from the Activity onResume(), will make the Chronometer counting from it:
    AppPrefs.put(startLdtPrefKey, startLdt, context)

    val now = LocalDateTime.now()!!
    var deltaInMilli = Duration.between(startLdt, now)!!.toMillis()

    val secondsInMilli = 1000L
    val minutesInMilli = secondsInMilli * 60
    val hoursInMilli = minutesInMilli * 60
    val daysInMilli = hoursInMilli * 24

    deltaInMilli %= daysInMilli

    val elapsedHours = deltaInMilli / hoursInMilli
    deltaInMilli %= hoursInMilli

    val elapsedMinutes = deltaInMilli / minutesInMilli
    deltaInMilli %= minutesInMilli

    val elapsedSeconds = deltaInMilli / secondsInMilli

    val elapsedTimeMilliseconds =
        elapsedHours * 60 * 60 * 1000 + elapsedMinutes * 60 * 1000 + elapsedSeconds * 1000

    this.base = SystemClock.elapsedRealtime() - elapsedTimeMilliseconds
    this.start()
} // Chronometer.start(String, LocalDateTime, Context)

fun Chronometer.finish() {
    // Must be called instead of stop() when user clicks STOP button.
    this.stop()
    this.base = SystemClock.elapsedRealtime() // reset displayed time to "00:00"
} // Chronometer.finish()

fun Chronometer.resume(instanceName: String? = null /* omit if only one Chronometer in app */, context: Context) {
    // Must be called from onResume() of the Chronometer's parent Activity
    val startLdtPrefKey = generateStartLdtPrefKey(instanceName)
    val startLdt = AppPrefs.getLocalDateTime(startLdtPrefKey, context)
        ?:
        return // it's null because start(context: Context) has never been called

    start(instanceName, startLdt, context)
} // Chronometer.resume()