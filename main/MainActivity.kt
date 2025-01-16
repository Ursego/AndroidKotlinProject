package ca.intfast.iftimer.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Chronometer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuCompat
import ca.intfast.iftimer.R
import ca.intfast.iftimer.appwide.AppState
import ca.intfast.iftimer.appwide.DurationController
import ca.intfast.iftimer.appwide.PrefKey
import ca.intfast.iftimer.appwide.vibrate
import ca.intfast.iftimer.cycle.CycleController
import ca.intfast.iftimer.pref.PrefActivity
import ca.intfast.iftimer.stats.StatsActivity
import ca.intfast.iftimer.util.*
import kotlinx.android.synthetic.main.activity_main.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.concurrent.TimeUnit

class MainActivity: AppCompatActivity(), Chronometer.OnChronometerTickListener {
    private lateinit var cyc: CycleController
    private lateinit var dur: DurationController
    private lateinit var msg: MainActivityMsgController
    private val currCycle get() = cyc.getCurrCycle()
    private val dateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    private var mealFinishedByForceDueToInactivity
        get() = AppPrefs.getBoolean("MainActivity.mealFinishedByForceDueToInactivity", this)
        set(value) = AppPrefs.put("MainActivity.mealFinishedByForceDueToInactivity", value, this)

    companion object {
        const val RED = Color.RED
        const val GREEN: Int = -16098048 // return value of Color.rgb(10,93,0)
        const val EIGHT_HOURS_AS_MINUTES = 480
        const val SIXTEEN_HOURS_AS_MINUTES = 960
        const val TWENTY_FOUR_HOURS_AS_MINUTES = 1440
    }
    /***********************************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cyc = CycleController(this)
        dur = DurationController(cyc, this)
        msg = MainActivityMsgController(dur, cyc, this)

        mealChronometerButton.setOnClickListener {
            val useLongClick = AppPrefs.getBoolean(PrefKey.USE_LONG_CLICK, this)
            if (useLongClick) toast(this, getString(R.string.toast__keep_button_for_half_second)) else buttonClicked()
        }

        mealChronometerButton.setOnLongClickListener {
            val useLongClick = AppPrefs.getBoolean(PrefKey.USE_LONG_CLICK, this)
            if (useLongClick) buttonClicked()
            // Return true to inform the calling function that the callback has been consumed the long click:
            true
            // Returning true tells the framework that the touch event is consumed and no further event handling
            // is required, i.e. there is no need to handle the regular (short) click which happened as well.
        }

        // On each tick of each Chronometer, onChronometerTick() will be called:
        mealChr.onChronometerTickListener = this
        windowsChr.onChronometerTickListener = this
        
        // Next controls are displayed only when FASTING WINDOW timer is green:
        windowsChrDays.setTextColor(GREEN)
        windowsChrDDHHMM.setTextColor(GREEN)
    } // onCreate()
    /***********************************************************************************************************************/
    override fun onStart() {
        super.onStart()

        if (!AppPrefs.getBoolean("PREF_ACTIVITY_SHOWN_ON_START", this)) {
            startActivity(Intent(this, PrefActivity::class.java))
            AppPrefs.put("PREF_ACTIVITY_SHOWN_ON_START", true, this)
        }
    } // onStart()
    /***********************************************************************************************************************/
    private fun buttonClickDeclined(): Boolean {
        // Minimum time periods for the button click to be declined:
        val MINIMUM_MEAL_MINUTES = 2 // before 10 minutes, user can proceed through confirmation message
        val MINIMUM_BETWEEN_MEALS_HOURS = 1 // before 3, 4 or 5 hours, user can proceed through confirmation message
        val MINIMUM_FASTING_HOURS = 3 // before 16 hours, user can proceed through confirmation message

        val errMsgFunc: () -> Unit = when (true) {
            (AppState.meal1 || AppState.meal2) && dur.mealTooShort(minimumMealMinutes = MINIMUM_MEAL_MINUTES)
                                        -> { { msg.errMsgMealTooShort(minimumMealMinutes = MINIMUM_MEAL_MINUTES) } }
            AppState.betweenMeals && dur.betweenMealsTooShort(minimumBetweenMealsHours = MINIMUM_BETWEEN_MEALS_HOURS)
                                        -> { { msg.errMsgBetweenMealsTooShort(minimumHours = MINIMUM_BETWEEN_MEALS_HOURS) } }
            AppState.fasting && dur.fastingTooShort(minimumFastingHours = MINIMUM_FASTING_HOURS)
                                        -> { { msg.errMsgFastingTooShort(minimumFastingHours = MINIMUM_FASTING_HOURS) } }
            else                        -> return false // buttonClicked() accepted
        }
        errMsgFunc.invoke()
        return true // buttonClicked() declined
    } // buttonClickedDeclined()
    /***********************************************************************************************************************/
    private fun buttonClicked() {
        if (buttonClickDeclined()) return

        if (AppPrefs.getBoolean(PrefKey.VIBRATE_ON_BUTTON_CLICK, this)) vibrate(this,100)

        if (AppState.betweenMeals && !dur.enoughTimeToFinishMeal2Inside8HoursEw()) {
            InfoMsg.modalDialog(
                title = getString(R.string.word__are_you_sure),
                msg = bottomMsgTextView.text as String, // now it is displaying bottom_msg__bm_red__will_exceed
                funcToCallOnPositiveResponse = { startMeal2() },
                context = this
            )
            return
        }

        val minimumBetweenMealsHours = AppPrefs.getInt(PrefKey.MINIMUM_BETWEEN_MEALS_HOURS, this)

        val tooEarly = when (true) {
            AppState.fasting               -> dur.fastingTooShort(minimumFastingHours = 16)
            AppState.meal1, AppState.meal2 -> dur.mealTooShort(minimumMealMinutes = 10)
            AppState.betweenMeals          -> dur.betweenMealsTooShort(minimumBetweenMealsHours = minimumBetweenMealsHours)
            else                           -> throw Exception("None of AppState properties is true.")
        }

        if (!tooEarly) {
            ///////////////////////////////////////////////////////////////////////////////////////////
            // Change AppState immediately, without modal dialog to ask user if he is sure:
            ///////////////////////////////////////////////////////////////////////////////////////////
            when (true) {
                AppState.fasting      -> startMeal1()
                AppState.meal1        -> startBetweenMeals()
                AppState.betweenMeals -> startMeal2()
                AppState.meal2        -> startFasting(displayMsgEwDuration = true)
            }
            return
        }

        ///////////////////////////////////////////////////////////////////////////////////////////
        // User wants to change AppState too early (but not as early so the click is declined with
        // an error message). Display confirmation message to ask user if he is sure:
        ///////////////////////////////////////////////////////////////////////////////////////////
        val title: String
        val msg: String
        val func: () -> Unit

        when (true) {
            AppState.fasting -> {
                title = getString(R.string.msg__too_short_fast__title)
                msg = getString(R.string.msg__too_short_fast__confirmation)
                func = { startMeal1() }
            }
            AppState.meal1 -> {
                title = getString(R.string.msg__too_short_meal__title)
                msg = getString(R.string.msg__too_short_meal__confirmation)
                func = { startBetweenMeals() }
            }
            AppState.betweenMeals -> {
                title = getString(R.string.msg__too_short_between_meals__title)
                msg = getString(R.string.msg__too_short_between_meals, minimumBetweenMealsHours.toString())
                func = { startMeal2() }
            }
            AppState.meal2 -> {
                title = getString(R.string.msg__too_short_meal__title)
                msg = getString(R.string.msg__too_short_meal__confirmation)
                func = { startFasting(displayMsgEwDuration = true) }
            }
            else -> throw Exception("None of AppState properties is true.")
        }

        InfoMsg.modalDialog(
            title = title,
            msg = msg,
            funcToCallOnPositiveResponse = func,
            context = this
        )
    } // buttonClicked()
    /***********************************************************************************************************************/
    override fun onChronometerTick(chronometer: Chronometer?) {
        setChrTextByItsBase(chronometer) // like "0:18" or "0:18:59"

        // onChronometerTick() is called TWICE a second since two the timers are being listened.
        // But we want setGui() be called only ONCE a second:
        when (true) {
            // mealChronometer is responsible to set GUI during meals:
            chronometer!!.id == mealChr.id && (AppState.meal1 || AppState.meal2) -> {
                finishMealByForceIfOneHourAchieved(abandoned = false)
                setGui()
            }

            // windowsChronometer is responsible to set GUI during fasting and between meals
            // (between meals - because mealChr is not ticking when Minimum gap between meals = 0):
            chronometer.id == windowsChr.id && (AppState.fasting || AppState.betweenMeals) -> {
                if (AppState.betweenMeals) {
                    val eightHoursEwAchieved = (dur.getBetweenMealsMinutesUpToNow() >= EIGHT_HOURS_AS_MINUTES)
                    if (eightHoursEwAchieved) makeCurrCycleOmad(appStateWhenAbandoned = AppState.BETWEEN_MEALS)
                    // If 8 hour EW is achieved when app is closed, makeCurrCycleOmad() will be called from onResumeCheckInactivity().
                }
                setGui()
            }
        }
    } // onChronometerTick()
    /***********************************************************************************************************************/
    override fun onResume() {
        super.onResume()

        cyc.loadCurrCycleFromDb()

//        // Situation: the "eating window will end in 1 hour" reminder was previously set in startMeal1(). User has just set
//        // the pref "Remind to prepare meal 2 one hour before eating window is over" to false and coming back to MainActivity:
//        if (!AppPrefs.getBoolean(PrefKey.REMIND_1_HOUR_BEFORE_EW_END, this))
//            mainActivityMsgController.cancelReminderEwWillEndInOneHour()

        onResumeCheckInactivity()

        if (mealChr.visibility == View.VISIBLE) resume(mealChr)
        if (windowsChr.visibility == View.VISIBLE) resume(windowsChr)

        setGui()
    } // onResume()
    /***********************************************************************************************************************/
    private fun start(chr: Chronometer, startLdt: LocalDateTime? = null /* null = "start from now" */) {
        val instanceName = when (chr.id) {
            mealChr.id -> "mealChronometer"
            windowsChr.id -> "windowsChronometer"
            else -> ""
        }
        chr.start(instanceName, startLdt, this)

        // If next command would not exist, user would see curr. time in default format (including seconds)
        // for a short moment - until setChronometerTextByItsBase() is called on next onChronometerTick():
        setChrTextByItsBase(chr)
    } // start()
    /***********************************************************************************************************************/
    private fun resume(chronometer: Chronometer) {
        val instanceName = when (chronometer.id) {
            mealChr.id -> "mealChronometer"
            windowsChr.id -> "windowsChronometer"
            else -> ""
        }
        chronometer.resume(instanceName, this)

        // If next command would not exist, user would see curr. time in default format (including seconds)
        // for a short moment - until setChronometerTextByItsBase() is called on next onChronometerTick().
        // That's why this func exists - otherwise we would call Chronometer.resume(...) directly:
        setChrTextByItsBase(chronometer)
    } // resume()
    /***********************************************************************************************************************/

