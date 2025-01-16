package ca.intfast.iftimer.util

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDoneException
import ca.intfast.iftimer.db.CustomSQLiteOpenHelper
import kotlin.reflect.KFunction

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// Before you add this class to your app, create CustomSQLiteOpenHelper: http://code.intfast.ca/viewtopic.php?t=815
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

open class CrudHelper(context: Context): CustomSQLiteOpenHelper(context) {
    // ----------------------------------------------------------------------------------------------------------------------
    // Encapsulates the typical CRUD functions applicable to DB entities.
    // In most cases, this class can be instantiated and used directly, with no need to be extended.
    // But if some entity's CRUD logic is less straightforward, you can inherit from CrudHelper and add/override functions:
    // class DeptCrudHelper(context: Context): CrudHelper(context) { ... }
    // ----------------------------------------------------------------------------------------------------------------------
    // Model (entity) classes (like Emp, Dept), for which you want to call CRUD functions, must implement Crudable interface.
    // ----------------------------------------------------------------------------------------------------------------------
    // How to use this class:
    //
    // Instantiate CrudHelper in the activity which will use it (like EmpListActivity and EmpEditActivity) as a property:
    // private val crudHelper = CrudHelper(context = this)
    //
    // That's it! Now, each function of the Activity can call the CRUD functions of crudHelper. For example:
    //
    // val emp = Emp()
    // newAutoincrementedId = crudHelper.insert(emp)
    // ...
    // crudHelper.update(emp)
    // ...
    // crudHelper.delete(emp)
    // empWithWorkerNumber25 = crudHelper.retrieveOne<Emp>(DbTable.EMP, "${DbColumn.WORKER_NUMBER} = 25")
    // val allEmployees = crudHelper.retrieveList<Emp>()
    // val activeEmployees = crudHelper.retrieveList<Emp>(tableName = DbTable.EMP, whereClause = "${DbColumn.IS_ACTIVE}=1")
    // val sql = "SELECT ${DbColumn.LAST_NAME} AS textValue FROM "${DbTable.EMP} ORDER BY ${DbColumn.LAST_NAME}"
    // val empLastNames = crudHelper.retrieveList<CrudableString>(sql)
    //
    // If no fitting function is found in crudHelper, the Activity can call the functions of
    // crudHelper.writableDatabase & crudHelper.readableDatabase directly.
    // For example, to run an SQL statement, which returns nothing (or you don't need the returned value), write:
    // crudHelper.writableDatabase.execSQL("...")
    // ----------------------------------------------------------------------------------------------------------------------

    // ----------------------------------------------------------------------------------------------------------------------
    // retrieveList() [SELECTs a recordset]:
    // ----------------------------------------------------------------------------------------------------------------------

    /***********************************************************************************************************************/
    inline fun <reified T: Crudable> retrieveList(sqlSelect: String, selectionArgs: Array<String>? = null): ArrayList<T> {
        // The number, types and order of the fields in the SELECT statement must correspond the fields, copied
        // in extractContentValues() and populateFromCursor() of the class, passed as T.

        // If you need to retrieve a recordset, which doesn't correspond to a particular table (for example,
        // to SELECT FROM a few joined tables, or grab statistics), then create (an pass to this function as <T>)
        // a custom class - just for that purpose. In this case, follow these rules:
        //      1. If the SQL SELECT has computed fields, give them aliases to be accessed by name in code.
        //      2. Override populateFromCursor() as usually (it's used on retrieval).
        //      3. Override TABLE_NAME, id and extractContentValues() this way:

        // override val TABLE_NAME: String
        //  get() = throw Exception("<YourClass>.TABLE_NAME should never be got!")
        //
        // override var id: Int?
        //   get() = throw Exception("<YourClass>.id should never be got!")
        //   set(value) {throw Exception("<YourClass>.id should never be set!")}
        //
        // override fun extractContentValues(): ContentValues = throw Exception("<YourClass>.extractContentValues() should never be called!")

        val entities = ArrayList<T>()
        val db = this.writableDatabase
        if (!db.isOpen) throw Exception("CrudHelper.retrieveList(): DB is closed.")

        val cursor = db.rawQuery(sqlSelect, selectionArgs)
            ?: throw Exception("CrudHelper.retrieveList(): rawQuery() returned null cursor by '$sqlSelect'.")
        cursor.use {
            while (cursor.moveToNext()) {
                // The following two code lines is a dirty trick to create an instance of a generic type.
                // To enable that, the generic parameter is marked as reified. That is possible only
                // in inline functions, so this function and all its callers are converted to inline.
                // http://code.intfast.ca/viewtopic.php?t=816
                val actualRuntimeClassConstructor: KFunction<T> = T::class.constructors.first()
                val entity: T = actualRuntimeClassConstructor.call()

                entity.populateFromCursor(cursor)
                entities.add(entity)
            }
        }

        return entities
    }
    /***********************************************************************************************************************/
    inline fun <reified T: Crudable> retrieveList
                (tableName: String, whereClause: String? = null, orderByClause: String? = null): ArrayList<T> {
        val sql = StringBuffer("SELECT * FROM $tableName")
        if (whereClause != null) sql.append(" WHERE $whereClause")
        if (orderByClause != null) sql.append(" ORDER BY $orderByClause")
        return this.retrieveList(sql.toString())
    }
    /***********************************************************************************************************************/

