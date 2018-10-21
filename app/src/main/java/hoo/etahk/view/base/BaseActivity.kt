package hoo.etahk.view.base

import android.app.ActivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import hoo.etahk.R
import hoo.etahk.common.Utils
import hoo.etahk.view.settings.SettingsActivity
import org.jetbrains.anko.startActivity

abstract class BaseActivity : AppCompatActivity() {
    var autoSetTaskDescription = true

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
            Utils.getBitmapFromVectorDrawable(this, R.drawable.ic_launcher_large),
            colorPrimary))
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
}