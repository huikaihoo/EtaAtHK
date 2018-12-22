package hoo.etahk.view.settings

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import hoo.etahk.BuildConfig
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.view.AlertDialogBuilder
import hoo.etahk.transfer.data.Exporter
import hoo.etahk.transfer.data.Importer
import hoo.etahk.transfer.repo.RoutesRepo
import hoo.etahk.view.base.BasePrefFragment
import hoo.etahk.view.follow.FollowActivity
import org.jetbrains.anko.startActivity

class GeneralPrefFragment : BasePrefFragment() {
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
        addPreferencesFromResource(R.xml.pref_general)
        setHasOptionsMenu(true)

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.

        // General
        val updateRoutes = findPreference(R.string.pref_update_routes)

        updateRoutes.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            RoutesRepo.updateParentRoutes(Constants.Company.BUS, true)
            true
        }

        // Backup and Restore
        val backup = findPreference(R.string.pref_backup)

        backup.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val exporter = Exporter()
            AlertDialogBuilder(context!!)
                .setTitle(exporter.export())
                .setPositiveButton(android.R.string.ok, null)
                .show()
            true
        }

        val restore = findPreference(R.string.pref_restore)

        restore.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val importer = Importer()
            val displayList = importer.getBackupList()
            var selectedIndex = -1

            if (displayList.isNotEmpty()) {
                lateinit var positiveButton: Button
                val dialog = AlertDialogBuilder(context!!, R.style.AppTheme_Dialog)
                    .setTitle(R.string.title_select_backup_to_restore)
                    .setSingleChoiceItems(displayList, selectedIndex) { dialog, which ->
                        selectedIndex = which
                        positiveButton.isEnabled = (selectedIndex >= 0)
                    }
                    .setPositiveButton(android.R.string.ok) { dialog, which ->
                        AlertDialogBuilder(context!!)
                            .setTitle(importer.import(displayList[selectedIndex].toString()))
                            .setPositiveButton(android.R.string.ok) { dialog, which ->
                                activity?.finishAffinity()
                                activity?.startActivity<FollowActivity>()
                            }
                            .show()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()

                positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.isEnabled = false
            } else {
                AlertDialogBuilder(context!!)
                    .setTitle("Cannot find any backup")
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
            true
        }

        // About
        val appVersion = findPreference(R.string.pref_app_version)

        appVersion.summary = BuildConfig.VERSION_NAME
        appVersion.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
            var counter = 0

            override fun onPreferenceClick(preference: Preference): Boolean {
                if (counter == 6) {
                    //preferenceScreen.addPreference()
                } else {
                    counter++
                }
                return true
            }
        }

        val licenses= findPreference(R.string.pref_licenses)

        licenses.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            activity?.startActivity<OssLicensesMenuActivity>(
                Constants.Argument.ARG_TITLE to getString(R.string.pref_title_licenses)
            )
            true
        }
    }
}