    // ----------------------------------------------------------------------------------------------------------------------
    // FUNCTIONS RELATED TO INACTIVITY AND MAKING THE CURR CYCLE OMAD:
    // ----------------------------------------------------------------------------------------------------------------------

    /***********************************************************************************************************************/
    private fun onResumeCheckInactivity() {
        // Inactivity happens when user didn't click the "START / FINISH MEAL 1 / 2" button for too long time,
        // and the app was on pause at the moment when the AppState came to its max time, so the app was not able
        // so solve the problem by showing a message and changing AppState to the next one automatically.
        // For example: when meal 1 duration comes to 1 hour while app is active, AppState is automatically switched to
        // BETWEEN_MEALS, and mainActivityMsgController.alertMealOneHourAchieved is called. It displays the message:
        // "You are eating already 1 hour!"
        // "That is unacceptable, stop eating NOW!!! The meal has been marked as finished."
        // But if app was on pause in that moment, the meal has not been finished by force,
        // so mealChronometer is now keeping counting over 1 hour. The Chronometers could count for ages,
        // but if user after long time will open app, we want to prepare the timers for Meal 1 of that happy day.
        // That's when onResumeCheckInactivity() saves the situation
        // by resetting the timers and deleting the curr cycle which was abandoned (except of fasting stage -
        // if app was abandoned during fasting, the curr cycle's meals and EW data are ok, so we keep it).

        if (!cyc.currCycleSavedInDb) return

        var appStateWhenAbandoned: String? = null // which AppState was started, but not finished; null = not abandoned

        // Define whether or not the timer has been abandoned by user (i.e. user stopped using the app):
        when (AppPrefs.getString(PrefKey.LAST_APP_STATE_SET_BY_USER, this)) {
            AppState.MEAL_1        -> if (dur.getMealMinutesUpToNow() >= 60)                     appStateWhenAbandoned = AppState.MEAL_1
            AppState.BETWEEN_MEALS -> if (dur.getEwMinutesUpToNow()   >= EIGHT_HOURS_AS_MINUTES) appStateWhenAbandoned = AppState.BETWEEN_MEALS
            AppState.MEAL_2        -> if (dur.getMealMinutesUpToNow() >= 60)                     appStateWhenAbandoned = AppState.MEAL_2
            AppState.FASTING       -> return // it cannot be abandoned on fasting - FW timer is able to count endless days
        }

        // Take remedial action if abandoned:
        when (appStateWhenAbandoned) {
            null -> return // nice, the app is not abandoned!
            AppState.MEAL_1, AppState.MEAL_2 -> {
                // Finish meal only once - not on each onResume().
                // mealFinishedByForceDueToInactivity was set to true in startMeal1() or startMeal2():
                if (!mealFinishedByForceDueToInactivity /* it's first time this func is called on resume */) {
                    mealFinishedByForceDueToInactivity = true // prevent finishing the meal on subsequent onResume()s
                    finishMealByForceIfOneHourAchieved(abandoned = true)
                }
            }
            AppState.BETWEEN_MEALS -> makeCurrCycleOmad(appStateWhenAbandoned)
                                      // If 8 hour EW is achieved when app is running, makeCurrCycleOmad()
                                      // will be called from onChronometerTick().
        }
    } // onResumeCheckInactivity()
    /***********************************************************************************************************************/
    private fun finishMealByForceIfOneHourAchieved(abandoned: Boolean) {
        // abandoned:
        //      true - called from onResumeCheckInactivity() if a meal was abandoned
        //      false - called from onMealChronometerTick() when a meal has just achieved one hour while app is running
        val mealMinutesUpToNow = dur.getMealMinutesUpToNow()
        if (mealMinutesUpToNow < 60) return

        if (!abandoned) {
            // One hour achieved while app is running, so it's guaranteed that we are inside EW now:
            when (true) {
                AppState.meal1 -> startBetweenMeals() // from now
                AppState.meal2 -> startFasting(displayMsgEwDuration = false) // from now
            }
            msg.msgMealFinishedByForceSinceOneHourAchieved(abandoned = false)
            return
        }

        // If this code reached, the app has been abandoned. The app was idling at the moment one hour was achieved.
        // Calculate the length of the not-finished meal as one hour, and finish it by force, starting the next AppState.

        val abandonedMealStartPlusOneHour =
            when (true) {
                AppState.meal1 -> currCycle.meal1Start!!.plusHours(1)
                AppState.meal2 -> currCycle.meal2Start!!.plusHours(1)
                else           -> throw Exception ("Func must be called only during a meal, not on ${AppState.curr}")
            }

        if (mealMinutesUpToNow < EIGHT_HOURS_AS_MINUTES) {
            // We are inside EW now. Finish the meal by force by starting the next AppState:
            when (true) {
                AppState.meal1 -> startBetweenMeals(startLdt = abandonedMealStartPlusOneHour)
                AppState.meal2 -> startFasting(startLdt = abandonedMealStartPlusOneHour, displayMsgEwDuration = false)
            }
        } else /* EW is over - start FW */ {
            // startFasting(), called next, will copy abandonedMealStartPlusOneHour to fastingStart only.
            // But if meal 1 was abandoned, we need to copy it to betweenMealsStart too - to mark the meal as finished:
            if (AppState.meal1) {
                currCycle.betweenMealsStart = abandonedMealStartPlusOneHour
                cyc.update(currCycle)
            }

            startFasting(startLdt = abandonedMealStartPlusOneHour, displayMsgEwDuration = false)
        }

        msg.msgMealFinishedByForceSinceOneHourAchieved(abandoned = true)
    } // finishMealByForceIfOneHourAchieved()
    /***********************************************************************************************************************/
    private fun makeCurrCycleOmad(appStateWhenAbandoned: String? = null) {
        // appStateWhenAbandoned:
        //      not null - automatically called from onChronometerTick() or onResumeCheckInactivity();
        //      null - called by user from menu.

        // Ths func can be called from onResumeCheckInactivity() many times (if, after marking as OMAD, the app
        // resumes many times). But we want the work to be done (and the message to be displayed to user) only once.
        // So, do nothing if Curr Cycle has already been marked as OMAD:
        val alreadyMarkedAsOmad = (currCycle.meal2Start == null && currCycle.fastingStart != null)
        if (alreadyMarkedAsOmad) return

        // Update cycleController.currCycle (meal2Start = null, fastingStart = betweenMealsStart) and save in DB:
        cyc.makeCurrCycleOmad()

        // Start fasting window timer. Use the moment, when between meals started, as starting point of fasting window:
        val betweenMealsStart = currCycle.betweenMealsStart
            ?: throw Exception("MainActivity.makeCurrCycleOmad(): betweenMealsStart is null.")
        start(windowsChr, startLdt = betweenMealsStart)

        if (appStateWhenAbandoned == null /* called by user from menu */) {
            mealChr.finish()
            setGui()
        } else {
            setGuiBottomMsg()
        }

        msg.msgDayMarkedAsOmad(appStateWhenAbandoned)
//        mainActivityMsgController.cancelReminderEwWillEndInOneHour()
    } // makeCurrCycleOmad()
//    /***********************************************************************************************************************/

