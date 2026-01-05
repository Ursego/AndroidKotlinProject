// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// Before you add this class to your app, create CustomSQLiteOpenHelper: https://tinyurl.com/SQLiteCRUD
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

package ca.intfast.iftimer.util

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDoneException
import ca.intfast.iftimer.db.CustomSQLiteOpenHelper
import ca.intfast.iftimer.db.DbColumn
import kotlin.reflect.KFunction

/************************************************************************************************************************
CrudHelper is an enhancement of CustomSQLiteOpenHelper which facilitates the typical CRUD operations.
It encapsulates population of ContentValues and reading from Cursor, so you will avoid them in your business logic.

The hierarchy:
SQLiteOpenHelper (a built-in type) - manages SQLite database manipulations (such as schema management, DML commands etc.)
    CustomSQLiteOpenHelper - adds creation, opening, and versioned schema upgrades for your app’s DB (an app-specific class)
        CrudHelper - adds encapsulation of CRUD operations (a generic util class, can be re-used in other apps)
-------------------------------------------------------------------------------------------------------------------------
IMPORTANT!
Entity classes (like Emp, Dept), for which you want to call CRUD functions of CrudHelper, must implement Crudable interface.

CrudHelper provides the following functions:

* retrieveRecord() // SELECT a single record
* retrieveList() // SELECT a recordset (ArrayList)

Functions which SELECT one scalar value of the given data type:

* retrieveString() // example: SELECT last_name FROM emp WHERE emp_id = 123
* retrieveLong() // example: SELECT COUNT(*) FROM emp
* retrieveDouble() // example: SELECT salary FROM emp WHERE emp_id = 123
* retrieveBoolean() // example: SELECT is_active FROM emp WHERE emp_id = 123
* exists() // mimics the EXISTS statement of SQL

// DML functions:

* insert()
* update()
* upsert() // UPDATE if the record exists, otherwise INSERT it
* delete()
-------------------------------------------------------------------------------------------------------------------------
USAGE:

To use CrudHelper, just instantiate it in the activity (like EmpListActivity and EmpEditActivity) as a property:
private val crudHelper = CrudHelper(context = this)
Now, enjoy the simplicity:

val newEmp = Emp()
<populate its fields>
val empId = crudHelper.insert(newEmp) // returns the DB-generated ID
...
emp = crudHelper.retrieveRecord<Emp>(
    tableName = DbTable.EMP,
    whereClause = "${DbColumn.EMP_ID} = ${emp.id}"
)
...
crudHelper.update(emp)
...
crudHelper.delete(emp)
...
val allEmployees = crudHelper.retrieveList<Emp>()
val activeEmployees = crudHelper.retrieveList<Emp>(tableName = DbTable.EMP, whereClause = "${DbColumn.IS_ACTIVE}=1")

If no fitting function is found in crudHelper, the Activity can call the functions of
  crudHelper.writableDatabase & crudHelper.readableDatabase directly.
For example, to run an SQL statement, which returns nothing (or you don't need the returned value), write:
crudHelper.writableDatabase.execSQL("...")
-------------------------------------------------------------------------------------------------------------------------
In most cases, CrudHelper can be instantiated and used directly, with no need to be extended.
But if some entity's CRUD logic is less straightforward, you can inherit from CrudHelper
    and add/override functions, providing the custom logic:
class DeptCrudHelper(context: Context): CrudHelper(context) { ... }
************************************************************************************************************************/

open class CrudHelper(context: Context): CustomSQLiteOpenHelper(context) {
    // ----------------------------------------------------------------------------------------------------------------------
    // retrieveRecord(sqlSelect, params, required)
    // retrieveRecord(tableName, id, idColName, required)
    // retrieveRecord(tableName, whereClause, params, required)
    // ----------------------------------------------------------------------------------------------------------------------

    inline fun <reified T: Crudable> retrieveRecord(
        sqlSelect: String,
        params: Array<String>? = null,
        required: Boolean = false
    ): T? {
        val entities: ArrayList<T> = this.retrieveList(sqlSelect, params)
        return when (entities.size) {
            1 -> entities[0]
            0 -> {
                if (required) throw Exception("CrudHelper.retrieveRecord(): no data found by '$sqlSelect'.")
                null
            }
            else -> throw
            Exception("CrudHelper.retrieveRecord(): ${entities.size} rows returned by '$sqlSelect' while one row expected.")
        }
    }

