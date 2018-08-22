package hoo.etahk.view.settings

import android.os.Bundle
import android.preference.Preference
import android.support.v7.app.AlertDialog
import android.widget.Button
import hoo.etahk.BuildConfig
import hoo.etahk.R
import hoo.etahk.transfer.data.Exporter
import hoo.etahk.transfer.data.Importer
import hoo.etahk.view.base.BasePrefFragment
import hoo.etahk.view.follow.FollowActivity
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.jetbrains.anko.startActivity

class GeneralPrefFragment : BasePrefFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_general)
        setHasOptionsMenu(true)

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.

        // Backup and Restore
        val backup = findPreference(R.string.pref_backup)

        backup.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val exporter = Exporter()
            alert(exporter.export()){
                okButton {}
            }.show()
            true
        }

        val restore = findPreference(R.string.pref_restore)

        restore.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val importer = Importer()
            val displayList = importer.getBackupList()
            var selectedIndex = -1

            if (displayList.isNotEmpty()) {
                lateinit var positiveButton: Button
                val dialog = AlertDialog.Builder(activity!!)
                    .setTitle(R.string.title_select_groups_to_move)
                    .setSingleChoiceItems(displayList, selectedIndex) { dialog, which ->
                        selectedIndex = which
                        positiveButton.isEnabled = (selectedIndex >= 0)
                    }
                    .setPositiveButton(android.R.string.ok) { dialog, which ->
                        alert(importer.import(displayList[selectedIndex].toString())) {
                            okButton {
                                activity.finishAffinity()
                                startActivity<FollowActivity>()
                            }
                        }.show()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()

                positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.isEnabled = false
            } else {
                alert("Cannot find any backup"){
                    okButton {}
                }.show()
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
    }
}