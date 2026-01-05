package ca.intfast.iftimer.cycle

import android.content.Context
import ca.intfast.iftimer.R
import ca.intfast.iftimer.appwide.AppState
import ca.intfast.iftimer.appwide.DurationController
import ca.intfast.iftimer.appwide.PrefKey
import ca.intfast.iftimer.db.DbColumn
import ca.intfast.iftimer.db.DbTable
import ca.intfast.iftimer.util.CustomAppCompatActivity
import ca.intfast.iftimer.util.CrudHelper
import ca.intfast.iftimer.util.InfoMsg
import java.time.LocalDateTime

class CycleController(private val context: Context): CrudHelper(context) {
    private lateinit var currCycle: Cycle
    private var currCycleExistsInDb: Boolean = true // false if startMeal1(), which INSERTs cycles, was never called after app install;
                                                    // populated in loadCurrCycleFromDb()
    val currCycleSavedInDb get() = currCycleExistsInDb

    fun getCurrCycle(): Cycle {
        if (!currCycleExistsInDb) throw Exception("CycleController.getCurrCycle(): currCycle doesn't exist now.")
        return currCycle
    } // getCurrCycle()

//    fun getCurrAppStateStartLdt(): LocalDateTime {
//        return when (true) {
//            AppState.meal1 -> currCycle.meal1Start!!
//            AppState.betweenMeals -> currCycle.betweenMealsStart!!
//            AppState.meal2 -> currCycle.meal2Start!!
//            AppState.fasting -> currCycle.fastingStart!!
//            else -> throw Exception("CycleController.getCurrAppStateStartLdt(): Invalid AppState ${AppState.curr}.")
//        }
//    } // getCurrAppStateStartLdt()

    fun loadCurrCycleFromDb() {
        val retrievedCurrCycle = retrieveRecord<Cycle>(DbTable.CYCLE, whereClause = "fastingFinish IS NULL", required = false)
        currCycleExistsInDb = (retrievedCurrCycle != null)
        if (currCycleExistsInDb) currCycle = retrievedCurrCycle!! // If currCycleExistsInDb = false, currCycle will be populated in startMeal1()
        setAppStateByCurrCycle()
    } // loadCurrCycleFromDb()

    fun atLeastOneCycleExistsForStats() = exists(DbTable.CYCLE,  whereClause = "fastingStart IS NOT NULL")

    fun atLeastOneCycleExistsInDb() = exists(DbTable.CYCLE)

    private fun setAppStateByCurrCycle() {
        if (!currCycleExistsInDb /* it doesn't exist if user never started meal 1 after app install */) {
            AppState.fasting = true // since meal 1 has not started yet, we treat this time as fasting
            return
        }

        val currCycleIsMarkedAsOmad = (currCycle.meal2Start == null && currCycle.fastingStart != null)
        if (currCycleIsMarkedAsOmad) {
            AppState.fasting = true // currently, user is ready to start new cycle by starting meal 1
            return
        }

        when (null) {
            currCycle.betweenMealsStart -> AppState.meal1        = true
            currCycle.meal2Start        -> AppState.betweenMeals = true
            currCycle.fastingStart      -> AppState.meal2        = true
            currCycle.fastingFinish     -> AppState.fasting      = true
            currCycle.meal1Start        -> throw Exception("CycleController.setCurrAppStateByCurrCycle(): meal1Start cannot be null.")
            else                        -> throw Exception("CycleController.setCurrAppStateByCurrCycle(): failed to set by currCycle: $currCycle")
        }
    } // setAppStateByCurrCycle()

    private fun retrievePrevCycle(): Cycle? {
        if (!currCycleExistsInDb)
        // User opened app just after install - currCycle property, which is required for the next WHERE clause,
        // will be initialized only in startMeal1(). Obviously, no prev cycle exists in this situation:
            return null

        val whereClause = StringBuffer("${DbColumn.ID} = (SELECT MAX(${DbColumn.ID}) FROM ${DbTable.CYCLE} prev")

        if (currCycle.id!! > 0) {
            whereClause.append(" WHERE prev.${DbColumn.ID} < ${currCycle.id}")
            // If currCycle.id is 0, the Cycle with Max _id in the table is, in fact, the previous one.
        }

        whereClause.append(")")

        return retrieveRecord(DbTable.CYCLE, whereClause.toString(), required = false) // can be null if it's the first cycle after app install
    } // retrievePrevCycle()

