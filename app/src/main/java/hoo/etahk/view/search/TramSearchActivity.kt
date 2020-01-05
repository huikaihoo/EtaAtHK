package hoo.etahk.view.search

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.mcxiaoke.koi.ext.newIntent
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Constants.RouteType
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.createShortcut
import hoo.etahk.common.extensions.tag
import hoo.etahk.common.tools.ThemeColor
import hoo.etahk.view.base.NavActivity
import kotlinx.android.synthetic.main.activity_container_tabs_fab.container
import kotlinx.android.synthetic.main.activity_container_tabs_fab.fab
import kotlinx.android.synthetic.main.activity_container_tabs_fab.tabs
import kotlinx.android.synthetic.main.activity_container_tabs_progress.toolbar
import kotlinx.android.synthetic.main.activity_search_nav.nav

class TramSearchActivity : NavActivity() {

    companion object {
        val configList: List<SearchTabConfig> by lazy {
            listOf(
                SearchTabConfig(R.string.tram,
                    listOf(RouteType.TRAM),
                    Constants.OrderBy.SEQ,
                    ThemeColor(R.color.colorTram, R.color.colorTramDark, R.color.colorTramAccent)
                )
            )
        }
    }

    init {
        autoSetTaskDescription = false
    }

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var pagerAdapter: TramSearchPagerAdapter? = null
    private lateinit var viewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Tram)
        window.navigationBarColor = Utils.getThemeColorPrimaryDark(this)
        super.setTaskDescription()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_nav)

        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)

        viewModel.configList = configList
        if (viewModel.selectedTabPosition == -1)
            viewModel.selectedTabPosition = 0 // TODO("Pass argument to Activity to set the default open tab")

        super.initNavigationDrawer()

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        pagerAdapter = TramSearchPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = pagerAdapter


        tabs.visibility = View.GONE
        fab.visibility = View.GONE

        subscribeUiChanges()
    }

    override fun onResume() {
        nav.menu.findItem(R.id.nav_tram).isChecked = true
        super.onResume()
    }

    private fun subscribeUiChanges() {
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_default_shortcut, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.menu_add_shortcut -> {
                createShortcut(
                    tag(),
                    R.string.sc_tram_s,
                    R.string.sc_tram_l,
                    R.drawable.ic_shortcut_tram,
                    newIntent<TramSearchActivity>(0)
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
