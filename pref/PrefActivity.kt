package ca.intfast.iftimer.pref

import android.os.Bundle
import ca.intfast.iftimer.R
import ca.intfast.iftimer.util.AppActivity

class PrefActivity: AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pref)
        setTitle(R.string.word__settings)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.layout_of_activity_pref, PrefFragment())
            .commit()
    }
}