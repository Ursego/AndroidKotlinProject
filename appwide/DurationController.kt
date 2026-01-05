package ca.intfast.iftimer.appwide

import android.content.Context
import ca.intfast.iftimer.R
import ca.intfast.iftimer.cycle.CycleController
import ca.intfast.iftimer.util.CustomAppCompatActivity
import java.time.Duration
import java.time.LocalDateTime

class DurationController(private val cyc: CycleController, val context: Context) {
    fun getMealMinutesUpToNow() = getMealDurationUpToNow().toMinutes().toInt()
   
    fun getBetweenMealsMinutesUpToNow() = getBetweenMealsDurationUpToNow().toMinutes().toInt()
   
    fun getEwMinutesUpToNow() = getEwDurationUpToNow().toMinutes().toInt()
   
    fun getFastingMinutesUpToNow() = getFastingDurationUpToNow().toMinutes().toInt()
   
    fun getBetweenMealsSecondsUpToNow(): Int {
        val seconds = getBetweenMealsDurationUpToNow().toMillis() / 1000
        return seconds.toInt()
    }
   
    fun getMealSecondsUpToNow(): Int {
        val seconds = getMealDurationUpToNow().toMillis() / 1000
        return seconds.toInt()
    }
   
    fun todayEwDuration(): Duration {
        if (!AppState.fasting) throw Exception("It should be called only on fasting, not on ${AppState.curr}.")
        return Duration.between(cyc.ewStart, cyc.ewFinish)
    }
   
//    fun getActualEwLength(): Int {
//        // If EW has not achieved the max EW length, selected in Settings, returns that length.
//        // If achieved, returns 8 (that means, that the progress bar is displaying now something like "8 h (extended from 6)").
//        if (AppState.fasting) throw Exception("It should not be called on fasting.")
//        val maxEwHours = AppPrefs.getInt(PrefKey.MAXIMUM_EW_HOURS, context)
//        val ewMinutesUpToNow = getEwMinutesUpToNow()
//
//        return if (ewMinutesUpToNow <= maxEwHours * 60) maxEwHours else 8
//    }

    // FUNCTIONS CALLED WHEN USER CLICKS ON START/FINISH MEAL BUTTON:

    fun mealTooShort(minimumMealMinutes: Int): Boolean {
        if (!(AppState.meal1 || AppState.meal2)) throw Exception("It should be called only during a meal, not on ${AppState.curr}.")
        return (getMealMinutesUpToNow() < minimumMealMinutes)
    }
   
    fun mealTooLong(): Boolean {
        if (!(AppState.meal1 || AppState.meal2)) throw Exception("It should be called only during a meal, not on ${AppState.curr}.")
        val mealMinutesUpToNow = getMealDurationUpToNow().toMinutes()
        val maxMealMinutes = CustomAppCompatActivity.getInt(PrefKey.MAXIMUM_MEAL_MINUTES, context)
        return (mealMinutesUpToNow >= maxMealMinutes)
    }
   
    fun fastingTooShort(minimumFastingHours: Int): Boolean {
        if (!AppState.fasting) throw Exception("It should be called only on fasting, not on ${AppState.curr}.")
        if (!cyc.atLeastOneCycleExistsInDb()) return false
        return (hoursAfterLastMealFinish() < minimumFastingHours)
    }
   
    fun betweenMealsTooShort(minimumBetweenMealsHours: Int): Boolean {
        if (!AppState.betweenMeals) throw Exception("It should be called only between meals, not on ${AppState.curr}.")
        return (hoursAfterLastMealFinish() < minimumBetweenMealsHours)
    }
   
    fun enoughTimeToFinishMeal2Inside8HoursEw(): Boolean {
        if (!AppState.betweenMeals) throw Exception("It should be called only between meals, not on ${AppState.curr}.")
        val maxMealMinutes = CustomAppCompatActivity.getInt(PrefKey.MAXIMUM_MEAL_MINUTES, context)
        val ewEndAsLdt = cyc.ewStart.plusHours(8)!!
        return (LocalDateTime.now()!!.plusMinutes(maxMealMinutes.toLong()) <= ewEndAsLdt)
    } // enoughTimeToFinishMeal2Inside8HoursEw()

    // FUNCTIONS WHICH CONVERT TIME TO USER-READABLE STRINGS:

    fun stringFromDuration(d: Duration /*, showSeconds: Boolean = false*/): String {
        // Returns human-readable representation of the Duration (like "2 days 23 hours 1 minute 59 seconds")
        val days = d.toDays().toInt()
        val hours = d.toHours().toInt()
        val minutes = d.toMinutes().toInt() % 60

        val buf = StringBuffer()
        var frag: String

        if (days > 0) {
            frag = "$days " + context.resources.getQuantityString(
                R.plurals.word__day,
                days
            ) + " "
            buf.append(frag)
        }

        if (hours > 0) {
            if (hours > 23) throw
            Exception("DurationController.stringFromDuration(): hours, extracted from Duration '$d', cannot be $hours.")
            frag = "$hours " + context.resources.getQuantityString(
                R.plurals.word__hour,
                hours
            ) + " "
            buf.append(frag)
        }

        if (minutes > 0) {
            if (minutes > 59) throw
            Exception("DurationController.stringFromDuration(): minutes, extracted from Duration '$d', cannot be $minutes.")
            frag = "$minutes " + context.resources.getQuantityString(
                R.plurals.word__minute,
                minutes
            ) + " "
            buf.append(frag)
        }

//        if (showSeconds) {
//            val seconds = ((d.toMillis() / 1000) % 60).toInt()
//            if (seconds > 0) {
//                if (seconds > 59) throw
//                Exception("DurationController.stringFromDuration(): seconds, extracted from Duration '$d', cannot be $seconds.")
//                frag = seconds.toString() + " " + context.resources.getQuantityString(
//                    R.plurals.word__second,
//                    seconds
//                )
//                buf.append(frag)
//            }
//        }

        return buf.toString().trim()
    }
   
    fun stringFromMinutes(minutes: Int): String = stringFromDuration(Duration.ofMinutes(minutes.toLong()))

    // PRIVATE:

    private fun getMealDurationUpToNow() = Duration.between(cyc.lastMealStart, LocalDateTime.now())!!
   
    private fun getBetweenMealsDurationUpToNow() = Duration.between(cyc.betweenMealsStart, LocalDateTime.now())!!
   
    private fun getEwDurationUpToNow() = Duration.between(cyc.ewStart, LocalDateTime.now())!!
   
    private fun getFastingDurationUpToNow() = Duration.between(cyc.lastMealFinish, LocalDateTime.now())!!
   
    private fun afterLastMealFinish(): Duration? {
        if (cyc.lastMealFinish == null) return null // no one meal has ever been finished after app install
        return Duration.between(cyc.lastMealFinish, LocalDateTime.now())
    }
   
    private fun hoursAfterLastMealFinish(): Long {
        val afterLastMealFinish = afterLastMealFinish() ?: throw Exception ("No meal of current cycle was finished.")
        return afterLastMealFinish.toHours()
    }
}