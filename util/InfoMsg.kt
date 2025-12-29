package ca.intfast.iftimer.util

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import ca.intfast.iftimer.R

object InfoMsg {
    fun show(title: String, msg: String, context: Context /*, buttonText: String? = null */) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(/* buttonText ?: */ context.getString(android.R.string.ok)
            ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .show()
    }

    fun modalDialog ( // https://tinyurl.com/InfoMsg
        title: String? = null,
        msg: String,
        funcToCallOnPositiveResponse: () -> Unit,
        funcToCallOnNegativeResponse: () -> Unit = {/* dummy function, does nothing */},
        context: Context,
        positiveButtonText: String = context.getString(R.string.word__yes),
        negativeButtonText: String = context.getString(R.string.word__no),
        doYoWantToProceedMsgFragment: String = context.getString(R.string.msg__do_you_want_to_proceed)
    ) {
        val builder = AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(msg + "\n\n" + doYoWantToProceedMsgFragment)
            setPositiveButton(positiveButtonText) { _, _ -> funcToCallOnPositiveResponse() }
            setNegativeButton(negativeButtonText) { _, _ -> funcToCallOnNegativeResponse() }
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()

        // Example of use:
        /*
        InfoMsg.modalDialog (
            title = getString(R.string.msg__do_you_want_to_take_over_the_world__title), // omit this arg if no title
            msg = getString(R.string.msg__do_you_want_to_take_over_the_world),
            funcToCallOnPositiveResponse = {takeOverTheWorld()},
            funcToCallOnNegativeResponse = {displayMsgYouAreLooser()}, // omit this arg to do nothing on Cancel
            context = this
        )
        */
    }

//    fun modalDialogOkCancel( // overload if nothing should be done on Cancel (no funcToCallOnCancel arg)
//                title: String? = null,
//                msg: String,
//                funcToCallOnOk: () -> Unit,
//                context: Context,
//                positiveButtonText: String? = null,
//                negativeButtonText: String? = null) {
//        modalDialogOkCancel(
//            title = title,
//            msg = msg,
//            funcToCallOnOk = funcToCallOnOk,
//            funcToCallOnCancel = {/* dummy function, does nothing */},
//            context = context,
//            positiveButtonText = positiveButtonText,
//            negativeButtonText = negativeButtonText
//        )
//    }
}