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