    // ----------------------------------------------------------------------------------------------------------------------
    // retrieveOne() [SELECTs one single record]:
    // ----------------------------------------------------------------------------------------------------------------------

    /***********************************************************************************************************************/
    inline fun <reified T: Crudable> retrieveOne
                (sqlSelect: String, selectionArgs: Array<String>? = null, required: Boolean = false): T? {
        val entities: ArrayList<T> = this.retrieveList(sqlSelect, selectionArgs)
        return when (entities.size) {
            1 -> entities[0]
            0 -> {
                if (required) throw Exception("CrudHelper.retrieveOne(): no data found by '$sqlSelect'.")
                null
            }
            else -> throw
            Exception("CrudHelper.retrieveOne(): ${entities.size} rows returned by '$sqlSelect' while one row expected.")
        }
    }
    /***********************************************************************************************************************/
    inline fun <reified T: Crudable> retrieveOne
                (tableName: String, id: Int, idColName: String = "_id", required: Boolean = true): T? {
        return this.retrieveOne(sqlSelect = "SELECT * FROM $tableName WHERE $idColName=$id", required = required)
    }
    /***********************************************************************************************************************/
    inline fun <reified T: Crudable> retrieveOne
                (tableName: String, whereClause: String, selectionArgs: Array<String>? = null, required: Boolean = true): T? {
        return this.retrieveOne(sqlSelect = "SELECT * FROM $tableName WHERE $whereClause",
            selectionArgs = selectionArgs, required = required)
    }
    /***********************************************************************************************************************/

    // ----------------------------------------------------------------------------------------------------------------------
    // Functions which SELECT one scalar value:
    // ----------------------------------------------------------------------------------------------------------------------

