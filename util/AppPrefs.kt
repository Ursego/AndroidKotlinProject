/****************************************************************************************************************************
AppPrefs which facilitates storing values in SharedPreferences.
It abstracts away the complexity of obtaining SharedPreferences, handling potential type casting errors,
and converting complex types (like `LocalDate`, `LocalDateTime`, etc.) to and from Strings for storage.
For example, to read a LocalDateTime, simply call:
val updatedAt = AppPrefs.getLocalDateTime(DbColumn.UPDATED_AT, context)

AppPrefs simplifies creation of properties which use SharedPreferences instead of a backing instance variable, for example:

class MyExampleActivity : AppCompatActivity() {
    var isInvoicePrintable
        get() = AppPrefs.getBoolean("isInvoicePrintable", this)
        set(value) = AppPrefs.put("isInvoicePrintable", value, this)
    ...
}

https://tinyurl.com/SharedPreferences
****************************************************************************************************************************/
package ca.intfast.iftimer.util

import android.content.Context
import androidx.preference.PreferenceManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Object which facilitates storing and retrieving values (including non-String types) in SharedPreferences.
 *
 * `AppPrefs` abstracts away the complexity of obtaining `SharedPreferences`, handling potential type casting errors,
 * and converting complex types (like `LocalDate`, `LocalDateTime`, etc.) to and from Strings for storage.
 *
 * It is particularly useful for creating properties that use `SharedPreferences` as a backing field instead of
 * a local variable.
 *
 * **Usage Example:**
 * ```kotlin
 * class MyExampleActivity : AppCompatActivity() {
 *     private val uuid = UUID.randomUUID().toString()
 *
 *     // Property backed by SharedPreferences
 *     var isPrintable: Boolean
 *         get() = AppPrefs.getBoolean("${uuid}isPrintable", this)
 *         set(value) = AppPrefs.put("${uuid}isPrintable", value, this)
 *
 *     fun readDateExample() {
 *         // Reading a LocalDateTime directly
 *         val lastUpdated = AppPrefs.getLocalDateTime("updated_at_key", this)
 *     }
 * }
 *
 * @see [SharedPreferences Documentation](https://developer.android.com/reference/android/content/SharedPreferences)
 */
object AppPrefs {
    fun getString(key: String, context: Context): String {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        return appSharedPreferences.getString(key, "")!!
    }

    fun getBoolean(key: String, context: Context): Boolean {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        return try {
            appSharedPreferences.getBoolean(key, false)
        } catch (e: ClassCastException) {
            // There is a preference with this name that is not a boolean
            val prefAsString = this.getString(key, context)
            if (prefAsString == "") return false
            prefAsString.toBoolean()
        }
    }

    fun getInt(key: String, context: Context): Int {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        return try {
            appSharedPreferences.getInt(key, 0)
        } catch (e: ClassCastException) {
            val prefAsString = this.getString(key, context)
            if (prefAsString == "") return 0
            prefAsString.toInt()
        }
    }

    fun getLong(key: String, context: Context): Long {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        return try {
            appSharedPreferences.getLong(key, 0)
        } catch (e: ClassCastException) {
            val prefAsString = this.getString(key, context)
            if (prefAsString == "") return 0
            prefAsString.toLong()
        }
    }

    fun getFloat(key: String, context: Context): Float {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        return try {
            appSharedPreferences.getFloat(key, 0.0F)
        } catch (e: ClassCastException) {
            val prefAsString = this.getString(key, context)
            if (prefAsString == "") return 0.0F
            prefAsString.toFloat()
        }
    }

    fun getLocalDate(key: String, context: Context): LocalDate? {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        val prefAsString = appSharedPreferences.getString(key, "")!!
        if (prefAsString == "") return null
        return Chronos.toLocalDate(prefAsString)!!
    }

    fun getLocalTime(key: String, context: Context): LocalTime? {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        val prefAsString = appSharedPreferences.getString(key, "")!!
        if (prefAsString == "") return null
        return Chronos.toLocalTime(prefAsString)!!
    }

    fun getLocalDateTime(key: String, context: Context): LocalDateTime? {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        val prefAsString = appSharedPreferences.getString(key, "")!!
        if (prefAsString == "") return null
        return Chronos.toLocalDateTime(prefAsString)!!
    }

    fun getStringSet(key: String, context: Context): MutableSet<String> {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        return appSharedPreferences.getStringSet(key, null)!!
    }

    fun put(key: String, value: String, context: Context) {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        appSharedPreferences.edit().putString(key, value).apply()
    }

    fun put(key: String, value: Boolean, context: Context) {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        appSharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun put(key: String, value: Int, context: Context) {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        appSharedPreferences.edit().putInt(key, value).apply()
    }

    fun put(key: String, value: Long, context: Context) {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        appSharedPreferences.edit().putLong(key, value).apply()
    }

    fun put(key: String, value: Float, context: Context) {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        appSharedPreferences.edit().putFloat(key, value).apply()
    }

    fun put(key: String, value: MutableSet<String>, context: Context) {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        appSharedPreferences.edit().putStringSet(key, value).apply()
    }

    fun put(key: String, value: LocalDate, context: Context) {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        val prefAsString = Chronos.toString(value)
        appSharedPreferences.edit().putString(key, prefAsString).apply()
    }

    fun put(key: String, value: LocalTime, context: Context) {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        val prefAsString = Chronos.toString(value)
        appSharedPreferences.edit().putString(key, prefAsString).apply()
    }

    fun put(key: String, value: LocalDateTime, context: Context) {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        val prefAsString = Chronos.toString(value)
        appSharedPreferences.edit().putString(key, prefAsString).apply()
    }
}