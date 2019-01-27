package hoo.etahk.view.base

import android.Manifest
import android.app.ActivityManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.getBitmapFromVectorDrawable
import hoo.etahk.view.settings.SettingsActivity
import org.jetbrains.anko.startActivity

abstract class BaseActivity : AppCompatActivity() {

    companion object {
        private val permissionMap = hashMapOf(
            Manifest.permission.ACCESS_FINE_LOCATION to Constants.Permission.PERMISSIONS_REQUEST_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE to Constants.Permission.PERMISSIONS_REQUEST_STORAGE
        )
    }

    var userIsInteracting: Boolean = false
    var autoSetTaskDescription = true

    override fun onUserInteraction() {
        super.onUserInteraction()
        userIsInteracting = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (autoSetTaskDescription) {
            setTaskDescription()
        }
    }

    fun setTaskDescription() {
        setTaskDescription(Utils.getThemeColorPrimaryDark(this))
    }

    fun setTaskDescription(colorPrimary: Int) {
        setTaskDescription(ActivityManager.TaskDescription(null,
            getBitmapFromVectorDrawable(R.drawable.ic_launcher_large),
            colorPrimary))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_empty, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_settings -> {
                startActivity<SettingsActivity>()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun replaceFragment(@IdRes originalFragmentId: Int, newFragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(originalFragmentId, newFragment)
            .addToBackStack(null)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }

    fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if the Permission is granted.
     * If yes, call [onRequestPermissionResult].
     * If no, call [ActivityCompat.requestPermissions]
     *
     * @param permission The name of the permission being checked and requested (if needed)
     * @return true if you have the permission already, or false if not
     */
    fun checkAndRequestPermission(permission: String): Boolean {
        val requestCode = permissionMap[permission]

        return when {
            requestCode == null -> {
                onRequestPermissionResult(false, permission)
                false
            }
            checkPermission(permission) -> {
                onRequestPermissionResult(true, permission)
                true
            }
            else -> {
                // Request the permission
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
                false
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionMap.forEach { (permission, code) ->
            if (requestCode == code) {
                val success = (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                onRequestPermissionResult(success, permission)
                return
            }
        }
    }

    open fun onRequestPermissionResult(isSuccess: Boolean, permission: String) {

    }
}