package hoo.etahk.view.route

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Constants.Company
import hoo.etahk.common.Constants.RouteType
import hoo.etahk.common.Utils
import hoo.etahk.common.constants.Argument
import hoo.etahk.common.constants.SharedPrefs
import hoo.etahk.common.extensions.createShortcut
import hoo.etahk.common.extensions.extras
import hoo.etahk.common.extensions.getExtra
import hoo.etahk.common.extensions.startCustomTabs
import hoo.etahk.common.extensions.tag
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.view.base.BaseActivity
import hoo.etahk.view.map.RoutesMapsActivity
import kotlinx.android.synthetic.main.activity_container_tabs_progress.container
import kotlinx.android.synthetic.main.activity_container_tabs_progress.progress_bar
import kotlinx.android.synthetic.main.activity_container_tabs_progress.tabs
import kotlinx.android.synthetic.main.activity_container_tabs_progress.toolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity

class RouteActivity : BaseActivity() {

    companion object {
        fun getTheme(company: String, typeCode: Long): Int {
            val isOvernight = (typeCode / RouteType.BUS_NIGHT * RouteType.BUS_NIGHT) == RouteType.BUS_NIGHT
            return when {
                isOvernight -> R.style.AppTheme_Night
                company == Company.KMB -> R.style.AppTheme_Kmb
                company == Company.LWB -> R.style.AppTheme_Lwb
                company == Company.NWFB -> R.style.AppTheme_Nwfb
                company == Company.CTB -> R.style.AppTheme_Ctb
                company == Company.NLB -> R.style.AppTheme_Nlb
                company == Company.MTRB -> R.style.AppTheme_Mtrb
                company == Company.TRAM -> R.style.AppTheme_Tram
                else -> R.style.AppTheme_Night
            }
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
    private var pagerAdapter: RoutePagerAdapter? = null
    private lateinit var viewModel: RouteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getTheme(getExtra(Argument.ARG_COMPANY), getExtra(Argument.ARG_TYPE_CODE)))
        window.navigationBarColor = Utils.getThemeColorPrimaryDark(this)
        super.setTaskDescription()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_tabs_progress)

        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(RouteViewModel::class.java)
        viewModel.routeKey = RouteKey(getExtra(Argument.ARG_COMPANY), getExtra(
            Argument.ARG_ROUTE_NO), -1L, -1L)
        viewModel.anotherCompany = getExtra(Argument.ARG_ANOTHER_COMPANY)
        viewModel.durationInMillis = SharedPrefs.DEFAULT_ETA_AUTO_REFRESH * Constants.Time.ONE_SECOND_IN_MILLIS

        extras.putLong(Argument.ARG_GOTO_BOUND, -1L)
        extras.putLong(Argument.ARG_GOTO_SEQ, -1L)

        // Setup Fragment
        pagerAdapter = RoutePagerAdapter(supportFragmentManager)
        container.adapter = pagerAdapter
        container.offscreenPageLimit = 3

        tabs.setupWithViewPager(container)

        // Setup Actionbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = viewModel.routeKey!!.getCompanyName() + " " + viewModel.routeKey!!.getRouteNoDisplay()

        // Setup Progressbar
        progress_bar.max = viewModel.durationInMillis.toInt()
        progress_bar.progress = 0

