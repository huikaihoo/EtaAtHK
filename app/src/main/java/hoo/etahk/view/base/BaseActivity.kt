package hoo.etahk.view.base

import android.app.ActivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import hoo.etahk.R
import hoo.etahk.common.Utils

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
}