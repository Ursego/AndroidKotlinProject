package ca.intfast.iftimer.util

import android.content.SharedPreferences
import androidx.preference.*

/****************************************************************************************************************************
If you want the values of your Preferences be automatically reflected in the Summaries (on screen start, and each time
the value is changed), then inherit yours preference fragment from this class (rather than from PreferenceFragmentCompat).
PreferenceFragmentAutomaticSummary processes all the Preferences of types EditTextPreference, ListPreference and
MultiSelectListPreference (and their descendants) on your settings screen - no additional coding required.
See http://code.intfast.ca/viewtopic.php?t=821
****************************************************************************************************************************/

abstract class PreferenceFragmentAutomaticSummary:
                                            PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onResume() {
        // IF YOU OVERRIDE IT, DON'T FORGET TO CALL IN THE FIRST LINE: super.onResume()
        super.onResume()

        sharedPreferences = preferenceManager.sharedPreferences
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val preferenceScreen = preferenceScreen
        for (i in 0 until preferenceScreen.preferenceCount) {
            setSummary(getPreferenceScreen().getPreference(i))
        }
    }

    override fun onPause() {
        // IF YOU OVERRIDE IT, DON'T FORGET TO CALL IN THE LAST LINE: super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        // IF YOU OVERRIDE IT, DON'T FORGET TO CALL IN THE FIRST LINE: super.onSharedPreferenceChanged(sharedPreferences, key)
        val pref = findPreference<Preference>(key)
        if (pref != null)
            setSummary(pref)
    }

    private fun setSummary(pref: Preference) {
        when (pref) {
            is EditTextPreference -> pref.summary = pref.text
            is ListPreference -> pref.summary = pref.entry
            is MultiSelectListPreference -> pref.summary = pref.values.toTypedArray().contentToString()
//            is PreferenceCategory -> {
//                // Loop through child preferences:
//                for (i in 0 until pref.preferenceCount) {
//                    setSummary(pref.getPreference(i))
//                }
//            }
        }
    }
}