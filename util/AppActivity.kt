package ca.intfast.iftimer.util

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

// A class to be inherited instead of AppCompatActivity by any Activity
// which should have a back icon on the menu bar.
open class AppActivity(): AppCompatActivity() {
    // Override in derived activities if they don't want the left arrow icon on the menu bar:
    protected open val displayBackIcon = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (displayBackIcon) {
            val actionBar = this.supportActionBar
                ?: throw Exception("AppActivity.onCreate(): actionBar is null.")
            actionBar.setDisplayHomeAsUpEnabled(displayBackIcon)
            actionBar.setDisplayShowHomeEnabled(displayBackIcon)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish() // user clicked "back" icon (left arrow) on the menu bar
        }
        return super.onOptionsItemSelected(item)
    }
}