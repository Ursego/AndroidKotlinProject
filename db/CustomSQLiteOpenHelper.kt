package ca.intfast.iftimer.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// http://code.intfast.ca/viewtopic.php?t=815
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

open class CustomSQLiteOpenHelper(context: Context): SQLiteOpenHelper(context, DbInfo.NAME, null, DbInfo.VERSION) {
    /***********************************************************************************************************************/
    override fun onCreate(db: SQLiteDatabase) {
        this.createDbObjects(db)
    }
    /***********************************************************************************************************************/
    private fun createDbObjects(db: SQLiteDatabase) {
        // Extracted from onCreate() to allow the logic be executed many times from onOpen() in debug purpose.
        // In production, it will be called only once, from onCreate(), since onOpen() will be commented out.

//        db.execSQL("DROP TABLE IF EXISTS " + DbTable.CYCLE) // todo: make sure it's commented out!!!

        val sql = "CREATE TABLE " + DbTable.CYCLE + " (" +
                DbColumn.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DbColumn.MEAL_1_START + " TEXT, " +
                DbColumn.BETWEEN_MEALS_START + " TEXT, " +
                DbColumn.MEAL_2_START + " TEXT, " +  // null - it was OMAD
                DbColumn.FASTING_START + " TEXT, " +
                DbColumn.FASTING_FINISH + " TEXT " +  // null - curr cycle, not null - archived cycle
            ")"
        db.execSQL(sql)
    }
    /***********************************************************************************************************************/
//    override fun onOpen(db: SQLiteDatabase?) { // todo: make sure it's commented out!!!
//        super.onOpen(db)
//        this.createDbObjects(db!!)
//    }
    /***********************************************************************************************************************/
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // See how to change DB schema correctly: https://thebhwgroup.com/blog/how-android-sqlite-onupgrade

//        if (oldVersion < 2)
//            db.execSQL("ALTER TABLE A ...")
//        if (oldVersion < 3)
//            db.execSQL("ALTER TABLE B ...")
    }
    /***********************************************************************************************************************/
    override fun close() {
        this.writableDatabase.close()
        super.close()
    }
    /***********************************************************************************************************************/
}
