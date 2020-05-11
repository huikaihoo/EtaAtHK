package hoo.etahk.view.settings

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
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

class RootPrefFragment : BasePrefFragment() {

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
        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        setPreferencesFromResource(R.xml.pref_root, rootKey)

        // General
        val language = findPreference(R.string.pref_language)
        language.setOnPreferenceChangeListener { _, newValue ->
            AppHelper.applyAppLocale(newValue.toString())
            activity?.restart()
            true
        }

        val updateRoutes = findPreference(R.string.pref_update_routes)
        updateRoutes.setOnPreferenceClickListener {
            App.instance.startServiceCompat(activity!!.newIntent<UpdateRoutesService>())
            true
        }

        // Backup and Restore
        val backup = findPreference(R.string.pref_backup)
        backup.setOnPreferenceClickListener {
            val exporter = Exporter()
            AlertDialogBuilder(context!!)
                .setTitle(R.string.title_backup_save_success_to)
                .setMessage(exporter.export())
                .setPositiveButton(android.R.string.ok, null)
                .show()
            true
        }

        val restore = findPreference(R.string.pref_restore)
        restore.setOnPreferenceClickListener {
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
        appName.setOnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, getString(R.string.play_store_developer_url).toUri()))
            true
        }

        val appVersion = findPreference(R.string.pref_app_version)
        appVersion.summary = App.instance.getVersionName()

        val privacyPolicy = findPreference(R.string.pref_privacy_policy)
        privacyPolicy.setOnPreferenceClickListener {
            (activity as AppCompatActivity).startCustomTabs(getString(R.string.privacy_policy_url))
            true
        }

        val disclaimer = findPreference(R.string.pref_disclaimer)
        disclaimer.setOnPreferenceClickListener {
            (activity as AppCompatActivity).startCustomTabs(getString(R.string.disclaimer_url))
            true
        }

        val licenses= findPreference(R.string.pref_licenses)
        licenses.setOnPreferenceClickListener {
            activity?.startActivity<OssLicensesMenuActivity>(
                Argument.ARG_TITLE to getString(R.string.pref_title_licenses)
            )
            true
        }

        // Testing
        val parameters = findPreference(R.string.pref_parameters)

        if (!viewModel.showTesting) {
            preferenceScreen.removePreference(findPreference(R.string.pref_testing))
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