    // ----------------------------------------------------------------------------------------------------------------------
    // FUNCTIONS CALLED WHEN USER CLICKS "START / FINISH MEAL" BUTTON:
    // ----------------------------------------------------------------------------------------------------------------------

    /***********************************************************************************************************************/
    private fun startMeal1() {
        cyc.startMeal1()
        start(mealChr) // start MEAL 1 timer
        start(windowsChr) // start EATING WINDOW timer
        mealFinishedByForceDueToInactivity = false // reset for onResumeCheckInactivity()
        setGuiMealChr()
        setGuiWindowsChr()
        windowsChrDays.text = ""
        setGuiBottomMsg()
//        mainActivityMsgController.setReminderEwWillEndInOneHour()
    } // startMeal1()
    /***********************************************************************************************************************/
    private fun startBetweenMeals(startLdt: LocalDateTime? = null /* null means "start from now" */) {
        cyc.startBetweenMeals(startLdt)
        start(mealChr, startLdt) // start AFTER MEAL 1 timer
        resume(windowsChr) // it was hidden during Meal 1
        setGuiMealChr()
        setGuiWindowsChr()
        setGuiBottomMsg()
    } // startBetweenMeals()
    /***********************************************************************************************************************/
    private fun startMeal2() {
        cyc.startMeal2()
        start(mealChr) // start MEAL 2 timer
        mealFinishedByForceDueToInactivity = false // reset for onResumeCheckInactivity()
        setGuiMealChr()
        setGuiWindowsChr()
        setGuiBottomMsg()
//        mainActivityMsgController.cancelReminderEwWillEndInOneHour()
    } // startMeal2()
    /***********************************************************************************************************************/
    private fun startFasting(startLdt: LocalDateTime? = null /* null means "start from now" */, displayMsgEwDuration: Boolean) {
        cyc.startFasting(startLdt)
        mealChr.finish() // stop MEAL 2 timer
        start(windowsChr, startLdt) // start FASTING WINDOW timer
        if (displayMsgEwDuration) msg.msgEwDuration()
        setGuiMealChr()
        setGuiWindowsChr()
        setGuiBottomMsg()
//        mainActivityMsgController.setReminderYouCanGoToSleep()
    } // startFasting(displayMsgEwDuration)
    /***********************************************************************************************************************/
    private fun startMeal2WhenDayMarkedAsOmad() {
        // Called through the menu item "START MEAL 2".
        // The day has been marked as OMAD. However, user decided to eat even though the EW will be longer than 8 hours.

        if (!canStartMeal2WhenDayMarkedAsOmad()) {
            // The menu was open when the action was allowed, but the action became prohibited while the menu was kept open:
            InfoMsg.show(title = "", msg = getString(R.string.msg__too_late), context = this)
            return
        }

        currCycle.fastingStart = null // cancel the FW; will be saved in DB by "update(currCycle)" in cyc.startMeal2()

        // Prevent null pointer in setGuiWaitTimeLabel() - even though cyc.startMeal2() will do that too:
        AppState.meal2 = true

        // Now, EATING WINDOW is counting from currCycle.betweenMealsStart since that time was used
        // as the start time of the FW when the day was marked as OMAD.
        // We want it to count from currCycle.meal1Start:
        start(windowsChr, currCycle.meal1Start)

        startMeal2()
    } // startMeal2WhenDayMarkedAsOmad()
    /***********************************************************************************************************************/
    private fun canStartMeal2WhenDayMarkedAsOmad(): Boolean {
        // If the day has been marked as OMAD, MEAL 2 still can be started through the menu.
        // Visibility conditions of the "START MEAL 2" menu item:
        //      It's FW now, and
        //      The day was marked as OMAD, and
        //      Not more than 12 hours passed after MEAL 1 start.

        if (!cyc.currCycleExistsInDb) return false // it's the 1st opening of the app after install

        if (!AppState.fasting) return false // it's not FW now

        if (currCycle.meal2Start != null) return false // the day was NOT marked as OMAD

        val minutesAfterMeal1Start = dur.getEwMinutesUpToNow() // even thou it's FW now, we can use this function
        if (minutesAfterMeal1Start >= 12 * 60) return false // more than 12 hours passed after MEAL 1 start

        return true
    }
    /***********************************************************************************************************************/

