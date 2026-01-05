package ca.intfast.iftimer.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/****************************************************************************************************************************
Object which facilitates conversion of LocalDate, LocalTime & LocalDateTime to String and back.
Encapsulates the fuss with date/time formats and parsing.
Useful when a time/date value, which should be manipulated in code in its original type,
is stored somewhere as String (for example, in Preferences or SQLite DB).
Chronos is the personification of time in pre-Socratic philosophy and later literature (https://en.wikipedia.org/wiki/Chronos)
https://tinyurl.com/ChronosObj
****************************************************************************************************************************/

object Chronos {
    val defaultDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val defaultTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
    val defaultDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // Convert TO String:

    fun toString(dVal: LocalDate?, pattern: String? = null): String? {
        if (dVal == null) return null
        return getDateFormatter(pattern).format(dVal)
    }

    fun toString(tVal: LocalTime?, pattern: String? = null): String? {
        if (tVal == null) return null
        return getTimeFormatter(pattern).format(tVal)
    }

    fun toString(dtVal: LocalDateTime?, pattern: String? = null): String? {
        if (dtVal == null) return null
        return getDateTimeFormatter(pattern).format(dtVal)
    }

    // Convert FROM String:

    fun toLocalDate(sVal: String?, pattern: String? = null): LocalDate? {
        if (sVal.isNullOrBlank()) return null
        return LocalDate.parse(sVal, getDateFormatter(pattern))
    }

    fun toLocalTime(sVal: String?, pattern: String? = null): LocalTime? {
        if (sVal.isNullOrBlank()) return null
        return LocalTime.parse(sVal, getTimeFormatter(pattern))
    }

    fun toLocalDateTime(sVal: String?, pattern: String? = null): LocalDateTime? {
        if (sVal.isNullOrBlank()) return null
        return LocalDateTime.parse(sVal, getDateTimeFormatter(pattern))
    }

    // Service functions:

    private fun getFormatter(pattern: String?, defaultFormatter: DateTimeFormatter): DateTimeFormatter =
        if (pattern.isNullOrBlank()) defaultFormatter else DateTimeFormatter.ofPattern(pattern)
        //pattern?.takeIf { it.isNotBlank() }?.let(DateTimeFormatter::ofPattern) ?: defaultFormatter

    private fun getDateFormatter(pattern: String? = null): DateTimeFormatter =
        getFormatter(pattern, defaultDateFormatter)

    private fun getTimeFormatter(pattern: String? = null): DateTimeFormatter =
        getFormatter(pattern, defaultTimeFormatter)

    private fun getDateTimeFormatter(pattern: String? = null): DateTimeFormatter =
        getFormatter(pattern, defaultDateTimeFormatter)
}