package ca.intfast.iftimer.util

import android.content.Context
import androidx.preference.PreferenceManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object AppPrefs {
    fun getString(key: String, context: Context, defValue: String = ""): String {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        return appSharedPreferences.getString(key, defValue)!!
    }

    fun getBoolean(key: String, context: Context, defValue: Boolean = false): Boolean {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        return try {
            appSharedPreferences.getBoolean(key, defValue)
        } catch (e: ClassCastException) {
            // There is a preference with this name that is not a boolean
            val prefAsString = this.getString(key, context)
            if (prefAsString == "") return defValue
            prefAsString.toBoolean()
        }
    }

    fun getInt(key: String, context: Context, defValue: Int = 0): Int {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        return try {
            appSharedPreferences.getInt(key, defValue)
        } catch (e: ClassCastException) {
            val prefAsString = this.getString(key, context)
            if (prefAsString == "") return defValue
            prefAsString.toInt()
        }
    }

    fun getLong(key: String, context: Context, defValue: Long = 0): Long {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        return try {
            appSharedPreferences.getLong(key, defValue)
        } catch (e: ClassCastException) {
            val prefAsString = this.getString(key, context)
            if (prefAsString == "") return defValue
            prefAsString.toLong()
        }
    }

    fun getFloat(key: String, context: Context, defValue: Float = 0.0F): Float {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        return try {
            appSharedPreferences.getFloat(key, defValue)
        } catch (e: ClassCastException) {
            val prefAsString = this.getString(key, context)
            if (prefAsString == "") return defValue
            prefAsString.toFloat()
        }
    }

    fun getLocalDate(key: String, context: Context, defValue: LocalDate? = null): LocalDate? {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        val stringDefValue = if (defValue == null) "" else Chronos.toString(defValue)
        val prefAsString = appSharedPreferences.getString(key, stringDefValue)!!
        if (prefAsString == "") return defValue
        return Chronos.toLocalDate(prefAsString)!!
    }

    fun getLocalTime(key: String, context: Context, defValue: LocalTime? = null): LocalTime? {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        val stringDefValue = if (defValue == null) "" else Chronos.toString(defValue)
        val prefAsString = appSharedPreferences.getString(key, stringDefValue)!!
        if (prefAsString == "") return defValue
        return Chronos.toLocalTime(prefAsString)!!
    }

    fun getLocalDateTime(key: String, context: Context, defValue: LocalDateTime? = null): LocalDateTime? {
        val appSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!
        val stringDefValue = if (defValue == null) "" else Chronos.toString(defValue)
        val prefAsString = appSharedPreferences.getString(key, stringDefValue)!!
        if (prefAsString == "") return defValue
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