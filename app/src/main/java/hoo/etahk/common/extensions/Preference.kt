package hoo.etahk.common.extensions

import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference

fun Preference.setSummary(newValue: Any?) {
    val newValueStr = newValue.toString()

    if (this is ListPreference) {
        // For list preferences, look up the correct display value in
        // the preference's 'entries' list.
        val index = this.findIndexOfValue(newValueStr)

        // Set the summary to reflect the new value.
        this.setSummary(
            if (index >= 0)
                this.entries[index]
            else
                null
        )
    } else if (this !is SwitchPreference && this !is CheckBoxPreference) {
        // For all other preferences, set the summary to the value's
        // simple string representation.
        this.summary = newValueStr
    }
}