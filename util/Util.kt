package ca.intfast.iftimer.util

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import ca.intfast.iftimer.R
import androidx.core.net.toUri


fun toast(context: Context, msg: String) = Toast.makeText(context, msg, Toast.LENGTH_LONG).show()

fun shareInSocialMedia(subject: String, msg: String, context: Context, recipient: String = "") {
    val intent = Intent(Intent.ACTION_SEND)

    "mailto:".toUri().also { intent.data = it }
    intent.type = "text/plain"

    if (recipient != "") intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, msg)

    try {
        ContextCompat.startActivity(context, Intent.createChooser(intent, context.getString(R.string.menu__share)), null)
    }
    catch (e: Exception){
        // No client able to send found:
        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
    }
} // shareInSocialMedia

//fun <T> Array<T>.toArrayList(): ArrayList<T> {
//    val arrayList = ArrayList<T>()
//    for ((i, value) in this.withIndex()) {
//        arrayList.add(value)
//    }
//    return arrayList
//}

//fun checkDb(context: Context, className: String? = null, funcName: String? = null): String {
//    // Call it this way: checkDb(this, this::class.simpleName!!, "funcname()")
//    if (!g_DEBUG_MODE) return ""
//    val msg: String
//    val crudHelper = CrudHelper(context)
//    val c = crudHelper.retrieveOne<Cycle>(DbTable.CYCLE, whereClause = "fastingFinish IS NULL", required = false)
//    // c is null now if startMeal1(), which INSERTs cycles, was never called after app install
//    if (c == null) {
//        msg = "Curr Cycle doesn't exist in DB"
//    } else {
//        msg = c.toString()
//    }
//    //dbgMsg(msg, className, funcName)
//    return msg
//}

//@Suppress("UNCHECKED_CAST")
//fun <T> genericArrayOfNulls(size: Int): Array<T?> = arrayOfNulls<Any?>(size) as Array<T?>