    fun deleteStats() {
        val whereClause = if (AppState.fasting) {
            "" // delete all cycles
        } else {
            loadCurrCycleFromDb() // initialize the lateinit currCycle
            "${DbColumn.ID} < ${currCycle.id}" // delete all except curr cycle
        }
        this.writableDatabase.delete(DbTable.CYCLE, whereClause, null)
    } // deleteStats()

//    fun deletePrevCycle() {
//        writableDatabase.delete(DbTable.CYCLE, "fastingStart IS NOT NULL", null)
//
//        // The before-previous cycle (if existed) has just become the new previous. We need to do some changes in it.
//        val newPrevCycle = retrievePrevCycle() // with the largest id which is smaller than currCycle.id
//        if (newPrevCycle != null) {
//            newPrevCycle.fastingFinish = if (AppState.fasting) null else currCycle.meal1Start
//            // todo: on AppState.fasting, test to ensure that the fasting timer is now counting from newPrevCycle.fastingStart
//        }
//        // todo: in the cycle, which just became Prev (if exists), make fastingStart = null
//    } // deletePrevCycle()

    fun retrieveCycleList(): ArrayList<Cycle> { // for DbSpyActivity
        return retrieveList(tableName = DbTable.CYCLE, orderByClause = DbColumn.ID + " DESC")
    } // retrieveCycleList()

    fun makeCurrCycleOmad() {
        currCycle.meal2Start = null // it can contain a value if user started meal 2, but abandoned and didn't finish it
        currCycle.fastingStart = currCycle.betweenMealsStart // start fasting window when user finished meal 1
        update(currCycle)
        AppState.fasting = true
        CustomAppCompatActivity.put(PrefKey.LAST_APP_STATE_SET_BY_USER, AppState.FASTING, context) // for MainActivity.onResumeCheckInactivity()
    } // makeCurrCycleOmad()

//    fun getStageStartLdt(): LocalDateTime {
//        return when (true) {
//            AppState.meal1        -> currCycle.meal1Start!!
//            AppState.betweenMeals -> currCycle.betweenMealsStart!!
//            AppState.meal2        -> currCycle.meal2Start!!
//            AppState.fasting      -> currCycle.fastingStart!!
//            else                  -> throw Exception("None of AppState properties is true.")
//        }
//    }
//
//    fun cancelOmad() { // rolls back the action of makeCurrCycleOmad()
//        currCycle.fastingStart = null // it was previously populated in makeCurrCycleOmad()
//        update(currCycle)
//        AppState.betweenMeals = true
//        AppPrefs.put(PrefKey.LAST_APP_STATE_SET_BY_USER, AppState.BETWEEN_MEALS, context) // for MainActivity.onResumeCheckInactivity()
//    } // cancelOmad()


    // ----------------------------------------------------------------------------------------------------------------------
    // FUNCTIONS CALLED WHEN USER CLICKS "START / FINISH MEAL" BUTTON:
    // ----------------------------------------------------------------------------------------------------------------------


    fun startMeal1() {
        val now = LocalDateTime.now()

        // First of all, finish (archive) the cycle which was current up to now:
        if (currCycleExistsInDb /* it doesn't exist if user starts the first meal 1 after app install */) {
            currCycle.fastingFinish = now // not null in that field also flags, that the cycle is inactive (archived)
            update(currCycle)
        }

        currCycle = Cycle() // starting meal 1 starts a new cycle, which becomes the current instead of the just finished
        currCycle.meal1Start = now
        insert(currCycle) // that also populates currCycle.id with the just generated autoincremented id
        if (currCycle.id == null) throw Exception("CycleController.startMeal1(): currCycle.id was not generated.")
        currCycleExistsInDb = true

        AppState.meal1 = true
        CustomAppCompatActivity.put(PrefKey.LAST_APP_STATE_SET_BY_USER, AppState.MEAL_1, context) // for MainActivity.onResumeCheckInactivity()
    } // startMeal1()

    fun startBetweenMeals(since: LocalDateTime? = null) { // when user clicks FINISH MEAL 1
        currCycle.betweenMealsStart = since ?: LocalDateTime.now()
        update(currCycle)
        AppState.betweenMeals = true
        CustomAppCompatActivity.put(PrefKey.LAST_APP_STATE_SET_BY_USER, AppState.BETWEEN_MEALS, context) // for MainActivity.onResumeCheckInactivity()
    } // startBetweenMeals()

    fun startMeal2() {
        currCycle.meal2Start = LocalDateTime.now()
        update(currCycle)
        AppState.meal2 = true
        CustomAppCompatActivity.put(PrefKey.LAST_APP_STATE_SET_BY_USER, AppState.MEAL_2, context) // for MainActivity.onResumeCheckInactivity()
    } // startMeal2()

    fun startFasting(since: LocalDateTime? = null) { // when user clicks FINISH MEAL
        currCycle.fastingStart = since ?: LocalDateTime.now()
        update(currCycle)
        AppState.fasting = true
        CustomAppCompatActivity.put(PrefKey.LAST_APP_STATE_SET_BY_USER, AppState.FASTING, context) // for MainActivity.onResumeCheckInactivity()
    } // startFasting()

