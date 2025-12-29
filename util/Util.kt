package ca.intfast.iftimer.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.LocaleList
import android.widget.Toast
import androidx.core.content.ContextCompat
import ca.intfast.iftimer.R

fun toast(context: Context, msg: String) = Toast.makeText(context, msg, Toast.LENGTH_LONG).show()

fun share(subject: String, msg: String, context: Context, recipient: String = "") {
    val intent = Intent(Intent.ACTION_SEND)

    intent.data = Uri.parse("mailto:")
    intent.type = "text/plain"

    if (recipient != "") intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, msg)

    try {
        ContextCompat.startActivity(context, Intent.createChooser(intent, context.getString(R.string.menu__share)), null)
    }
    catch (e: Exception){
        // No client, able to send, found:
        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
    }
}

fun easternSlavicLangExistsInDevice(): Boolean {
    val ussrLangs = arrayListOf(
        "ru" /* Russian */,
        "uk" /* Ukrainian */,
        "be" /* Belorussian */
    )
    val localeList = LocaleList.getDefault()
    val localeListSize = localeList.size()
    var i = 0

    while (i < localeListSize) {
        if (ussrLangs.contains(localeList[i].language)) return true // LocaleList.[int] is operator overload for LocaleList.get(int)
        i++
    }
    return false
}

//fun ussrLangExistsInDevice(): Boolean {
//    val ussrLangs = arrayListOf(
//        "az" /* Azerbaijani */,
//        "be" /* Belorussian */,
//        "et" /* Estonian */,
//        "hy" /* Armenian */,
//        "ka" /* Georgian */,
//        "kk" /* Kazakh */,
//        "ky" /* Kirghiz */,
//        "lt" /* Lithuanian */,
//        "lv" /* Latvian */,
//        "mo" /* Moldavian */,
//        "ru" /* Russian */,
//        "tg" /* Tajik */,
//        "tk" /* Turkmen */,
//        "uk" /* Ukrainian */,
//        "uz" /* Uzbek */
//    )
//    val localeList = LocaleList.getDefault()
//    val localeListSize = localeList.size()
//    var i = 0
//
//    while (i < localeListSize) {
//        if (ussrLangs.contains(localeList[i].language)) return true // LocaleList.[int] is operator overload for LocaleList.get(int)
//        i++
//    }
//    return false
//}

//fun dbgMsg(msg: String, className: String? = null, funcName: String? = null) {
//    // Call it this way: dbgMsg("Fuck!", this::class.simpleName, "funcname()")
//    if (!g_DEBUG_MODE) return
//    Log.d("####### $className.$funcName #######", "$msg")
//}

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

