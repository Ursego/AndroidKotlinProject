package ca.intfast.iftimer.appwide

/****************************************************************************************************************************
Container for constants, by which the application preferences are accessed
****************************************************************************************************************************/

object PrefKey {
    const val LAST_APP_STATE_SET_BY_USER = "LAST_APP_STATE_SET_BY_USER"

    // Settings screen:
    const val MAXIMUM_MEAL_MINUTES = "MAX_MEAL_MINUTES"
    const val MAXIMUM_EW_HOURS = "MAX_EW_HOURS"
    const val MINIMUM_BETWEEN_MEALS_HOURS = "MIN_HOURS_BETWEEN_MEALS"
    const val BEEP_ON_ALARM = "BEEP_ON_ALARM"
    const val VIBRATE_ON_ALARM = "VIBRATE_ON_ALARM"
    const val VIBRATE_ON_BUTTON_CLICK = "VIBRATE_ON_BUTTON_CLICK"
    const val USE_LONG_CLICK = "USE_LONG_CLICK"
//    const val SHOW_SECONDS = "SHOW_SECONDS"
//    const val REMIND_GO_TO_SLEEP = "REMIND_GO_TO_SLEEP"
//    const val MINIMUM_BEFORE_SLEEP_HOURS = "MINIMUM_BEFORE_SLEEP_HOURS"
}

/*
AppPrefs.put(PrefKey.REMINDER_TITLE, aaa, this)
val aaa = AppPrefs.getBoolean(PrefKey.BEEP_ON_ALARM, this)
val aaa = AppPrefs.getInt(PrefKey.MIN_BETWEEN_MEALS_HOURS, this)
if (AppPrefs.getBoolean(PrefKey.REMIND_1_HOUR_BEFORE_EW_END, this)) {

    }
 */