    /***********************************************************************************************************************/
    fun queryForString(sqlSelect: String, required: Boolean = false): String? {
        // Executes a statement that returns a scalar String value. For example, SELECT last_name FROM emp WHERE emp_id = 123
        val result: String

        try {
            val statement = this.readableDatabase.compileStatement(sqlSelect)
            result = statement.simpleQueryForString()
        } catch (e: SQLException /* compileStatement() failed */) {
            throw Exception("CrudHelper.queryForString(): '$sqlSelect' is not a valid SQL statement.")
        } catch (e: SQLiteDoneException /* simpleQueryForString() returned zero rows */) {
            if (required) throw Exception("CrudHelper.queryForString(): no data found by '$sqlSelect'.")
            return null
        }

        return result
    }
    /***********************************************************************************************************************/
    fun queryForLong(sqlSelect: String, required: Boolean = false): Long? {
        // Executes a statement that returns a scalar Long value. For example, SELECT COUNT(*) FROM emp
        val result: Long

        try {
            val statement = this.readableDatabase.compileStatement(sqlSelect)
            result = statement.simpleQueryForLong()
        } catch (e: SQLException /* compileStatement() failed */) {
            throw Exception("CrudHelper.queryForLong(): '$sqlSelect' is not a valid SQL statement.")
        } catch (e: SQLiteDoneException /* simpleQueryForLong() returned zero rows */) {
            if (required) throw Exception("CrudHelper.queryForLong(): no data found by '$sqlSelect'.")
            return null
        }

        return result
    }
    /***********************************************************************************************************************/
    fun queryForDouble(sqlSelect: String, required: Boolean = false): Double? {
        // Executes a statement that returns a scalar String value convertible to Double.
        // For example, SELECT salary FROM emp WHERE emp_id = 123
        val resultAsDouble: Double
        val resultAsString = this.queryForString(sqlSelect, required)
        if (resultAsString == null && !required) return null
        // if (result == null && required), then an Exception has already been thrown by queryForString()

        try {
            resultAsDouble = resultAsString!!.toDouble()
        } catch (e: NumberFormatException) {
            throw Exception("CrudHelper.queryForDouble(): The value, retrieved by '$sqlSelect', is $resultAsString. " +
                    "It cannot be converted to Double.")
        }

        return resultAsDouble
    }
    /***********************************************************************************************************************/
    fun queryForBoolean(sqlSelect: String, required: Boolean = false): Boolean? {
        // Executes a statement that returns a scalar Long value which can be treated as Boolean (i.e. 0 or 1).
        // For example, SELECT is_active FROM emp WHERE emp_id = 123
        val result = this.queryForLong(sqlSelect, required)
        if (result == null && !required) return null
        // if (result == null && required), then an Exception has already been thrown by queryForLong()

        when (result) {
            1L -> return true
            0L -> return false
        }

        throw Exception("CrudHelper.queryForBoolean(): The value, retrieved by '$sqlSelect', is $result. " +
                "To be treated as Boolean, it must be 0 or 1.")
    }
    /***********************************************************************************************************************/
    fun exists(tableName: String, whereClause: String? = null): Boolean {
        // Mimics the EXISTS statement of SQL.
        val sqlSelect = "SELECT Count(1) FROM $tableName" + if (whereClause != null) " WHERE $whereClause" else ""
        val count = this.queryForLong(sqlSelect, required = false)!!
        return (count > 0)
    }
    /***********************************************************************************************************************/

    // ----------------------------------------------------------------------------------------------------------------------
    // DML:
    // ----------------------------------------------------------------------------------------------------------------------

    /***********************************************************************************************************************/
    open fun insert(entity: Crudable, idAutoIncrement: Boolean = true): Int {
        if (idAutoIncrement && entity.id != null)
            throw Exception("CrudHelper.insert(): entity.id must be null (not ${entity.id}) since idAutoIncrement = true.")
        val cv = entity.extractContentValues()
        val rowId = this.writableDatabase.insert(entity.TABLE_NAME, null, cv)
        if (rowId == -1L) throw Exception("CrudHelper.insert() failed.")
        if (idAutoIncrement) entity.id = rowId.toInt()
        return rowId.toInt()
    }
    /***********************************************************************************************************************/
    open fun update(entity: Crudable, whereClause: String? = null): Int {
        // If whereClause is not supplied, this fun updates by entity.id.
        val cv = entity.extractContentValues()
        val finalWhereClause = whereClause ?: "${entity.ID_COL_NAME}=${entity.id}"
        return writableDatabase.update(entity.TABLE_NAME, cv, finalWhereClause, null)
    }
    /***********************************************************************************************************************/
    open fun upsert(entity: Crudable): Int { // UPDATE if exists, INSERT if doesn't; use with autoincremented ID
        return if (entity.id != null) update(entity) else insert(entity)
    }
    /***********************************************************************************************************************/
    open fun upsert(entity: Crudable, whereClause: String): Int { // UPDATE if exists, INSERT if doesn't; use with a custom PK
        val rowsUpdated = update(entity, whereClause)
        if (rowsUpdated > 0) return rowsUpdated
        return insert(entity)
    }
    /***********************************************************************************************************************/
    open fun delete(entity: Crudable): Int {
        // Deletes the entity by its id. If deleting condition is different (or there is no condition at all), then call directly:
        // <your CrudHelper>.writableDatabase.delete(<table>, <whereClause>, <whereArgs>)
        return this.writableDatabase.delete(entity.TABLE_NAME, "${entity.ID_COL_NAME}=${entity.id}", null)
    }
    /***********************************************************************************************************************/
} // class CrudHelper