    // ----------------------------------------------------------------------------------------------------------------------
    // FUNCTIONS RELATED TO APP STATE CANCELLATION:
    // ----------------------------------------------------------------------------------------------------------------------

    /***********************************************************************************************************************/
    private fun appStateCanBeCancelled(): Boolean {
        return when (true) {
            AppState.meal1, AppState.meal2 -> true // no limitation - a meal can be cancelled at any moment

            AppState.betweenMeals -> (dur.getMealMinutesUpToNow() < 60)

            AppState.fasting -> {
                // Call atLeastOneCycleExistsInDb() to prevent crash whe app open first time after install - dur.getMealMinutesUpToNow()
                // will call CycleController.getCurrCycle() through a calls chain, but no curr cycle exists yet:
                if (cyc.atLeastOneCycleExistsInDb())
                    (dur.getMealMinutesUpToNow() < 60)
                else
                    false
            }

            else -> throw Exception("None of AppState properties is true.")
        }
    } // appStateCanBeCancelled()
    /***********************************************************************************************************************/
    private fun cancelCurrAppState() {
        if (!appStateCanBeCancelled()) {
            // The menu was open when the action was allowed, but the action became prohibited while the menu was kept open:
            InfoMsg.show(title = "", msg = getString(R.string.msg__too_late), context = this)
            return
        }

        // Manage curr cycle in DB and in CycleController.currCycle; update AppState with the cycle which has just become curr:
        cyc.cancelCurrAppState()

        // Update chronometers start times (since they are counting now according to the app state which has just been cancelled):
        val mealChronometerStartLdt = when (true) {
            AppState.meal1        -> currCycle.meal1Start
            AppState.betweenMeals -> currCycle.betweenMealsStart
            AppState.meal2        -> currCycle.meal2Start
            else                  -> null // mealChronometer is invisible on fasting
        }
        if (mealChronometerStartLdt != null)
            start(mealChr, startLdt = mealChronometerStartLdt)

        val windowsChronometerStartLdt = if (AppState.fasting) currCycle.fastingStart else currCycle.meal1Start
        if (windowsChronometerStartLdt != null) // can be null on AppState.fasting if user cancelled the first MEAL1 after app install
            start(windowsChr, startLdt = windowsChronometerStartLdt)

        setGui()
    } // cancelCurrAppState()
    /***********************************************************************************************************************/

