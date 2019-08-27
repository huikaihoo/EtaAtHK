package hoo.etahk.view.base

import androidx.annotation.StringRes
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

abstract class BasePrefFragment: PreferenceFragmentCompat() {
    fun findPreference(@StringRes resId: Int): Preference {
        return super.findPreference(getString(resId))!!
    }
}