    inline fun <reified T: Crudable> retrieveRecord (
        tableName: String,
        id: Int,
        idColName: String = DbColumn.ID, // pass if the ID column name is not the default "_id"
        required: Boolean = true
    ): T? {
        return this.retrieveRecord(sqlSelect = "SELECT * FROM $tableName WHERE $idColName=$id", required = required)
    }

    inline fun <reified T: Crudable> retrieveRecord(
        tableName: String,
        whereClause: String,
        params: Array<String>? = null,
        required: Boolean = true
    ): T? {
        return this.retrieveRecord(
            sqlSelect = "SELECT * FROM $tableName WHERE $whereClause",
            params = params,
            required = required
        )
    }

    // ----------------------------------------------------------------------------------------------------------------------
    // retrieveList(sqlSelect, params)
    // retrieveList(tableName, whereClause, orderByClause)
    // ----------------------------------------------------------------------------------------------------------------------

    inline fun <reified T: Crudable> retrieveList(
        sqlSelect: String,
        params: Array<String>? = null
    ): ArrayList<T> {
        // The number, types and order of the fields in the SELECT statement must fit the fields, copied
        // in extractContentValues() and populateFromCursor() of the class, passed as T.

        // If you need to retrieve a recordset, which doesn't map to a particular table (for example,
        // to SELECT FROM a few joined tables, or grab statistics), then create (an pass to this function as <T>)
        // a custom class - just for that purpose. In this case, follow these rules:
        //      1. If the SQL SELECT has computed fields, give them aliases to be accessed by name in code.
        //      2. Override populateFromCursor() in the regular way (it's used on retrieval).
        //      3. Override tableName, id and extractContentValues() this way:

        // override val tableName: String
        //  get() = throw Exception("<YourClass>.tableName should never be obtained!")
        //
        // override var id: Int?
        //   get() = throw Exception("<YourClass>.id should never be obtained!")
        //   set(value) {throw Exception("<YourClass>.id should never be set!")}
        //
        // override fun extractContentValues(): ContentValues =
        //      throw Exception("<YourClass>.extractContentValues() should never be called!")

        val entities = ArrayList<T>()
        val db = this.writableDatabase
        if (!db.isOpen) throw Exception("CrudHelper.retrieveList(): DB is closed.")

        val cursor = db.rawQuery(sqlSelect, params)
        cursor.use {
            while (cursor.moveToNext()) {
                // The following two code lines is a dirty trick to create an instance of a generic type.
                // To enable that, the generic parameter is marked as reified.
                // That is possible only in inline functions, so this function and all its callers are converted to inline.
                // http://tinyurl.com/GenericTypeConstructor
                val actualRuntimeClassConstructor: KFunction<T> = T::class.constructors.first()
                val entity: T = actualRuntimeClassConstructor.call()

                entity.populateFromCursor(cursor)
                entities.add(entity)
            }
        }

        return entities
    }

    inline fun <reified T: Crudable> retrieveList(
        tableName: String,
        whereClause: String? = null,
        orderByClause: String? = null
    ): ArrayList<T> {
        val sql = buildString {
            append("SELECT * FROM $tableName")
            whereClause?.let { append(" WHERE $it") }
            orderByClause?.let { append(" ORDER BY $it") }
        }
        return this.retrieveList(sql)
    }

    // ----------------------------------------------------------------------------------------------------------------------
    // Functions to SELECT one scalar value:
    // ----------------------------------------------------------------------------------------------------------------------

    // Executes a statement that returns a scalar String value.
    fun retrieveString(
        sqlSelect: String,
        required: Boolean = false
    ): String? {
        val result: String

        try {
            val statement = this.readableDatabase.compileStatement(sqlSelect)
            result = statement.simpleQueryForString()
        } catch (e: SQLException /* compileStatement() failed */) {
            throw Exception("'$sqlSelect' is not a valid SQL statement.")
        } catch (e: SQLiteDoneException /* simpleQueryForString() returned zero rows */) {
            if (required) throw Exception("No data found by this query: '$sqlSelect'.")
            return null
        }

        return result
    }

