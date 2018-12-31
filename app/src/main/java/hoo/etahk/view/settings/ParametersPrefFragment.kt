package hoo.etahk.view.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hoo.etahk.R
import hoo.etahk.view.base.BasePrefFragment

class ParametersPrefFragment : BasePrefFragment() {

    /**
     * Called during [.onCreate] to supply the preferences for this fragment.
     * Subclasses are expected to call [.setPreferenceScreen] either
     * directly or via helper methods such as [.addPreferencesFromResource].
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     * @param rootKey If non-null, this preference fragment should be rooted at the
     * [androidx.preference.PreferenceScreen] with this key.
     */
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_parameters)
        setHasOptionsMenu(true)

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        bindPreferenceSummaryToValue(R.string.param_app_mode)
        bindPreferenceSummaryToValue(R.string.param_paged_list_page_size)
        bindPreferenceSummaryToValue(R.string.param_gist_id_kmb)
        bindPreferenceSummaryToValue(R.string.param_gist_id_nwfb)
        bindPreferenceSummaryToValue(R.string.param_user_agent)
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.pref_title_parameters)
    }
}