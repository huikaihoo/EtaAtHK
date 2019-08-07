package hoo.etahk.view.base

import androidx.annotation.StringRes
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import hoo.etahk.common.extensions.setSummary
import hoo.etahk.common.helper.SharedPrefsHelper

abstract class BasePrefFragment: PreferenceFragmentCompat() {

    companion object {
        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val bindSummaryOnPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                preference?.setSummary(newValue)
                true
            }
    }

    fun findPreference(@StringRes resId: Int): Preference {
        return super.findPreference(getString(resId))!!
    }

    fun bindPreferenceSummary(@StringRes resId: Int, onPreferenceChangeListener: Preference.OnPreferenceChangeListener = bindSummaryOnPreferenceChangeListener) {
        val preference = findPreference(resId)
        preference.onPreferenceChangeListener = onPreferenceChangeListener

        // Trigger the listener immediately with the preference's
        // current value.
        if ( preference is SwitchPreference || preference is CheckBoxPreference ) {
            preference.setSummary(SharedPrefsHelper.get<Boolean>(resId))
        } else {
            preference.setSummary(SharedPrefsHelper.get<String>(resId) as Any?)
        }
    }
}