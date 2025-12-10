package ca.intfast.iftimer.stats

import android.content.ContentValues
import android.database.Cursor
import ca.intfast.iftimer.db.DbColumn
import ca.intfast.iftimer.util.Crudable
import ca.intfast.iftimer.util.getInt

class Stats(): Crudable {
    // Fields retrieved from DB in StatsActivity.retrieveStats():
    var avgMeal1: Int? = null
    var avgBetweenMeals: Int? = null
    var avgMeal2: Int? = null
    var avgEw: Int? = null
    var meal1Count: Int? = null
    var meal2Count: Int? = null

    // Fields calculated in StatsActivity.retrieveStats():
    var avgMeal: Int? = null
    var omadsCount: Int? = null
    var omadsPct: Int? = null

    override fun populateFromCursor(cursor: Cursor) {
        this.avgMeal1 = cursor.getInt(DbColumn.AVG_MEAL_1)
        this.avgBetweenMeals = cursor.getInt(DbColumn.AVG_BETWEEN_MEALS)
        this.avgMeal2 = cursor.getInt(DbColumn.AVG_MEAL_2)
        this.avgEw = cursor.getInt(DbColumn.AVG_EW)
        this.meal1Count = cursor.getInt(DbColumn.MEAL_1_COUNT)
        this.meal2Count = cursor.getInt(DbColumn.MEAL_2_COUNT)
    }

    // The rest are irrelevant since the data source is not a straightforward SELECT of all columns of one table:

    override val TABLE_NAME: String
        get() = throw Exception("Stats.TABLE_NAME should never be got!")

    override var id: Int?
        get() = throw Exception("Stats.id should never be got!")
        set(value) {throw Exception("Stats.id should never be set!")}

    override fun extractContentValues(): ContentValues = throw Exception("Stats.extractContentValues() should never be called!")
}