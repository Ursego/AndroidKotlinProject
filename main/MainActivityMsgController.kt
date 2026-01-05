package ca.intfast.iftimer.main

import android.annotation.SuppressLint
import android.content.Context
import ca.intfast.iftimer.R
import ca.intfast.iftimer.appwide.AppState
import ca.intfast.iftimer.appwide.DurationController
import ca.intfast.iftimer.appwide.PrefKey
import ca.intfast.iftimer.appwide.beepAndVibrate
import ca.intfast.iftimer.cycle.CycleController
import ca.intfast.iftimer.util.CustomAppCompatActivity
import ca.intfast.iftimer.util.InfoMsg
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivityMsgController (private val dur: DurationController, private val cyc: CycleController, private val context: Context) {

    // INFORMATION MESSAGES:

    fun msgEwDuration() {
        val ewDuration = dur.todayEwDuration()
//        val showSeconds = AppPrefs.getBoolean(PrefKey.SHOW_SECONDS, context)
        val ewDurationAsString = dur.stringFromDuration(ewDuration/*, showSeconds*/)
        val EIGHT_HOURS_AS_MILLIS = 28800000
        val titleR: Int
        val msgR: Int

        if (ewDuration.toMillis() <= EIGHT_HOURS_AS_MILLIS) {
            titleR = R.string.word__good
            msgR = R.string.msg__ew_duration_good
        } else {
            titleR = R.string.word__bad
            msgR = R.string.msg__ew_duration_bad
        }

        InfoMsg.show(title = context.getString(titleR), msg = context.getString(msgR, ewDurationAsString), context = context)
    } // msgEwDuration()

    fun msgMealFinishedByForceSinceOneHourAchieved(abandoned: Boolean) {
        // Called from MainActivity.finishMealByForceIfOneHourAchieved().

        // abandoned:
        //      true - finishMealByForceIfOneHourAchieved() was called from onResumeCheckInactivity()
        //      false - finishMealByForceIfOneHourAchieved() was called from onChronometerTick()

        val title: String
        val msg: String

        if (abandoned) {
            val chronometerLabelText = when (true) {
                AppState.betweenMeals -> context.getString(R.string.word__after_meal_1) // "AFTER MEAL 1"
                AppState.fasting -> context.getString(R.string.word__fw) // "FASTING WINDOW"
                else -> null // that will never happen
            }

            // "You didn't mark your most recent meal as finished."
            title = context.getString(R.string.msg__meal_one_hour__abandoned__title)
            // "That was done automatically when the meal time reached one hour. The %1$s timer is now counting from that moment."
            msg = context.getString(R.string.msg__meal_one_hour__abandoned, chronometerLabelText)
        } else {
            beepAndVibrate(context) // app is open, so let's draw user attention

            // "You have been eating for 1 hour already!"
            title = context.getString(R.string.msg__meal_one_hour__title)
            // "That is unacceptable, stop eating NOW!!! The meal has been marked as finished."
            msg = context.getString(R.string.msg__meal_one_hour)
        }

        InfoMsg.show(title, msg, context)
    } // msgMealFinishedByForceSinceOneHourAchieved()

    fun msgDayMarkedAsOmad(appStateWhenAbandoned: String? = null) {
        // Called from MainActivity.makeCurrCycleOmad()
        val titleR = if (appStateWhenAbandoned == null /* day was marked as OMAD by user from menu */)
            R.string.msg__curr_day_marked_as_omad__title
        else // makeCurrCycleOmad() automatically called from MainActivity.onChronometerTick() or MainActivity.onResumeCheckInactivity()
            R.string.msg__prev_day_marked_as_omad__title

        val msgR = when (appStateWhenAbandoned) {
            AppState.BETWEEN_MEALS -> R.string.msg__prev_day_marked_as_omad__bm
            null /* makeCurrCycleOmad() called by user from menu */ -> R.string.msg__curr_day_marked_as_omad
            else -> throw Exception("msgDayMarkedAsOmad must never be called on abandonedOnAppState = '$appStateWhenAbandoned'.")
        }

        InfoMsg.show(title = context.getString(titleR), msg = context.getString(msgR), context = context)
    } // msgDayMarkedAsOmad()

    fun msgCopyright() { // "Meal length must be at least %1$s minutes."
        InfoMsg.show(title = context.getString(R.string.msg__copyright__title),
            msg = context.getString(R.string.msg__copyright),
            context = context)
    } // msgCopyright()

    // BOTTOM MESSAGE:

    fun generateBottomMsg(oldBottomMsg: String, maxHoursInWindowsProgressBar: Int, timersColor: Int): String {
        if (AppState.meal1 || AppState.meal2) {
            if (timersColor == MainActivity.GREEN) return ""
            if (oldBottomMsg == "" /* beep & vibrate only once - not on each tick */) beepAndVibrate(context)
            return context.getString(R.string.bottom_msg__meal_red)
        }

        if (AppState.betweenMeals && timersColor == MainActivity.GREEN)
            return generateBottomMsgBetweenMealsGreen(maxHoursInWindowsProgressBar)

        if (AppState.fasting && !cyc.atLeastOneCycleExistsInDb())
            return "" // user cancelled first MEAL 1 after app install - we need to erase "YOU ARE NOT HUNGRY ANYMORE!" which could be in bottom msg

        val generateMsg = when (true) {
            AppState.betweenMeals -> true // color is red if this code reached
            AppState.fasting      -> (timersColor == MainActivity.RED)
            else                  -> return "[MainActivityMsgController.generateBottomMsg should never be called on ${AppState.curr}]"
        }

        if (generateMsg)
            return generateBottomMsgRed(maxHoursInWindowsProgressBar)

        return "" // display nothing in bottom message
    } // generateBottomMsg()

    private fun generateBottomMsgBetweenMealsGreen(maxHoursInWindowsProgressBar: Int): String {
        // "You have %1$s\nto start meal 2 (%2$s at the latest).\nEating window ends at %3$s"
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        var maxEwHours = maxHoursInWindowsProgressBar//AppPrefs.getInt(PrefKey.MAX_EW_HOURS, context)
        if (maxEwHours < 8) {
            val ewMinutesUpToNow = dur.getEwMinutesUpToNow()
            if (ewMinutesUpToNow > maxEwHours * 60) {
                // The 4-7 hours EW, set by user in Settings, is over, but user didn't eat MEAL 2.
                // EW, displayed in EW timer's progress bar, has been prolonged to maximum allowed 8 hours in
                // MainActivity.setGuiWindowsProgressBar(). Bottom message must behave accordingly:
                maxEwHours = 8
            }
        }

        val ewEndAsLdt = cyc.ewStart.plusHours(maxEwHours.toLong())!!
        val ewEndAsLt = ewEndAsLdt.toLocalTime()
        val ewEndAsString = ewEndAsLt.format(formatter)

        val maxMealMinutes = CustomAppCompatActivity.getInt(PrefKey.MAXIMUM_MEAL_MINUTES, context)
        val nextMealAsLdt = ewEndAsLdt.plusMinutes((maxMealMinutes * -1).toLong())
        val nextMealAsLt = nextMealAsLdt.toLocalTime()
        val nextMealAsString = nextMealAsLt.format(formatter)
        val youHaveDuration = Duration.between(LocalDateTime.now(), nextMealAsLdt.plusMinutes(1)/*.plusSeconds(1)*/)
        var youHaveDurationAsString = dur.stringFromDuration(youHaveDuration)
        if (youHaveDurationAsString == "")
            youHaveDurationAsString = "[MainActivityMsgController.generateBottomMsgBetweenMealsGreen failed to calc youHaveDurationAsString]"

        val msg = context.getString(R.string.bottom_msg__bm_green, youHaveDurationAsString, nextMealAsString, ewEndAsString)
        return msg.replace(oldValue = "..", newValue = ".") // "p.m.." > "p.m."
    } // generateBottomMsgBetweenMealsGreen()

    @SuppressLint("StringFormatInvalid")
    private fun generateBottomMsgRed(maxHoursInWindowsProgressBar: Int): String {
        val hoursBeforeNextMeal: Int
        val stageStartLdt: LocalDateTime

        when (true) {
            AppState.betweenMeals -> {
                hoursBeforeNextMeal = CustomAppCompatActivity.getInt(PrefKey.MINIMUM_BETWEEN_MEALS_HOURS, context)
                stageStartLdt = cyc.betweenMealsStart
            }
            AppState.fasting -> {
                hoursBeforeNextMeal = 16
                stageStartLdt = cyc.lastMealFinish!! // now, it returns currCycle.fastingStart
            }
            else -> return "[MainActivityMsgController.generateBottomMsgRed should never be called on ${AppState.curr} <<1>>]"
        }

        val nextMealAsLdt = stageStartLdt.plusHours(hoursBeforeNextMeal.toLong())
        val nextMealAsLt = nextMealAsLdt.toLocalTime()
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        val nextMealAsString = nextMealAsLt.format(formatter)
        var waitDuration = Duration.between(
            LocalDateTime.now(),
            nextMealAsLdt.plusMinutes(1).plusSeconds(1)
        )
        // Avoid msg "Wait X hours 1 minute" which is displayed for a second when a betweenMeals/FW starts -
        // instead, display "Wait X hours" immediately:
        val hours = when (true) {
            AppState.fasting -> 16
            AppState.betweenMeals -> CustomAppCompatActivity.getInt(PrefKey.MINIMUM_BETWEEN_MEALS_HOURS, context) // 2, 3 or 4
            else -> null // that will never happen
        }
        if (waitDuration.toMinutes().toInt() == hours!! * 60 + 1)
            waitDuration = waitDuration.plusMinutes(-1) // that will remove "1 minute" from the message

        val waitDurationAsString = dur.stringFromDuration(waitDuration)

        val msg = when (true) {
            AppState.betweenMeals -> {
                val ewEndAsLdt = cyc.ewStart.plusHours(maxHoursInWindowsProgressBar.toLong())!!
                val ewEndAsLt = ewEndAsLdt.toLocalTime()
                val ewEndAsString = ewEndAsLt.format(formatter)
                val maxMealMinutes = CustomAppCompatActivity.getInt(PrefKey.MAXIMUM_MEAL_MINUTES, context)
                val nextMealLatestAsLdt = ewEndAsLdt.plusMinutes((maxMealMinutes * -1).toLong())
                val nextMealLatestAsString = nextMealLatestAsLdt.format(formatter)

                if (nextMealAsLdt < nextMealLatestAsLdt) {
                    if (waitDurationAsString != "") {
                        // "Wait %1$s before next meal.\n\nStart it between %2$s and %3$s.\n\nEating window ends at %4$s"
                        context.getString(R.string.bottom_msg__bm_red__range,
                            waitDurationAsString, nextMealAsString, nextMealLatestAsString, ewEndAsString)
                    } else {
                        // "Eating window ends at %1$s.\nIf you start %2$s-minutes meal now,\nyou will exceed %3$s-hours eating window."
                        val eightHoursEwEndAsLdt = cyc.ewStart.plusHours(8)!!
                        val eightHoursEwEndAsLt = eightHoursEwEndAsLdt.toLocalTime()
                        val eightHoursEwEndAsString = eightHoursEwEndAsLt.format(formatter)
                        context.getString(R.string.bottom_msg__bm_red__will_exceed, eightHoursEwEndAsString, maxMealMinutes.toString())
                    }
                } else {
                    // That can happen when the meals are so long that it's impossible to keep the EW (for example, EW = 4 hours;
                    // meal 1 was 40 min, and meal 2 supposed to be 20 min or more - the 4 hours are covered).
                    // So, display only one time - meal start (not the range - 2nd time would be equal or prior to the 1st!).
                    // "Wait %1$s before next meal.\n\nStart it at %2$s.\n\nEating window ends at %3$s"
                    context.getString(R.string.bottom_msg__bm_red__start_only, waitDurationAsString, nextMealAsString, ewEndAsString)
                }
            }
            AppState.fasting -> {
                // "Next meal: %1$s or later. Wait %2$s.":
                context.getString(R.string.bottom_msg__fasting_red, nextMealAsString, waitDurationAsString)
            }
            else -> return "[MainActivityMsgController.generateBottomMsgRed should never be called on ${AppState.curr} <<2>>]"
        } // val msg = when (true)

        return msg.replace(oldValue = "..", newValue = ".") // "p.m.." > "p.m."
    } // generateBottomMsgRed()


//    // ERROR MESSAGES (DISPLAYED WHEN BUTTON CLICK DECLINED):
//
//    fun errMsgMealTooShort(minimumMealMinutes: Int) { // "Meal length must be at least %1$s minutes."
//        InfoMsg.show(title = context.getString(R.string.msg__too_short_meal__title),
//            msg = context.getString(R.string.msg__too_short_meal__error, minimumMealMinutes.toString()),
//            context = context)
//    } // errMsgMealTooShort()
//
//    fun errMsgBetweenMealsTooShort(minimumHours: Int) {
//        // "The gap between the meals must be at least %1$s hours to allow the previous food be properly digested."
//        InfoMsg.show(title = context.getString(R.string.msg__too_short_between_meals__title),
//            msg = context.getString(R.string.msg__too_short_between_meals, minimumHours.toString()),
//            context = context)
//    } // errMsgBetweenMealsTooShort()
//
//    fun errMsgFastingTooShort(minimumFastingHours: Int) { // "The fasting must be at least %1$s hours (in ideal - at last 16 hours)."
//        InfoMsg.show(title = context.getString(R.string.msg__too_short_fast__title),
//            msg = context.getString(R.string.msg__too_short_fast__error, minimumFastingHours.toString()),
//            context = context)
//    } // errMsgFastingTooShort()

//    // REMINDERS - MESSAGES THAT POPUP EVEN WHEN THE APP IS IDLING:
//
//    fun setReminderEwWillEndInOneHour() { // called from MainActivity.startMeal1()
//        cancelReminderEwWillEndInOneHour() // maybe, the reminder was set by meal 1, which was cancelled after that
//        if (!AppPrefs.getBoolean(PrefKey.REMIND_1_HOUR_BEFORE_EW_END, context)) return
//
//        // If user selected an EW less than 8 hours, remind an hour before its end:
//        val maxEwHours = AppPrefs.getInt(PrefKey.MAX_EW_HOURS, context)
//        if (maxEwHours < 8 /* 4, 5, 6 or 7 */) {
//            reminderController.remindAfterHours((maxEwHours - 1),
//                context.getString(R.string.alert__ew_will_end_in_1_h__title),
//                context.getString(R.string.alert__ew_will_end_in_1_h___less_than_8_h_ew),
//                ReminderRequestCode.ONE_HOUR_BEFORE_EW_END, context)
//        }
//
//        // Anyway, remind an hour before the max. allowed 8-hours EW's end
//        // (so, there can be two reminders if user didn't eat meal 2 after the first reminder):
////        reminderController.remindAfterSeconds(10,
//        reminderController.remindAfterHours(7,
//            context.getString(R.string.alert__ew_will_end_in_1_h__title),
//            context.getString(R.string.alert__ew_will_end_in_1_h___8_h_ew),
//            ReminderRequestCode.ONE_HOUR_BEFORE_EW_END, context)
//    }
//
//    fun cancelReminderEwWillEndInOneHour() {
//        // Called from MainActivity: startMeal2(), makeCurrCycleOmad() & onResumeCheckInactivity()
////        if (!AppPrefs.getBoolean(PrefKey.REMIND_1_HOUR_BEFORE_EW_END, context)) return
//
//        reminderController.cancelReminder(ReminderRequestCode.ONE_HOUR_BEFORE_EW_END, context)
//    }
//
//    fun setReminderYouCanGoToSleep(windowsChronometerText: String) {
//        // if (remindGoToSleep && <minBeforeSleepHours> hours after fasting start have passed), display alarm:
//        // "You can go to sleep!"
//        // "%1$s hours after eating have passed."
//
//        if (!AppState.fasting) return // relevant only during fasting
//
//        val remindToGoToSleep = AppPrefs.getBoolean(PrefKey.REMIND_GO_TO_SLEEP, context)
//        if (!remindToGoToSleep) return
//
//        val minimumBeforeSleepHours = AppPrefs.getInt(PrefKey.MINIMUM_BEFORE_SLEEP_HOURS, context)
//        if (windowsChronometerText != "0$minimumBeforeSleepHours:00:00") return
//
//        InfoMsg.alert (
//            title = context.getString(R.string.alert__minimum_before_sleep_hours__title), // "You can go to sleep!"
//            msg = context.getString(R.string.alert__minimum_before_sleep_hours,
//                                        minimumBeforeSleepHours.toString()), // "%1$s hours after eating have passed."
//            context = context
//        )
//    }
}