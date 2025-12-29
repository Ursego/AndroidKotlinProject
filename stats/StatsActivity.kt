package ca.intfast.iftimer.stats

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import ca.intfast.iftimer.R
import ca.intfast.iftimer.appwide.DurationController
import ca.intfast.iftimer.cycle.CycleController
import ca.intfast.iftimer.databinding.ActivityStatsBinding
import ca.intfast.iftimer.db.DbColumn
import ca.intfast.iftimer.db.DbTable
import ca.intfast.iftimer.util.CrudHelper
import ca.intfast.iftimer.util.InfoMsg
import kotlin.math.roundToInt

class StatsActivity: AppCompatActivity() {
    private lateinit var b: ActivityStatsBinding // "b"inding
    private val cyc = CycleController(this)
    private val crudHelper = CrudHelper(this)
    /***********************************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(b.root)

        setTitle(R.string.word__stats)

        populate()
        deleteOld()

        // Display "back" icon (left arrow) on the menu bar:
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    } // onCreate()
    /***********************************************************************************************************************/
//    private fun deletePrevCycle() {
//        cyc.deletePrevCycle()
//
//        if (cyc.atLeastOneCycleExistsForStats()) {
//            populate() // refresh screen to reflect the change
//        } else {
//            finish()
//        }
//    } // deletePrevCycle()
    /***********************************************************************************************************************/
    private fun deleteStats() {
        cyc.deleteStats()
        finish()
    } // deleteStats()
    /***********************************************************************************************************************/
    private fun populate() {
        val dur = DurationController(cyc, this)

        // PREVIOUS CYCLE:
        var s = retrieveStats("1") // average for one cycle means the cycle itself
        b.prevMeal1.text = if (s.avgMeal1!! > 0) dur.stringFromMinutes(s.avgMeal1!!) else "---"
        b.prevMeal2.text = if (s.avgMeal2!! > 0) dur.stringFromMinutes(s.avgMeal2!!) else "---"
        b.prevEw.text = if (s.avgEw!! > 0) dur.stringFromMinutes(s.avgEw!!) else "---"

        // LAST 7 CYCLES:
        s = retrieveStats("7")
        b.lastCyclesTitle7.text = getString(R.string.stats__last_n_cycles, "7")
        b.avgMeal7.text = if (s.avgMeal!! > 0) dur.stringFromMinutes(s.avgMeal!!) else "---"
        b.avgEw7.text = if (s.avgEw!! > 0) dur.stringFromMinutes(s.avgEw!!) else "---"
        b.omadsNum7.text = if (s.omadsCount!! > 0) "${s.omadsCount} (${s.omadsPct}%)" else "---"

        // LAST 30 CYCLES:
        s = retrieveStats("30")
        b.lastCyclesTitle30.text = getString(R.string.stats__last_n_cycles, "30")
        b.avgMeal30.text = if (s.avgMeal!! > 0) dur.stringFromMinutes(s.avgMeal!!) else "---"
        b.avgEw30.text = if (s.avgEw!! > 0) dur.stringFromMinutes(s.avgEw!!) else "---"
        b.omadsNum30.text = if (s.omadsCount!! > 0) "${s.omadsCount} (${s.omadsPct}%)" else "---"

        // LAST 365 CYCLES:
        s = retrieveStats("365")
        b.lastCyclesTitle365.text = getString(R.string.stats__last_n_cycles, "365")
        b.avgMeal365.text = if (s.avgMeal!! > 0) dur.stringFromMinutes(s.avgMeal!!) else "---"
        b.avgEw365.text = if (s.avgEw!! > 0) dur.stringFromMinutes(s.avgEw!!) else "---"
        b.omadsNum365.text = if (s.omadsCount!! > 0) "${s.omadsCount} (${s.omadsPct}%)" else "---"

        // OMADs:
        b.omadsNumTitle7.text = getString(R.string.stats__cycles, "7")
        b.omadsNumTitle30.text = getString(R.string.stats__cycles, "30")
        b.omadsNumTitle365.text = getString(R.string.stats__cycles, "365")
    } // populate()
    /***********************************************************************************************************************/
    private fun retrieveStats(rowsLimit: String): Stats {
        val sqlSelect = "SELECT " +
            "ROUND(AVG((STRFTIME('%s', ${DbColumn.BETWEEN_MEALS_START}) - STRFTIME('%s', ${DbColumn.MEAL_1_START})) / 60)) AS ${DbColumn.AVG_MEAL_1}, " +
            "ROUND(AVG((STRFTIME('%s', ${DbColumn.MEAL_2_START}) - STRFTIME('%s', ${DbColumn.BETWEEN_MEALS_START})) / 60)) AS ${DbColumn.AVG_BETWEEN_MEALS}, " +
            "ROUND(AVG((STRFTIME('%s', ${DbColumn.FASTING_START}) - STRFTIME('%s', ${DbColumn.MEAL_2_START})) / 60)) AS ${DbColumn.AVG_MEAL_2}, " +
            "ROUND(AVG((STRFTIME('%s', ${DbColumn.FASTING_START}) - STRFTIME('%s', ${DbColumn.MEAL_1_START})) / 60)) AS ${DbColumn.AVG_EW}, " +
            "COUNT(1) AS ${DbColumn.MEAL_1_COUNT}, " + // can also be used as total cycles count
            "(" +
                "SELECT COUNT(1) " +
                  "FROM ${DbTable.CYCLE} " +
                 "WHERE ${DbColumn.ID} IN (SELECT ${DbColumn.ID} " + // limit by the same condition as the overall query...
                                                                            "FROM ${DbTable.CYCLE} " +
                                                                         "WHERE ${DbColumn.FASTING_START} IS NOT NULL " +
                                                                   "ORDER BY ${DbColumn.ID} DESC " +
                                                                             "LIMIT $rowsLimit) " +
                   "AND ${DbColumn.ID} IN (SELECT ${DbColumn.ID} " + // ...and exclude OMADs from that population
                                                                        "FROM ${DbTable.CYCLE} " +
                                                                     "WHERE ${DbColumn.MEAL_2_START} IS NOT NULL)" +
            ") AS ${DbColumn.MEAL_2_COUNT} " +
            "FROM ${DbTable.CYCLE} " +
            "WHERE ${DbColumn.ID} IN (SELECT ${DbColumn.ID} " +
                                                                       "FROM ${DbTable.CYCLE} " +
                                                                    "WHERE ${DbColumn.FASTING_START} IS NOT NULL " +
                                                              "ORDER BY ${DbColumn.ID} DESC " +
                                                                        "LIMIT $rowsLimit)"

        val stats = crudHelper.retrieveOne<Stats>(sqlSelect, required = true)!!

        val avgMealTemp = (
                        (stats.avgMeal1!! * stats.meal1Count!! + stats.avgMeal2!! * stats.meal2Count!!).toDouble()
                        /
                        (stats.meal1Count!! + stats.meal2Count!!).toDouble()
                    )
        stats.avgMeal = if (avgMealTemp > 0) avgMealTemp.roundToInt() else 0
        stats.omadsCount = stats.meal1Count!! - stats.meal2Count!!
        val omadsPctTemp = (stats.omadsCount!!.toDouble() / stats.meal1Count!!.toDouble() * 100)
        stats.omadsPct = if (omadsPctTemp > 0) omadsPctTemp.roundToInt() else 0

        return stats
    } // retrieveStats()
    /***********************************************************************************************************************/
    private fun deleteOld() {
        val whereClause = "${DbColumn.ID} NOT IN (" +
                                                        "SELECT ${DbColumn.ID} " +
                                                          "FROM ${DbTable.CYCLE} " +
                                                         "ORDER BY ${DbColumn.ID} DESC " +
                                                         "LIMIT 365)"
        crudHelper.writableDatabase.delete(DbTable.CYCLE, whereClause, null)
    } // deleteOld()
    /***********************************************************************************************************************/

    // ----------------------------------------------------------------------------------------------------------------------
    // FUNCTIONS RELATED TO MENU:
    // ----------------------------------------------------------------------------------------------------------------------

    /***********************************************************************************************************************/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_stats -> {
                InfoMsg.modalDialog(
                    title = item.title.toString(),
                    msg = "",
                    funcToCallOnPositiveResponse = { deleteStats() },
                    context = this
                )
            }
            android.R.id.home -> finish() // user clicked "back" icon (left arrow) on the menu bar
        }
        return super.onOptionsItemSelected(item)
    } // onOptionsItemSelected()
    /***********************************************************************************************************************/
    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        (menu as MenuBuilder).setOptionalIconsVisible(true)
        menuInflater.inflate(R.menu.stats_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    } // onCreateOptionsMenu()
    /***********************************************************************************************************************/
}