    // Executes a statement that returns a scalar Long value.
    fun retrieveLong(
        sqlSelect: String,
        required: Boolean = false
    ): Long? {
        val result: Long

        try {
            val statement = this.readableDatabase.compileStatement(sqlSelect)
            result = statement.simpleQueryForLong()
        } catch (e: SQLException /* compileStatement() failed */) {
            throw Exception("CrudHelper.retrieveLong(): '$sqlSelect' is not a valid SQL statement.")
        } catch (e: SQLiteDoneException /* simpleQueryForLong() returned zero rows */) {
            if (required) throw Exception("CrudHelper.retrieveLong(): no data found by '$sqlSelect'.")
            return null
        }

        return result
    }

    // Executes a statement that returns a scalar String value convertible to Double.
    // For example, SELECT salary FROM emp WHERE emp_id = 123
    fun retrieveDouble(
        sqlSelect: String,
        required: Boolean = false
    ): Double? {
        val resultAsDouble: Double
        val resultAsString = this.retrieveString(sqlSelect, required)
        if (resultAsString == null && !required) return null
        // if (result == null && required), then an Exception has already been thrown by retrieveString()

        try {
            resultAsDouble = resultAsString!!.toDouble()
        } catch (e: NumberFormatException) {
            throw Exception("CrudHelper.retrieveDouble(): The value, retrieved by '$sqlSelect', is $resultAsString. " +
                    "It cannot be converted to Double.")
        }

        return resultAsDouble
    }

    // Executes a statement that returns a scalar Long value which can be treated as Boolean (i.e. 0 or 1).
    // For example, SELECT is_active FROM emp WHERE emp_id = 123
    fun retrieveBoolean(
        sqlSelect: String,
        required: Boolean = false
    ): Boolean? {
        val result = this.retrieveLong(sqlSelect, required)
        if (result == null && !required) return null
        // if (result == null && required), then an Exception has already been thrown by retrieveLong()

        when (result) {
            1L -> return true
            0L -> return false
        }

        throw Exception("CrudHelper.retrieveBoolean(): The value, retrieved by '$sqlSelect', is $result. " +
                "To be treated as Boolean, it must be 0 or 1.")
    }

    // Mimics the EXISTS statement of SQL.
    fun exists(
        tableName: String,
        whereClause: String? = null
    ): Boolean {
        val sqlSelect = "SELECT Count(1) FROM $tableName" + if (whereClause != null) " WHERE $whereClause" else ""
        val count = this.retrieveLong(sqlSelect, required = false)!!
        return (count > 0)
    }

    // ----------------------------------------------------------------------------------------------------------------------
    // DML:
    // ----------------------------------------------------------------------------------------------------------------------

    open fun insert(
        entity: Crudable,
        idAutoIncrement: Boolean = true
    ): Int {
        if (idAutoIncrement && entity.id != null)
            throw Exception("CrudHelper.insert(): entity.id must be null (not ${entity.id}) since idAutoIncrement = true.")
        val cv = entity.extractContentValues()
        val rowId = this.writableDatabase.insert(entity.tableName, null, cv)
        if (rowId == -1L) throw Exception("CrudHelper.insert() failed.")
        if (idAutoIncrement) entity.id = rowId.toInt()
        return rowId.toInt()
    }

    open fun update(
        entity: Crudable,
        whereClause: String? = null // if not supplied, this fun updates by entity.id.
    ): Int {
        val cv = entity.extractContentValues()
        val finalWhereClause = whereClause ?: "${entity.idColName}=${entity.id}"
        return this.writableDatabase.update(entity.tableName, cv, finalWhereClause, null)
    }

    // UPDATE if exists, INSERT if doesn't; use with autoincrement ID
    open fun upsert(
        entity: Crudable
    ): Int {
        return if (entity.id != null) update(entity) else insert(entity)
    }

    // UPDATE if exists, INSERT if doesn't; use with a custom PK
    open fun upsert(
        entity: Crudable,
        whereClause: String
    ): Int {
        val rowsUpdated = update(entity, whereClause)
        if (rowsUpdated > 0) return rowsUpdated
        return insert(entity)
    }

    // Deletes the entity by its id.
    // If the deleting condition is different (or there is no condition at all, which deletes all rows), then call directly:
    // <your CrudHelper>.writableDatabase.delete(<table>, <whereClause>, <whereArgs>)
    open fun delete(
        entity: Crudable
    ): Int {
        return this.writableDatabase.delete(
            entity.tableName,
            "${entity.idColName}=${entity.id}",
            null
        )
    }
} // class CrudHelper