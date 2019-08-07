package hoo.etahk.view.settings

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProviders
import androidx.preference.Preference
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.mcxiaoke.koi.ext.newIntent
import com.mcxiaoke.koi.ext.restart
import hoo.etahk.R
import hoo.etahk.common.constants.Argument
import hoo.etahk.common.extensions.startCustomTabs
import hoo.etahk.common.extensions.startServiceCompat
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.view.AlertDialogBuilder
import hoo.etahk.transfer.data.Exporter
import hoo.etahk.transfer.data.Importer
import hoo.etahk.view.App
import hoo.etahk.view.base.BaseActivity
import hoo.etahk.view.base.BasePrefFragment
import hoo.etahk.view.follow.FollowActivity
import hoo.etahk.view.service.UpdateRoutesService
import org.jetbrains.anko.startActivity

class GeneralPrefFragment : BasePrefFragment() {

    private lateinit var testing: Preference
    private lateinit var viewModel: SettingsViewModel

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
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)

        addPreferencesFromResource(R.xml.pref_general)
        setHasOptionsMenu(true)

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.

        // General
        bindPreferenceSummary(R.string.pref_language, Preference.OnPreferenceChangeListener { preference, newValue ->
            AppHelper.applyAppLocale(newValue.toString())
            activity?.restart()
            true
        })

        val updateRoutes = findPreference(R.string.pref_update_routes)

        updateRoutes.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            App.instance.startServiceCompat(activity!!.newIntent<UpdateRoutesService>())
            true
        }

        // Bus
        bindPreferenceSummary(R.string.pref_bus_jointly)

        // Backup and Restore
        val backup = findPreference(R.string.pref_backup)

        backup.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val exporter = Exporter()
            AlertDialogBuilder(context!!)
                .setTitle(R.string.title_backup_save_success_to)
                .setMessage(exporter.export())
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
        val appName = findPreference(R.string.pref_app_name)
        appName.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, getString(R.string.play_store_developer_url).toUri()))
            true
        }

        val appVersion = findPreference(R.string.pref_app_version)
        appVersion.summary = App.instance.getVersionName()

        val privacyPolicy = findPreference(R.string.pref_privacy_policy)
        privacyPolicy.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            (activity as AppCompatActivity).startCustomTabs(getString(R.string.privacy_policy_url))
            true
        }

        val disclaimer = findPreference(R.string.pref_disclaimer)
        disclaimer.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            (activity as AppCompatActivity).startCustomTabs(getString(R.string.disclaimer_url))
            true
        }

        val licenses= findPreference(R.string.pref_licenses)

        licenses.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            activity?.startActivity<OssLicensesMenuActivity>(
                Argument.ARG_TITLE to getString(R.string.pref_title_licenses)
            )
            true
        }

        // Testing
        val parameters = findPreference(R.string.pref_parameters)

        parameters.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            activity!!.supportFragmentManager.beginTransaction().replace(R.id.container, ParametersPrefFragment()).addToBackStack(null).commit()
            true
        }

        testing = findPreference(R.string.pref_testing)
        if (!viewModel.showTesting) {
            preferenceScreen.removePreference(testing)
        }

        // Disable preference if permission is not granted
        if (!(activity as BaseActivity).checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            backup.isEnabled = false
            restore.isEnabled = false
            parameters.isEnabled = false
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.title_settings)
    }
}