    // ----------------------------------------------------------------------------------------------------------------------
    // FUNCTIONS WHICH SETUP VISUAL APPEARANCE:
    // ----------------------------------------------------------------------------------------------------------------------

    /***********************************************************************************************************************/
    private fun setChrTextByItsBase(chr: Chronometer?) {
        // Sets displayed time according to SHOW_SECONDS pref, like "0:18" (if false) or "0:18:59" (if true).
        val timeMillis: Long = SystemClock.elapsedRealtime() - chr!!.base
        val h = (timeMillis / 3_600_000) % 24 // after 23:59:59 restart counting from 00:00:00 (days shown in windowsChronometerDays)
        var m = (timeMillis - h * 3_600_000) / 60000
        if (m >= 1440) m -= 1440
        val hh = h.toString()
        val mm = if (m < 10) "0$m" else m.toString()

        chr.text ="$hh:$mm"

//        var text = "$hh:$mm"
//
//        if (AppPrefs.getBoolean(PrefKey.SHOW_SECONDS, this)) {
//            val s = (timeMillis - h * 3_600_000 - m * 60_000) / 1000
//            val ss = if (s < 10) "0$s" else s.toString()
//            text += ":$ss"
//        }
//
//        chr.text = text
    } // setChronometerTextByItsBase()
    /***********************************************************************************************************************/
    private fun defineChrColor(): Int {
        return when (true) {
            AppState.meal1, AppState.meal2 -> if (dur.mealTooLong())               RED else GREEN

            AppState.betweenMeals -> if (
                dur.betweenMealsTooShort(minimumBetweenMealsHours = AppPrefs.getInt(PrefKey.MINIMUM_BETWEEN_MEALS_HOURS, this))
                ||
                !dur.enoughTimeToFinishMeal2Inside8HoursEw()
                                        )                                          RED else GREEN

            AppState.fasting -> if (dur.fastingTooShort(minimumFastingHours = 16)) RED else GREEN

            else -> throw Exception("None of AppState properties is 'true'.")
        }
    } // defineChronometersColor()
    /***********************************************************************************************************************/
    private fun setGuiMealChr() {
        mealChr.visibility = if (AppState.fasting) View.INVISIBLE else View.VISIBLE
        mealChrLabel.visibility = mealChr.visibility

        when (true) {
            AppState.betweenMeals -> {
                val minBetweenMealsHours = AppPrefs.getInt(PrefKey.MINIMUM_BETWEEN_MEALS_HOURS, this)
                if (minBetweenMealsHours == 0 /* don't monitor compliance with the minimum time interval between meals */) {
                    mealChr.visibility      = View.INVISIBLE
                    mealChrLabel.visibility = View.INVISIBLE
                    mealChronometerButton.text = getString(R.string.word__start_meal_2)
                    return
                }
            }
            AppState.fasting -> {
                mealChronometerButton.text = getString(R.string.word__start_meal_1) // ready to start new cycle
                return
            }
        }

        val labelR: Int?
        val buttonR: Int?

        when (true) {
            AppState.meal1 -> {
                labelR = R.string.word__meal_1
                buttonR = R.string.word__finish_meal_1
            }
            AppState.betweenMeals -> {
                labelR = R.string.word__after_meal_1
                buttonR = R.string.word__start_meal_2
            }
            AppState.meal2 -> {
                labelR = R.string.word__meal_2
                buttonR = R.string.word__finish_meal_2
            }
            AppState.fasting -> {
                labelR = null // just a fake value - the label is invisible on fasting
                buttonR = R.string.word__start_meal_1
            }
            else -> throw Exception("None of AppState properties is 'true'.")
        }

        if (labelR != null) {
            mealChrLabel.text = getString(labelR)
        }
        mealChronometerButton.text = getString(buttonR)

        val color = defineChrColor()
        mealChrBeginLabel.setTextColor(color)
        mealChrLabel.setTextColor(color)
        mealChrEndLabel.setTextColor(color)
        mealChr.setTextColor(color)
        mealPctLabel.setTextColor(color)
        mealWaitTimeLabel.setTextColor(color)
        mealDurationLabel.setTextColor(color)
        mealProgressBar.progressTintList = ColorStateList.valueOf(color)
    } // setGuiMealChronometer()
    /***********************************************************************************************************************/
    private fun setGuiMealChrBeginEndLabels() {
        mealChrBeginLabel.visibility = mealChr.visibility // displays from which moment meal chronometer is counting
        mealChrEndLabel.visibility = mealProgressBar.visibility // displays when ends period, visualized by meal progress bar
        if (mealChrBeginLabel.visibility == View.INVISIBLE && mealChrEndLabel.visibility == View.INVISIBLE) return

        mealChrBeginLabel.text = ""
        val stageBeginLdt = when (true) {
            AppState.meal1        -> currCycle.meal1Start!!
            AppState.betweenMeals -> currCycle.betweenMealsStart!!
            AppState.meal2        -> currCycle.meal2Start!!
            else                  -> throw Exception("This 'when' should not be reached on fasting stage.")
        }
        mealChrBeginLabel.text = " " + stageBeginLdt.toLocalTime().format(dateTimeFormatter) + " "

        if (mealChrEndLabel.visibility == View.VISIBLE) {
            mealChrEndLabel.text = ""
            val stageEndLdt =
                stageBeginLdt.plusSeconds(mealProgressBar.max.toLong()) // mealProgressBar.max contains seconds
            mealChrEndLabel.text = " " + stageEndLdt.toLocalTime().format(dateTimeFormatter) + " "
        }
    } // setGuiMealChronometerStartEndLabels()
    /***********************************************************************************************************************/
    @SuppressLint("SetTextI18n")
    private fun setGuiMealChrProgressBar() {
        mealProgressBar.visibility = if (AppState.fasting) View.INVISIBLE else View.VISIBLE
        mealPctLabel.visibility = mealProgressBar.visibility
        mealWaitTimeLabel.visibility = mealProgressBar.visibility
        mealDurationLabel.visibility = mealProgressBar.visibility
        if (mealProgressBar.visibility == View.INVISIBLE) return

        val maxSeconds: Int // to populate mealProgressBar.max
        val progressSeconds: Int // to populate mealProgressBar.progress

        when (true) {
            AppState.meal1, AppState.meal2 -> {
                val maxMealMinutes = AppPrefs.getInt(PrefKey.MAXIMUM_MEAL_MINUTES, this)
                maxSeconds = maxMealMinutes * 60
                progressSeconds = dur.getMealSecondsUpToNow()
                mealDurationLabel.text = maxMealMinutes.toString() + " " + resources.getQuantityString(R.plurals.word__minute, maxMealMinutes)
            }
            AppState.betweenMeals -> {
                val minBetweenMealsHours = AppPrefs.getInt(PrefKey.MINIMUM_BETWEEN_MEALS_HOURS, this)
                if (minBetweenMealsHours == 0 /* don't monitor compliance with the minimum time interval between meals */) {
                    mealProgressBar.visibility   = View.INVISIBLE
                    mealPctLabel.visibility      = View.INVISIBLE
                    mealWaitTimeLabel.visibility = View.INVISIBLE
                    mealDurationLabel.visibility = View.INVISIBLE
                    return
                }

                maxSeconds = minBetweenMealsHours * 60 * 60
                progressSeconds = dur.getBetweenMealsSecondsUpToNow()
                mealDurationLabel.text = minBetweenMealsHours.toString() + " " + resources.getQuantityString(R.plurals.word__hour, minBetweenMealsHours)
            }
            AppState.fasting -> {
                // Just fake values to prevent compiler from complaining - the progress bar is invisible on fasting,
                // so this section will never be reached:
                maxSeconds = 0
                progressSeconds = 0
                mealDurationLabel.text = ""
            }
            else -> throw Exception("setMealProgressBarAppearance(): None of AppState properties is 'true'.")
        }

        val pct = progressSeconds.toDouble() / maxSeconds.toDouble() * 100.0
        if (pct >= 100.0) {
            mealProgressBar.visibility   = View.INVISIBLE
            mealPctLabel.visibility      = View.INVISIBLE
            mealWaitTimeLabel.visibility = View.INVISIBLE
            mealDurationLabel.visibility = View.INVISIBLE
            return
        }

        mealProgressBar.max = maxSeconds
        mealProgressBar.progress = progressSeconds
        mealPctLabel.text = pct.toInt().toString() + "%"
        setGuiWaitTimeLabel(mealWaitTimeLabel, progressSeconds, maxSeconds)
    } // setGuiMealProgressBar()
    /***********************************************************************************************************************/
    private fun setGuiWindowsChr() {
        windowsChr.visibility =
            if (AppState.meal1 || (AppState.fasting && !cyc.atLeastOneCycleExistsInDb()))
                View.INVISIBLE else View.VISIBLE
        windowsChrLabel.visibility = getWindowsChrLabelVisibility() //  windowsChrLabel.visibility = windowsProgressBar.visibility

        windowsChrDDHHMM.visibility = View.INVISIBLE
        windowsChrDays.text = ""
        if (windowsChrLabel.visibility == View.INVISIBLE) return

        val r = if (AppState.fasting) R.string.word__fw else R.string.word__ew
        windowsChrLabel.text = getString(r)

        val color = defineChrColor()
        windowsChrBeginLabel.setTextColor(color)
        windowsChrLabel.setTextColor(color)
        windowsChrEndLabel.setTextColor(color)
        windowsChr.setTextColor(color)
        windowsPctLabel.setTextColor(color)
        windowWaitTimeLabel.setTextColor(color)
        windowsDurationLabel.setTextColor(color)
        windowsProgressBar.progressTintList = ColorStateList.valueOf(color)

        // When fasting achieves 24 hours, windowsChronometer is reset to zeros and starts to count from "00"00".
        // In that situation, let's display "1 day +", "2 days +" etc. in windowsChronometerDays:
        if (AppState.fasting) {
            // Prevent crash when user cancels the first MEAL 1 after app install -
            // getFastingDurationUpToNow() will access curr cycle which doesn't exist yet:
            if (!cyc.atLeastOneCycleExistsInDb()) {
                return // in fact, FASTING WINDOW timer is not shown now
            }

            val fastingMinutesUpToNow = dur.getFastingMinutesUpToNow()
            if (fastingMinutesUpToNow < TWENTY_FOUR_HOURS_AS_MINUTES) {
                windowsChrDays.text = ""
            } else {
                val fastingDaysUpToNow = TimeUnit.MINUTES.toDays(fastingMinutesUpToNow.toLong()).toInt()
                windowsChrDays.text = fastingDaysUpToNow.toString() + " " + resources.getQuantityString(R.plurals.word__day, fastingDaysUpToNow) + " + "
                // Later, startMeal1() will set windowsChronometerDays.text to empty string

                // windowsChrDays & windowsChr show the fasting time as days & time (like 1 day + 02:15).
                // Let's also show the fasting time as time duration (like 26 h 15 m).
                // We can reuse windowWaitTimeLabel do display that, since it's not in use now for its main purpose.
                windowsChrDDHHMM.visibility = View.VISIBLE
                val fastingDuration = dur.getFastingDurationUpToNow()
                windowsChrDDHHMM.text = dur.stringFromDurationNoDays(fastingDuration)
            }
        }
    } // setGuiWindowsChronometer()
    /***********************************************************************************************************************/
    private fun setGuiWindowsChrBeginEndLabels() {
        windowsChrBeginLabel.visibility = getWindowsChrLabelVisibility()
        if (windowsChrDays.text != "") windowsChrBeginLabel.visibility = View.INVISIBLE // hide if fasting longer than 1 day
        windowsChrEndLabel.visibility = windowsProgressBar.visibility // displays when ends period, visualized by window progress bar
        if (windowsChrBeginLabel.visibility == View.INVISIBLE && windowsChrEndLabel.visibility == View.INVISIBLE) return

        windowsChrBeginLabel.text = ""
        val stageBeginLdt = if (AppState.fasting) currCycle.fastingStart!! else currCycle.meal1Start!!
        windowsChrBeginLabel.text = " " + stageBeginLdt.toLocalTime().format(dateTimeFormatter) + " "

        if (windowsChrEndLabel.visibility == View.VISIBLE) {
            windowsChrEndLabel.text = ""
            if (!AppState.meal2) {
                val stageEndLdt =
                    stageBeginLdt.plusMinutes(windowsProgressBar.max.toLong()) // windowsProgressBar.max contains minutes
                windowsChrEndLabel.text = " " + stageEndLdt.toLocalTime().format(dateTimeFormatter) + " "
            }
        }
    } // setGuiWindowsChronometerStartEndLabels()
    /***********************************************************************************************************************/
    @SuppressLint("SetTextI18n")
    private fun setGuiWindowsChrProgressBar() {
        var maxMinutes = 0 // to populate windowsProgressBar.max
        var progressMinutes = 0 // to populate windowsProgressBar.progress
        var extendTo8 = false

        windowsProgressBar.visibility =
            if (
                    AppState.meal2 // there is nothing to plan today since user has already started MEAL 2
                    ||
                    (AppState.fasting && !cyc.atLeastOneCycleExistsInDb())
                ) View.INVISIBLE
            else
                View.VISIBLE

        windowsPctLabel.visibility = windowsProgressBar.visibility
        windowWaitTimeLabel.visibility = windowsProgressBar.visibility
        windowsDurationLabel.visibility = windowsProgressBar.visibility
        if (windowsProgressBar.visibility == View.INVISIBLE) return

        when (true) {
            AppState.fasting && cyc.atLeastOneCycleExistsInDb() -> {
                // Display FASTING window progress:
                maxMinutes = SIXTEEN_HOURS_AS_MINUTES
                progressMinutes = dur.getFastingMinutesUpToNow()
                windowsDurationLabel.text = "16 " + resources.getQuantityString(R.plurals.word__hour, 16)
            }

            AppState.meal1, AppState.betweenMeals, AppState.meal2 -> {
                // Display EATING window progress:
                val maxEwHours = AppPrefs.getInt(PrefKey.MAXIMUM_EW_HOURS, this)
                var finalMaxEwHours = maxEwHours.toString()
                maxMinutes = maxEwHours * 60
                progressMinutes = dur.getEwMinutesUpToNow()

                if (AppState.betweenMeals) {
                    val maxMealMinutes = AppPrefs.getInt(PrefKey.MAXIMUM_MEAL_MINUTES, this)
                    extendTo8 = (maxEwHours < 8 && progressMinutes >= (maxMinutes - maxMealMinutes))
                    if (extendTo8) {
                        // There is not enough time to compete the meal staying in the 4-7 hours EW,
                        // or that EW is over at all. Let's make EW timer jump to 8 hours, so user will see how much time
                        // he has not to exceed the maximum allowed 8-hours EW:
                        finalMaxEwHours = "8"
                        maxMinutes = EIGHT_HOURS_AS_MINUTES
                        // That 8-h EW will also be reflected in bottom message -
                        // see MainActivityMsgController.generateBottomMsgBetweenMealsGreen().
                    }
                }
                windowsDurationLabel.text =
                    if (!extendTo8)
                        finalMaxEwHours + " " + resources.getQuantityString(R.plurals.word__hour, maxEwHours)
                    else
                        // "%1$s h (extended from %2$s h)"
                        getString(R.string.windows_duration_label_text_extended, finalMaxEwHours, maxEwHours.toString())
            } // AppState.betweenMeals, AppState.meal2
        } // when (true)

        val pct = progressMinutes.toDouble() / maxMinutes.toDouble() * 100.0
        if (pct >= 100.0) {
            windowsProgressBar.visibility   = View.INVISIBLE
            windowsPctLabel.visibility      = View.INVISIBLE
            windowWaitTimeLabel.visibility  = View.INVISIBLE
            windowsDurationLabel.visibility = View.INVISIBLE
            return
        }

        windowsProgressBar.max = maxMinutes
        windowsProgressBar.progress = progressMinutes
        windowsPctLabel.text = pct.toInt().toString() + "%"
        setGuiWaitTimeLabel(windowWaitTimeLabel, progressSeconds = progressMinutes * 60, maxSeconds = maxMinutes * 60)
    } // setGuiWindowsProgressBar()
    /***********************************************************************************************************************/
    @SuppressLint("SetTextI18n")
    private fun setGuiWaitTimeLabel(waitTimeLabel: TextView, progressSeconds: Int, maxSeconds: Int) {
        val stageStartLdt = when (waitTimeLabel.id) {
            mealWaitTimeLabel.id -> when (true) {
                                        AppState.meal1        -> currCycle.meal1Start!!
                                        AppState.betweenMeals -> currCycle.betweenMealsStart!!
                                        AppState.meal2        -> currCycle.meal2Start!!
                                        else                  -> throw Exception("The func should not be called for mealWaitTimeLabel on fasting.")
                                    }
            windowWaitTimeLabel.id -> if (AppState.fasting) currCycle.fastingStart!! else currCycle.meal1Start!!
            else -> throw Exception("waitTimeLabel arg can point only to mealWaitTimeLabel or windowWaitTimeLabel.")
        }

        val stageEndLdt = stageStartLdt.plusSeconds(maxSeconds.toLong())
        var progressLdt = stageStartLdt.plusSeconds(progressSeconds.toLong())
        progressLdt = progressLdt.plusMinutes(-1).plusSeconds(1)
        waitTimeLabel.text = "-" + dur.stringFromDuration(Duration.between(progressLdt, stageEndLdt))
    } // setWaitTimeLabel()
    /***********************************************************************************************************************/
    private fun setGuiBottomMsg() {
        bottomMsgTextView.text = msg.generateBottomMsg(
            oldBottomMsg = bottomMsgTextView.text.toString(),
            maxHoursInWindowsProgressBar = (windowsProgressBar.max / 60),
            timersColor = (if(mealChr.visibility == View.VISIBLE) mealChr else windowsChr).currentTextColor
        )
    } // generateBottomMsg()
    /***********************************************************************************************************************/
    private fun setGui() {
        setGuiMealChr()
        setGuiMealChrProgressBar()
        setGuiMealChrBeginEndLabels()
        setGuiWindowsChr()
        setGuiWindowsChrProgressBar()
        setGuiWindowsChrBeginEndLabels()
        setGuiBottomMsg()
    } // setGuiBottomMsg()
    /***********************************************************************************************************************/
    private fun getWindowsChrLabelVisibility(): Int {
        val visible = when (true) {
            AppState.meal1 ->
                (windowsProgressBar.visibility == View.VISIBLE) // windowsProgressBar (but not windowsChr) is visible during MEAL 1
            AppState.betweenMeals ->
                true
            AppState.meal2 ->
                (windowsChr.visibility == View.VISIBLE) // windowsChr (but not windowsProgressBar) is visible during MEAL 2
            AppState.fasting ->
                cyc.atLeastOneCycleExistsInDb()
            else ->
                throw Exception("getWindowsChrLabelVisibility(): None of AppState properties is 'true'.")
        }

        return if (visible) View.VISIBLE else View.INVISIBLE
    } // getWindowsChrLabelVisibility()
    /***********************************************************************************************************************/

