package ca.intfast.iftimer.pref

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import ca.intfast.iftimer.R
import ca.intfast.iftimer.appwide.PrefKey
import ca.intfast.iftimer.util.AppPrefs
import ca.intfast.iftimer.util.InfoMsg
import ca.intfast.iftimer.util.PreferenceFragmentAutomaticSummary

class PrefFragment: PreferenceFragmentAutomaticSummary(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val screen = preferenceManager.createPreferenceScreen(preferenceManager.context)
        var dropDownPref: DropDownPreference
        var switchPref: SwitchPreferenceCompat

        // Maximum eating window length (hours):
        dropDownPref = DropDownPreference(context)
        dropDownPref.key = PrefKey.MAXIMUM_EW_HOURS
        dropDownPref.title = getString(R.string.pref_title__max_ew_hours)
        dropDownPref.isSingleLineTitle = false
        dropDownPref.entryValues /* what we save */ = arrayOf(/*"1", "2",*/ "4", "5", "6", "7", "8")
        dropDownPref.entries /* what we show to user */ = dropDownPref.entryValues
        dropDownPref.setDefaultValue("8")
        dropDownPref.setOnPreferenceChangeListener { preference, newValue -> ewOk(preference, newValue) }
        screen.addPreference(dropDownPref)

        // Maximum meal length (minutes):
        dropDownPref = DropDownPreference(context)
        dropDownPref.key = PrefKey.MAXIMUM_MEAL_MINUTES
        dropDownPref.title = getString(R.string.pref_title__max_meal_minutes)
        dropDownPref.isSingleLineTitle = false
        dropDownPref.entryValues /* what we save */ = arrayOf(/*"1",*/ "15", "20", "25", "30", "35", "40")
        dropDownPref.entries /* what we show to user */ = dropDownPref.entryValues
        dropDownPref.setDefaultValue("30")
        dropDownPref.setOnPreferenceChangeListener { preference, newValue -> ewOk(preference, newValue) }
        screen.addPreference(dropDownPref)

        // Minimum gap between meals (hours):
        dropDownPref = DropDownPreference(context)
        dropDownPref.key = PrefKey.MINIMUM_BETWEEN_MEALS_HOURS
        dropDownPref.title = getString(R.string.pref_title__min_between_meals_hours)
        dropDownPref.isSingleLineTitle = false
        dropDownPref.entryValues /* what we save */ = arrayOf("0", "3", "4", "5")
        dropDownPref.entries /* what we show to user */ = dropDownPref.entryValues
        dropDownPref.setDefaultValue("3")
        dropDownPref.setOnPreferenceChangeListener { preference, newValue -> ewOk(preference, newValue) }
        screen.addPreference(dropDownPref)

//        // Remind to prepare meal 2 one hour before eating window is over:
//        switchPref = SwitchPreferenceCompat(context)
//        switchPref.key = PrefKey.REMIND_1_HOUR_BEFORE_EW_END
//        switchPref.title = getString(R.string.pref_title__remind_1_hour_before_ew_end) "Remind to prepare meal 2 one hour before eating window is over"
//        switchPref.isSingleLineTitle = false
//        switchPref.setDefaultValue(true)
//        screen.addPreference(switchPref)

        // Beep on messages which says to finish meal:
        switchPref = SwitchPreferenceCompat(context)
        switchPref.key = PrefKey.BEEP_ON_ALARM
        switchPref.title = getString(R.string.pref_title__beep_on_alarm)
        switchPref.isSingleLineTitle = false
        switchPref.setDefaultValue(true)
        screen.addPreference(switchPref)

        // Vibrate on messages which says to finish meal:
        switchPref = SwitchPreferenceCompat(context)
        switchPref.key = PrefKey.VIBRATE_ON_ALARM
        switchPref.title = getString(R.string.pref_title__vibrate_on_alarm)
        switchPref.isSingleLineTitle = false
        switchPref.setDefaultValue(true)
        screen.addPreference(switchPref)

        // Vibrate shortly on button click:
        switchPref = SwitchPreferenceCompat(context)
        switchPref.key = PrefKey.VIBRATE_ON_BUTTON_CLICK
        switchPref.title = getString(R.string.pref_title__vibrate_on_button_click)
        switchPref.isSingleLineTitle = false
        switchPref.setDefaultValue(true)
        screen.addPreference(switchPref)

        // Hold button for half a second to avoid accidental click:
        switchPref = SwitchPreferenceCompat(context)
        switchPref.key = PrefKey.USE_LONG_CLICK
        switchPref.title = getString(R.string.pref_title__use_long_click)
        switchPref.isSingleLineTitle = false
        switchPref.setDefaultValue(false)
        screen.addPreference(switchPref)

//        // Show seconds:
//        switchPref = SwitchPreferenceCompat(context)
//        switchPref.key = PrefKey.SHOW_SECONDS
//        switchPref.title = getString(R.string.pref_title__show_seconds)
//        switchPref.isSingleLineTitle = false
//        switchPref.setDefaultValue(false)
//        screen.addPreference(switchPref)

//        // Time limits:
//        prefCategory = PreferenceCategory(context)
//        prefCategory.key = "MAX_DURATION_CATEGORY"
//        prefCategory.title = getString(R.string.pref_title__time_limits_category)
//        prefCategory.isSingleLineTitle = false
//        screen.addPreference(prefCategory)

//        // Gap between last meal and sleep:
//        prefCategory = PreferenceCategory(context)
//        prefCategory.key = "BEFORE_SLEEP_CATEGORY"
//        prefCategory.title = getString(R.string.pref_title__before_sleep_category)
//        prefCategory.isSingleLineTitle = false
//        screen.addPreference(prefCategory)
//
//            // Remind 'You can go to sleep since X hours after eating have passed'
//            switchPref = SwitchPreferenceCompat(context)
//            switchPref.key = PrefKey.REMIND_GO_TO_SLEEP
//            switchPref.title = getString(R.string.pref_title__remind_go_to_sleep)
//            switchPref.isSingleLineTitle = false
//            switchPref.setDefaultValue(false)
//            screen.addPreference(switchPref)
//
//            // Min. hours between last meal and going to sleep (at least 3 recommended)
//            dropDownPref = DropDownPreference(context)
//            dropDownPref.key = PrefKey.MINIMUM_BEFORE_SLEEP_HOURS
//            dropDownPref.title = getString(R.string.pref_title__min_before_sleep_hours)
//            dropDownPref.isSingleLineTitle = false
//            dropDownPref.entryValues /* what we save */ = arrayOf("1", "2", "3", "4")
//            dropDownPref.entries /* what we show to user */ = dropDownPref.entryValues
//            dropDownPref.setDefaultValue("3")
//            screen.addPreference(dropDownPref)

        // On alarm and reminder messages, also:
//        prefCategory = PreferenceCategory(context)
//        prefCategory.key = "ON_ALARM_CATEGORY"
//        prefCategory.title = getString(R.string.pref_title__on_alarm_category)
//        prefCategory.isSingleLineTitle = false
//        screen.addPreference(prefCategory)


        preferenceScreen = screen
    }

    private fun ewOk(preference: Preference, newValue: Any): Boolean {
        val maxMealMinutes: Int
        val minBetweenMealsHours: Int
        val maxEwHours: Int
        when (preference.key) {
            PrefKey.MAXIMUM_MEAL_MINUTES -> {
                maxMealMinutes = (newValue as String).toInt()
                minBetweenMealsHours = AppPrefs.getInt(PrefKey.MINIMUM_BETWEEN_MEALS_HOURS, context!!)
                maxEwHours = AppPrefs.getInt(PrefKey.MAXIMUM_EW_HOURS, context!!)
            }
            PrefKey.MINIMUM_BETWEEN_MEALS_HOURS -> {
                maxMealMinutes = AppPrefs.getInt(PrefKey.MAXIMUM_MEAL_MINUTES, context!!)
                minBetweenMealsHours = (newValue as String).toInt()
                maxEwHours = AppPrefs.getInt(PrefKey.MAXIMUM_EW_HOURS, context!!)
            }
            PrefKey.MAXIMUM_EW_HOURS -> {
                maxMealMinutes = AppPrefs.getInt(PrefKey.MAXIMUM_MEAL_MINUTES, context!!)
                minBetweenMealsHours = AppPrefs.getInt(PrefKey.MINIMUM_BETWEEN_MEALS_HOURS, context!!)
                maxEwHours = (newValue as String).toInt()
            }
            else -> throw Exception("PrefFragment.ewOk() shuld bot be called for pref ${preference.key}")
        }

        if ((maxMealMinutes * 2) + (minBetweenMealsHours * 60) <= maxEwHours * 60) return true // accept the new value

        InfoMsg.show (
            // "Value rejected due to mismatch."
            title = context!!.getString(R.string.msg__ew_mismatch__title),
            // "Two meals of %1$s minutes each with %2$s hours gap between them exceed %3$s-hours eating window."
            msg = context!!.getString(R.string.msg__ew_mismatch,
                maxMealMinutes.toString(), minBetweenMealsHours.toString(), maxEwHours.toString()),
            context = context!!
        )
        return false // reject the new value
    }
}