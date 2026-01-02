/****************************************************************************************************************************
Dbg is a lightweight debug helper utility for logging and displaying debug messages.

It provides two main functions to assist with debugging:

* Dbg.log(msg: String)
    Wraps Log.d to print messages to Logcat, automatically appending the caller's class and method name.

* Dbg.msg(msg: String, context: Context)
    Displays a modal alert dialog with the message, useful for quick visual debugging without checking Logcat.
    Includes logic to prevent execution in prod eliminating risk if debug calls are accidentally left in the code.

Both the functions display the caller, i.e. the function which has invoked the Dbg.log() or Dbg.msg().
That can be useful if you debug a scenario consisting of a few functions.
In fact, it's the only reason to prefer Dbg.log() over Log.d().

https://github.com/Ursego/AndroidKotlin/blob/main/Dbg.kt
****************************************************************************************************************************/

package ca.intfast.iftimer.util

import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AlertDialog

object Dbg {
    // Temporarily make DBG_MODE true to enable debug functionality which should not work in prod (like Dbg.msg()).
    // Before publishing, don't forget make DBG_MODE false again:
    private const val DBG_MODE = true

    fun log(msg: String) {
        var caller = getCaller()
        caller = if (caller.isNotEmpty()) " $caller " else ""
        Log.d("#######$caller#######", msg)
    }

    fun msg(msg: String, context: Context) {
        if (!isDbgMode()) return // disable the message in prod if the developer has forgotten to remove a Dbg.msg() call

        val caller = getCaller()
        val displayMsg = if (caller.isNotEmpty()) "$caller\n\n$msg" else msg

        Handler(context.mainLooper).post {
            AlertDialog.Builder(context)
                .setTitle("Debug")
                .setMessage(displayMsg)
                .setPositiveButton("Close", null)
                .setCancelable(false)
                .show()
        }
    }

    private fun getCaller(): String {
        val stackTrace = Throwable().stackTrace

        val dbgClassName = Dbg::class.java.name

        // Packages/classes that should never be reported as the "caller"
        val skipPrefixes = listOf(
            dbgClassName, // Dbg itself
            "java.",
            "kotlin.",
            "kotlinx.",
            "dalvik.",
            "sun.",
            "com.android.",
            "android.",
            "androidx."
        )

        // Scan the stack trace and pick the first frame that belongs to your app code,
        //   skipping Dbg itself + framework/runtime calls (android/androidx/kotlin/java/etc.).
        for (e in stackTrace) {
            val cn = e.className

            // Skip Dbg itself and any framework/runtime frames
            if (skipPrefixes.any { cn.startsWith(it) }) continue

            val method = e.methodName

            // Clean up generated class names (e.g., MainActivity$onCreate$1)
            val simpleClass = cn.substringAfterLast('.').substringBefore('$')

            // "Top-level" Kotlin functions live in *Kt classes (FileNameKt)
            val isTopLevelFunc = cn.endsWith("Kt") && !cn.contains("$")

            return if (isTopLevelFunc) {
                method
            } else {
                "$simpleClass.$method"
            }
        }

        return ""
    }

    fun isDbgMode(): Boolean {
        if (!this.DBG_MODE) return false
        // A safeguard to prevent debug features from accidentally appearing in production builds
        // running on real user devices, even if the developer forgot to revert DBG_MODE to false:
        return isRunningOnEmulator()
    }

    fun isRunningOnEmulator(): Boolean {
        return (
                Build.FINGERPRINT.startsWith("generic") ||
                        arrayOf("vbox", "test-keys").any { Build.FINGERPRINT.lowercase().contains(it) } ||
                        arrayOf("google_sdk", "Emulator", "Android SDK built for x86").any { Build.MODEL.contains(it) } ||
                        Build.MANUFACTURER.contains("Genymotion") ||
                        (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                        Build.PRODUCT.contains("sdk_gphone") ||
                        arrayOf("goldfish", "ranchu").any { Build.HARDWARE.contains(it) }
                )
    }
}
