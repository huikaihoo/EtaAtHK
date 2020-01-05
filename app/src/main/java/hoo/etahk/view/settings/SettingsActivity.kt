package hoo.etahk.view.settings

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProviders
import hoo.etahk.BuildConfig
import hoo.etahk.R
import hoo.etahk.common.extensions.startCustomTabs
import hoo.etahk.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_settings.toolbar

class SettingsActivity : BaseActivity() {

    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)

        if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            checkAndRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            if (!viewModel.isInit) {
                onRequestPermissionResult(true, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onRequestPermissionResult(isSuccess: Boolean, permission: String) {
        viewModel.isInit = true

        // load General Preference fragment
        supportFragmentManager.beginTransaction().replace(R.id.container, RootPrefFragment()).commitAllowingStateLoss()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.menu_play_store -> {
                startActivity(Intent(Intent.ACTION_VIEW, (getString(R.string.play_store_app_url) + BuildConfig.APPLICATION_ID).toUri()))
                true
            }
            R.id.menu_github -> {
                startCustomTabs(getString(R.string.github_url))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}