        subscribeUiChanges()
    }

    private fun setActionBarSubtitle(route: Route) {
        if (supportActionBar?.subtitle.isNullOrBlank() ) {
            supportActionBar?.subtitle = route.from.value + route.getDirectionArrow() + route.to.value
        }
    }

    private fun subscribeUiChanges() {
        viewModel.getParentRoute().observe(this, Observer<Route> {
            viewModel.updateChildRoutes(it)
            pagerAdapter?.dataSource = it
            setActionBarSubtitle(it)
            tabs.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(container) {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    super.onTabSelected(tab)
                    if (tabs.tabCount > 0)
                        viewModel.selectedTabPosition = tab.position
                }
            })
            if (!viewModel.isGotoBoundUsed) {
                val gotoBound = getExtra<Long>(Argument.ARG_GOTO_BOUND)
                if (gotoBound > 0) {
                    viewModel.isGotoBoundUsed = true
                    viewModel.selectedTabPosition = if (gotoBound >= 2L) 1 else 0
                }
            }
            tabs.getTabAt(viewModel.selectedTabPosition)?.select()
        })

        viewModel.getMillisLeft().observe(this, Observer<Long> {
            GlobalScope.launch(Dispatchers.Main){
                progress_bar.progress = it.toInt()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_route, menu)

        if (getExtra<String>(Argument.ARG_ANOTHER_COMPANY).isNotBlank()) {
            menu.findItem(R.id.menu_another_company).isVisible = true
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.menu_maps -> {
                startActivity<RoutesMapsActivity>(
                    Argument.ARG_COMPANY to viewModel.routeKey?.company,
                    Argument.ARG_ROUTE_NO to viewModel.routeKey?.routeNo,
                    Argument.ARG_GOTO_BOUND to (container.currentItem + 1).toLong()
                )
                true
            }
            R.id.menu_timetable -> {
                val url = viewModel.getTimetableUrl(
                    viewModel.routeKey?.company ?: "",
                    viewModel.routeKey?.routeNo ?: "",
                    (container.currentItem + 1).toLong(),
                    1L
                )

                if (url.isNotBlank()) {
                    startCustomTabs(url)
                } else {
                    startActivity<TimetableActivity>(
                        Argument.ARG_COMPANY to viewModel.routeKey?.company,
                        Argument.ARG_ROUTE_NO to viewModel.routeKey?.routeNo,
                        Argument.ARG_TYPE_CODE to (viewModel.routeKey?.typeCode?: RouteType.NONE),
                        Argument.ARG_GOTO_BOUND to (container.currentItem + 1).toLong()
                    )
                }
                true
            }
            R.id.menu_another_company -> {
                startActivity<RouteActivity>(
                    Argument.ARG_COMPANY to viewModel.anotherCompany,
                    Argument.ARG_ROUTE_NO to viewModel.routeKey?.routeNo,
                    Argument.ARG_TYPE_CODE to (viewModel.routeKey?.typeCode?: RouteType.NONE),
                    Argument.ARG_ANOTHER_COMPANY to viewModel.routeKey?.company,
                    Argument.ARG_GOTO_BOUND to -1L,
                    Argument.ARG_GOTO_SEQ to -1L
                )
                true
            }
            R.id.menu_add_favourite -> {
                viewModel.insertRouteFavourite()
                Snackbar.make(container, R.string.msg_add_to_favourite_success, Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.menu_add_shortcut -> {
                val intent = intentFor<RouteActivity>(
                    Argument.ARG_COMPANY to viewModel.routeKey?.company,
                    Argument.ARG_ROUTE_NO to viewModel.routeKey?.getRouteNoDisplay(),
                    Argument.ARG_TYPE_CODE to (viewModel.routeKey?.typeCode?: RouteType.NONE),
                    Argument.ARG_ANOTHER_COMPANY to viewModel.anotherCompany,
                    Argument.ARG_GOTO_BOUND to -1L,
                    Argument.ARG_GOTO_SEQ to -1L
                )

                val shortcutResId = when ( getTheme(viewModel.routeKey?.company?: "", viewModel.routeKey?.typeCode?: RouteType.NONE) ) {
                    R.style.AppTheme_Kmb -> R.drawable.ic_shortcut_bus_kmb
                    R.style.AppTheme_Lwb -> R.drawable.ic_shortcut_bus_lwb
                    R.style.AppTheme_Nwfb  -> R.drawable.ic_shortcut_bus_nwfb
                    R.style.AppTheme_Ctb -> R.drawable.ic_shortcut_bus_ctb
                    R.style.AppTheme_Nlb -> R.drawable.ic_shortcut_bus_nlb
                    else -> R.drawable.ic_shortcut_bus_night
                }

                createShortcut(
                    tag() + "_" + viewModel.routeKey?.routeStr,
                    viewModel.routeKey!!.getCompanyName() + " " + viewModel.routeKey!!.routeNo,
                    shortcutResId,
                    intent
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