    // ----------------------------------------------------------------------------------------------------------------------
    // FUNCTIONS RELATED TO MENU:
    // ----------------------------------------------------------------------------------------------------------------------

    /***********************************************************************************************************************/
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var url: String? = null
        val openLinksInRussian = easternSlavicLangExistsInDevice()

        when (item?.itemId) {
            R.id.cancel_curr_app_state -> {
                InfoMsg.modalDialog(
                    title = item.title.toString(),
                    msg = "",
                    funcToCallOnPositiveResponse = { cancelCurrAppState() },
                    context = this
                )
            }

            R.id.start_meal_2_when_day_marked_as_omad -> {
                InfoMsg.modalDialog(
                    title = item.title.toString(),
                    msg = getString(R.string.msg__ew_is_over),
                    funcToCallOnPositiveResponse = { startMeal2WhenDayMarkedAsOmad() },
                    context = this
                )
            }

            R.id.open_stats_activity -> {
                if (cyc.atLeastOneCycleExistsForStats()) {
                    startActivity(Intent(this, StatsActivity::class.java))
                } else {
                    InfoMsg.show(title = "", msg = getString(R.string.msg__no_stats_exist), context = this)
                }
            }

            R.id.open_pref_activity         -> startActivity(Intent(this, PrefActivity::class.java))

            R.id.open_home_page             -> url = if (openLinksInRussian) "http://intfast.ca/периодическое-голодание-таймер/"
                                                                        else "http://intfast.ca/intermittent-fasting-timer-app-android/"
            R.id.open_how_it_works          -> url = if (openLinksInRussian) "http://forumy.ca/viewtopic.php?t=6511"
                                                                        else "http://intfast.ca/how-intermittent-fasting-works/"
            R.id.open_practical_instruction -> url = if (openLinksInRussian) "http://forumy.ca/viewtopic.php?t=6513"
                                                                        else "http://intfast.ca/instruction-how-to-start-intermittent-fasting/"
            R.id.open_fb_group              -> url = if (openLinksInRussian) "https://www.facebook.com/groups/PeriodicheskoeGolodanie/"
                                                                        else "https://www.facebook.com/groups/IntFast.ca/"

            R.id.rate_app                   -> url = "market://details?id=$packageName" // change for app stores other than Google Play

            R.id.share                      -> share(subject = getString(R.string.word__share_subject),
                                                         msg = getString(R.string.word__share_msg), context = this)
            R.id.copyright                  -> msg.msgCopyright()

//            R.id.open_db_spy_activity       -> startActivity(Intent(this, DbSpyActivity::class.java)) // todo: make sure it's commented out
        }

