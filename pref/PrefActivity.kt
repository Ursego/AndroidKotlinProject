package ca.intfast.iftimer.pref

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import ca.intfast.iftimer.R
import ca.intfast.iftimer.appwide.PrefKey
import ca.intfast.iftimer.util.toast

class PrefActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pref)
        setTitle(R.string.word__settings)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.layout_of_activity_pref, PrefFragment())
            .commit()

        // Display "back" icon (left arrow) on the menu bar:
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish() // user clicked "back" icon (left arrow) on the menu bar
        }
        return super.onOptionsItemSelected(item!!)
    }
}