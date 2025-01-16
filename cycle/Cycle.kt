package ca.intfast.iftimer.cycle

import android.content.ContentValues
import android.database.Cursor
import ca.intfast.iftimer.db.DbColumn
import ca.intfast.iftimer.db.DbTable
import ca.intfast.iftimer.util.*
import java.time.LocalDateTime

class Cycle: Crudable {
    var meal1Start: LocalDateTime? = null
    var betweenMealsStart: LocalDateTime? = null
    var meal2Start: LocalDateTime? = null
    var fastingStart: LocalDateTime? = null
    var fastingFinish: LocalDateTime? = null
    /***********************************************************************************************************************/
    override val TABLE_NAME: String
        get() = DbTable.CYCLE
    /***********************************************************************************************************************/
    override var id: Int? = null
    /***********************************************************************************************************************/
    override fun extractContentValues() : ContentValues {
        val cv = ContentValues()
        // DON'T PUT PK FIELD(S) - PK IS AUTOGENERATED (ON INSERT) OR SUPPLIED WITHIN WHERE CLAUSE (ON UPDATE)
        cv.put(DbColumn.MEAL_1_START, this.meal1Start)
        cv.put(DbColumn.BETWEEN_MEALS_START, this.betweenMealsStart)
        cv.put(DbColumn.MEAL_2_START, this.meal2Start)
        cv.put(DbColumn.FASTING_START, this.fastingStart)
        cv.put(DbColumn.FASTING_FINISH, this.fastingFinish)
        return cv
    }
    /***********************************************************************************************************************/
    override fun populateFromCursor(cursor: Cursor) {
        this.id = cursor.getInt(DbColumn.ID)
        this.meal1Start = cursor.getLocalDateTime(DbColumn.MEAL_1_START)
        this.betweenMealsStart = cursor.getLocalDateTime(DbColumn.BETWEEN_MEALS_START)
        this.meal2Start = cursor.getLocalDateTime(DbColumn.MEAL_2_START)
        this.fastingStart = cursor.getLocalDateTime(DbColumn.FASTING_START)
        this.fastingFinish = cursor.getLocalDateTime(DbColumn.FASTING_FINISH)
    }
    /***********************************************************************************************************************/
    override fun toString(): String {
        return  "id=$id, " +
                "meal1Start=$meal1Start, " +
                "betweenMealsStart=$betweenMealsStart, " +
                "meal2Start=$meal2Start, " +
                "fastingStart=$fastingStart, " +
                "fastingFinish=$fastingFinish"
    }
    /***********************************************************************************************************************/
}