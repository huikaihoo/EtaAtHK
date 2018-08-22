package hoo.etahk.view.settings

import android.os.Bundle
import hoo.etahk.R
import hoo.etahk.view.base.BasePrefFragment

class ParametersPrefFragment : BasePrefFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_general)
        setHasOptionsMenu(true)

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
    }
}