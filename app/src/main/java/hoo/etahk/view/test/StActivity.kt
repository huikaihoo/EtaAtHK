package hoo.etahk.view.test

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.mcxiaoke.koi.ext.Bundle
import com.mcxiaoke.koi.ext.startActivity
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.view.App
import hoo.etahk.view.fh.FHActivity

class StActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val action = intent.action

        Log.d("TAG,", this.getComponentName().toString())

        if (Intent.ACTION_CREATE_SHORTCUT == action) {
            setupShortcut()
            finish()
            return
        }


        var value = Constants.MiscType.ROUTE_FAVOURITE.toString()
        try {

            val activityInfo = packageManager
                .getActivityInfo(
                    ComponentName(
                    this.packageName, this.packageName + ".CreateShortcuts"
                ), PackageManager.GET_META_DATA)


            value = activityInfo.metaData.getString(Constants.Argument.ARG_MISC_TYPE)
            Log.d("TAG,", this.getComponentName().toString())
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        Handler().postDelayed({
            this@StActivity.startActivity<FHActivity>(0, Bundle {
                putString(Constants.Argument.ARG_MISC_TYPE, value)
            })
//            this@StActivity.startActivity(
//                Intent(
//                    this@StActivity,
//                    BusSearchActivity::class.java
//                )
//            )
            this@StActivity.finish()
        }, 100)
    }

    private fun setupShortcut() {
        val shortcutIntent = Intent(Intent.ACTION_MAIN)
        shortcutIntent.setClassName(this, this.javaClass.name)

        // Then, set up the container intent (the response to the caller)

        val intent = Intent()
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, App.instance.getString(R.string.sc_follow_s))
        val iconResource =
            Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_shortcut_follow)
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)

        // Now, return the result to the launcher
        setResult(RESULT_OK, intent)
    }
}