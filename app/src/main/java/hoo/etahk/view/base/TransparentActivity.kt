package hoo.etahk.view.base

import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MenuItem
import com.mcxiaoke.koi.ext.find
import com.mcxiaoke.koi.utils.nougatOrNewer
import hoo.etahk.R
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

abstract class TransparentActivity : AppCompatActivity(), AnkoLogger {
    
    /**
     * @return Pending Top of GoogleMap UI component
     */
    protected val pendingTop: Int
        get() = actionBarHeight + if (isStatusBarTransparent) statusBarHeight else 0

    /**
     * @return Pending Bottom of GoogleMap UI component
     */
    protected val pendingBottom: Int
        get() = if (isInMultiWindowMode) 0 else navigationBarHeight

    /**
     * Source: http://stackoverflow.com/questions/3407256/height-of-status-bar-in-android/3410200#3410200
     * @return Height of StatusBar
     */
    private val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")

            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    /**
     * Source: http://stackoverflow.com/questions/29907615/android-transparent-status-bar-and-actionbar
     * @return Height of StatusBar
     */
    private val actionBarHeight: Int
        get() {
            val tv = TypedValue()
            theme.resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, tv, true)
            return TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        }

    /**
     * Source: http://stackoverflow.com/questions/3407256/height-of-status-bar-in-android/3410200#3410200
     * @return Height of NavigationBar
     */
    private val navigationBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")

            if (resourceId > 0 && showNavigationBar) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    /**
     * Source: https://stackoverflow.com/questions/28983621/detect-soft-navigation-bar-availability-in-android-device-progmatically
     * @return true if NavigationBar exist
     */
    private val showNavigationBar: Boolean
        get() {
            val resourceId = resources.getIdentifier("config_showNavigationBar", "bool", "android")
            return (resourceId > 0 && resources.getBoolean(resourceId))
        }

    /**
     * Source: https://stackoverflow.com/questions/28983621/detect-soft-navigation-bar-availability-in-android-device-progmatically
     * @return true if StatusBar is Transparent
     */
    private val isStatusBarTransparent: Boolean
        get() {
            if (!isInMultiWindowMode) {
                return true
            }

            val d = windowManager.defaultDisplay

            val realDisplayMetrics = DisplayMetrics()
            d.getRealMetrics(realDisplayMetrics)

            val realHeight = realDisplayMetrics.heightPixels
            val realWidth = realDisplayMetrics.widthPixels

            val displayMetrics = DisplayMetrics()
            d.getMetrics(displayMetrics)

            val displayHeight = displayMetrics.heightPixels + statusBarHeight + navigationBarHeight
            val displayWidth = displayMetrics.widthPixels

            debug("$realHeight $realWidth $displayHeight $displayWidth")
            return (displayHeight >= realHeight) || (displayWidth >= realWidth)
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.menu_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun isInMultiWindowMode(): Boolean {
        var result = false
        if (nougatOrNewer()) {
            result = super.isInMultiWindowMode()
        }
        return result
    }

    protected fun setupActionBar() {
        // Setup ActionBar
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(this, R.color.colorSemiTransparentBlack)
                )
            )
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        // Setup Toolbar
        val toolbar = find<Toolbar>(R.id.action_bar)
        toolbar.contentInsetStartWithNavigation = 0
    }
}
