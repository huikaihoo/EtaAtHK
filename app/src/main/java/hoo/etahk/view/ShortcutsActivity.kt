package hoo.etahk.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mcxiaoke.koi.ext.Bundle
import com.mcxiaoke.koi.ext.newIntent
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.constants.Argument
import hoo.etahk.view.fh.FHActivity
import hoo.etahk.view.follow.FollowActivity
import hoo.etahk.view.search.BusSearchActivity

class ShortcutsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (componentName.shortClassName) {
            getString(R.string.aan_sc_follow) -> {
                setupShortcut(R.string.sc_follow_s, R.drawable.ic_shortcut_follow, newIntent<FollowActivity>(0))
            }
            getString(R.string.aan_sc_favourite) -> {
                val intent = newIntent<FHActivity>(0, Bundle {
                    putString(Argument.ARG_MISC_TYPE, Constants.MiscType.ROUTE_FAVOURITE.toString())
                })
                setupShortcut(R.string.sc_favourite_s, R.drawable.ic_shortcut_favourite, intent)
            }
            getString(R.string.aan_sc_history) -> {
                val intent = newIntent<FHActivity>(0, Bundle {
                    putString(Argument.ARG_MISC_TYPE, Constants.MiscType.ROUTE_HISTORY.toString())
                })
                setupShortcut(R.string.sc_history_s, R.drawable.ic_shortcut_history, intent)
            }
            getString(R.string.aan_sc_bus) -> {
                setupShortcut(R.string.sc_bus_s, R.drawable.ic_shortcut_bus, newIntent<BusSearchActivity>(0))
            }
        }

        finish()
    }

    @Suppress("DEPRECATION")
    fun setupShortcut(labelResId: Int, iconResId: Int, intent: Intent) {
        val shortcutIntent = Intent()
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(labelResId))
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, iconResId))
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent)
        setResult(RESULT_OK, shortcutIntent)
    }
}