        if (url != null) startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

        return super.onOptionsItemSelected(item!!)
    } // onOptionsItemSelected
    /***********************************************************************************************************************/
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val startMeal2WhenDayMarkedAsOmadMenuItem = menu!!.findItem(R.id.start_meal_2_when_day_marked_as_omad)!!
        startMeal2WhenDayMarkedAsOmadMenuItem.isVisible = canStartMeal2WhenDayMarkedAsOmad()

        onPrepareOptionsMenuCancelCurrAppState(menu!!)

        return super.onPrepareOptionsMenu(menu)
    } // onPrepareOptionsMenu()
    /***********************************************************************************************************************/
    private fun onPrepareOptionsMenuCancelCurrAppState(menu: Menu) {
        // Extract from onPrepareOptionsMenu() dealing with cancelCurrAppStateMenuItem

        val cancelCurrAppStateMenuItem = menu.findItem(R.id.cancel_curr_app_state)!!

        cancelCurrAppStateMenuItem.isVisible = appStateCanBeCancelled()
        if (!cancelCurrAppStateMenuItem.isVisible) return

        val titleR: Int
        val iconR: Int

        when (true) {
            AppState.meal1, AppState.meal2 -> {
                titleR = R.string.menu__cancel_meal
                iconR = android.R.drawable.ic_menu_close_clear_cancel
            }
            AppState.betweenMeals, AppState.fasting -> {
                titleR = R.string.menu__resume_meal
                iconR = android.R.drawable.ic_menu_revert
            }
            else -> throw Exception("None of AppState properties is 'true'.")
        }

        cancelCurrAppStateMenuItem.title = getString(titleR)
        cancelCurrAppStateMenuItem.icon = getDrawable(iconR)
    }
    /***********************************************************************************************************************/
    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        (menu as MenuBuilder).setOptionalIconsVisible(true)
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)
        return super.onCreateOptionsMenu(menu)
    } // onCreateOptionsMenu()
    /***********************************************************************************************************************/
    //    override fun onPause() {
    //        dbgMsg("CALLED", this::class.simpleName, "onPause()")
    //        toast(this, "onPause: " + AppState.curr)
    //        super.onPause()
    //    }
    /***********************************************************************************************************************/
    //    override fun onStop() {
    //        dbgMsg("CALLED", this::class.simpleName, "onStop()")
    //        toast(this, "onStop: " + AppState.curr)
    //        super.onStop()
    //    }
    /***********************************************************************************************************************/
    //    override fun onStart() {
    //        super.onStart()
    //        dbgMsg("CALLED", this::class.simpleName, "onStart()")
    //        toast(this, "onStart: " + AppState.curr)
    //    }
    /***********************************************************************************************************************/
    //    override fun onRestart() {
    //        super.onRestart()
    //        dbgMsg("CALLED", this::class.simpleName, "onRestart()")
    //        toast(this, "onRestart: " + AppState.curr)
    //    }
    /***********************************************************************************************************************/
}
