package ca.intfast.iftimer.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/****************************************************************************************************************************
Object which facilitates conversion of LocalDate, LocalTime & LocalDateTime to String and back.
Encapsulates the fuss with date/time formats and parsing.
Useful when a time/date value, which should be manipulated in code in its original type,
is stored somewhere as String (for example, in Preferences or SQLite DB).
Chronos is the personification of time in pre-Socratic philosophy and later literature (https://en.wikipedia.org/wiki/Chronos)
http://code.intfast.ca/viewtopic.php?t=825
****************************************************************************************************************************/

object Chronos {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE//DateTimeFormatter.ofPattern("<your format>")
    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME //DateTimeFormatter.ofPattern("<your format>")
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME //DateTimeFormatter.ofPattern("<your format>")

    // LocalDate:

    fun toLocalDate (valAsString: String?): LocalDate? {
        if (valAsString == null) return null
        try {
            return LocalDate.parse(valAsString, dateFormatter)
        } catch (e: DateTimeParseException) {
            throw Exception("Cannot parse '$valAsString' to LocalDate.")
        }
    }

    fun toString (valAsLocalDate: LocalDate?): String? {
        if (valAsLocalDate == null) return null
        return dateFormatter.format(valAsLocalDate)
    }

    // LocalTime:

    fun toLocalTime (valAsString: String?): LocalTime? {
        if (valAsString == null) return null
        try {
            return LocalTime.parse(valAsString, timeFormatter)
        } catch (e: DateTimeParseException) {
            throw Exception("Cannot parse '$valAsString' to LocalTime.")
        }
    }

    fun toString (valAsLocalTime: LocalTime?): String? {
        if (valAsLocalTime == null) return null
        return timeFormatter.format(valAsLocalTime)
    }

    // LocalDateTime:

    fun toLocalDateTime (valAsString: String?): LocalDateTime? {
        if (valAsString == null) return null
        try {
            return LocalDateTime.parse(valAsString, dateTimeFormatter)
        } catch (e: DateTimeParseException) {
            throw Exception("Cannot parse '$valAsString' to LocalDateTime.")
        }
    }

    fun toString (valAsLocalDateTime: LocalDateTime?): String? {
        if (valAsLocalDateTime == null) return null
        return dateTimeFormatter.format(valAsLocalDateTime)
    }
}