    // ----------------------------------------------------------------------------------------------------------------------
    // FUNCTIONS WHICH CANCEL APP STATES:
    // ----------------------------------------------------------------------------------------------------------------------

    fun cancelCurrAppState() {
        when (true) {
            AppState.meal1        -> cancelMeal1()
            AppState.betweenMeals -> cancelBetweenMeals()
            AppState.meal2        -> cancelMeal2()
            AppState.fasting      -> cancelFasting()
            else                  -> throw Exception("None of AppState properties is true.")
        }
    }

    private fun cancelMeal1() {
        val prevCycle = retrievePrevCycle() // with the largest id which is smaller than currCycle.id

        delete(currCycle)

        // Make prev cycle curr cycle:
        if (prevCycle != null) {
            prevCycle.fastingFinish = null // that will flag it as curr, so loadCurrCycleFromDb() will retrieve it in the future
            update(prevCycle)
            currCycle = prevCycle
        }

        AppState.fasting = true
        CustomAppCompatActivity.put(PrefKey.LAST_APP_STATE_SET_BY_USER, AppState.FASTING, context) // for MainActivity.onResumeCheckInactivity()
    } // cancelMeal1()

    private fun cancelBetweenMeals() {
        currCycle.betweenMealsStart = null
        update(currCycle)
        AppState.meal1 = true
        CustomAppCompatActivity.put(PrefKey.LAST_APP_STATE_SET_BY_USER, AppState.MEAL_1, context) // for MainActivity.onResumeCheckInactivity()
    } // cancelBetweenMeals()

    private fun cancelMeal2() {
        val dur = DurationController(this, context)
        if (dur.getEwMinutesUpToNow() < 8 * 60) {
            currCycle.meal2Start = null
            startBetweenMeals(since = currCycle.betweenMealsStart) // with the same betweenMealsStart, i.e. don't change it
        } else {
            makeCurrCycleOmad()
            InfoMsg.show(
                title = context.getString(R.string.msg__curr_day_marked_as_omad__title),
                msg = context.getString(R.string.msg__curr_day_marked_as_omad),
                context = context)
        }
    } // cancelMeal2()

    private fun cancelFasting() {
        currCycle.fastingStart = null
        update(currCycle)
        AppState.meal2 = true
        CustomAppCompatActivity.put(PrefKey.LAST_APP_STATE_SET_BY_USER, AppState.MEAL_2, context) // for MainActivity.onResumeCheckInactivity()
    } // cancelFasting()

    // ----------------------------------------------------------------------------------------------------------------------
    // Properties used by DurationController to define if too short/long time passed after particular events:
    // ----------------------------------------------------------------------------------------------------------------------

    val lastMealStart: LocalDateTime
    get() = if (currCycle.meal2Start != null) currCycle.meal2Start!! else currCycle.meal1Start!!

    val lastMealFinish: LocalDateTime? // null when AppState = MEAL_1 in in the first cycle after app install
    get() {
        // Can be called from DurationController.afterLastMealFinish(), which is called from fastingTooShort(),
        // which is called from MainActivity.startMeal1() when user clicks START first time after app install.
        // In this case, currCycle is not instantiated yet since startMeal1(), which instantiates it, has not been called
        // yet. In that situation, let's return a fake last meal finish time which is 16 hours ago. That will cause
        // fastingTooShort() to return false, which will prevent the message, shown in MainActivity.startMeal1():
        if (!currCycleExistsInDb /* it doesn't exist if user never started meal 1 after app install */)
            return LocalDateTime.now().plusHours(-16)

        if (currCycle.fastingStart != null) return currCycle.fastingStart!! // finish time of meal 2; if OMAD, it's finish time of meal 1
        if (currCycle.betweenMealsStart != null) return currCycle.betweenMealsStart!! // finish time of meal 1

        // None of the current cycle meals is finished yet. That means, that AppState is now MEAL_1.
        // So, we need to define when the last meal of the PREVIOUS cycle finished.
        val prevCycle = retrievePrevCycle()
        return prevCycle?.fastingStart
    }

    val ewStart: LocalDateTime
    get() = currCycle.meal1Start!! // cannot be null - populated when a new cycle is being created

    val ewFinish: LocalDateTime
    get() {
        if (!AppState.fasting) throw Exception("CycleController.ewFinish should be called only on fasting, not on ${AppState.curr}.")
        return currCycle.fastingStart!!
    }

    val betweenMealsStart: LocalDateTime
    get() {
        if (!AppState.betweenMeals) throw
        Exception("CycleController.betweenMealsStart should be called only between meals, not on ${AppState.curr}.")
        return currCycle.betweenMealsStart!!
    }
}