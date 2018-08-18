package hoo.etahk.view.route

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.Menu
import android.view.MenuItem
import com.mcxiaoke.koi.ext.Bundle
import com.mcxiaoke.koi.ext.newIntent
import com.mcxiaoke.koi.ext.startActivity
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Constants.Argument
import hoo.etahk.common.Constants.Company
import hoo.etahk.common.Constants.RouteType
import hoo.etahk.common.Utils
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.view.base.BaseActivity
import hoo.etahk.view.map.RoutesMapsActivity
import kotlinx.android.synthetic.main.activity_route.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class RouteActivity : BaseActivity() {

    companion object {
        private const val TAG = "RouteActivity"

        fun getTheme(company: String, typeCode: Long): Int {
            val isOvernight = (typeCode / RouteType.BUS_NIGHT * RouteType.BUS_NIGHT) == RouteType.BUS_NIGHT
            return when {
                isOvernight -> R.style.AppTheme_Night
                company == Company.KMB -> R.style.AppTheme_Kmb
                company == Company.LWB -> R.style.AppTheme_Lwb
                company == Company.NWFB -> R.style.AppTheme_Nwfb
                company == Company.CTB -> R.style.AppTheme_Ctb
                company == Company.NLB -> R.style.AppTheme_Nlb
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
        setTheme(getTheme(intent.extras.getString(Argument.ARG_COMPANY), intent.extras.getLong(Argument.ARG_TYPE_CODE)))
        window.navigationBarColor = Utils.getThemeColorPrimaryDark(this)
        super.setTaskDescription()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)

        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(RouteViewModel::class.java)
        viewModel.routeKey = RouteKey(intent.extras.getString(Argument.ARG_COMPANY), intent.extras.getString(Argument.ARG_ROUTE_NO), -1L, -1L)
        viewModel.durationInMillis = Constants.SharePrefs.DEFAULT_ETA_AUTO_REFRESH * Constants.Time.ONE_SECOND_IN_MILLIS

        intent.extras.putLong(Argument.ARG_GOTO_BOUND, -1L)
        intent.extras.putLong(Argument.ARG_GOTO_SEQ, -1L)

        // Setup Fragment
        pagerAdapter = RoutePagerAdapter(supportFragmentManager)
        container.adapter = pagerAdapter
        container.offscreenPageLimit = 3

        tabs.setupWithViewPager(container)

        // Setup Actionbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = viewModel.routeKey!!.getCompanyName() + " " + viewModel.routeKey!!.routeNo

        // Setup Progressbar
        progress_bar.max = viewModel.durationInMillis.toInt()
        progress_bar.progress = 0


        subscribeUiChanges()
    }

    private fun setActionBarSubtitle(route: Route) {
        if (supportActionBar?.subtitle.isNullOrBlank() ) {
            val directionArrow = getString(
                    when (route.direction) {
                        0L -> R.string.arrow_circular
                        1L -> R.string.arrow_one_way
                        else -> R.string.arrow_two_ways
                    })

            supportActionBar?.subtitle = route.from.value + directionArrow + route.to.value
        }
    }

    private fun subscribeUiChanges() {
        viewModel.getParentRoute().observe(this, Observer<Route> {
            it?.let {
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
                    val gotoBound = intent.extras.getLong(Argument.ARG_GOTO_BOUND)
                    if (gotoBound > 0) {
                        viewModel.isGotoBoundUsed = true
                        viewModel.selectedTabPosition = if (gotoBound >= 2L) 1 else 0
                    }
                }
                tabs.getTabAt(viewModel.selectedTabPosition)?.select()
            }
        })

        viewModel.getMillisLeft().observe(this, Observer<Long> {
            it?.let {
                launch(UI){
                    progress_bar.progress = it.toInt()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_route, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.menu_maps -> {
                startActivity<RoutesMapsActivity>(Bundle {
                    putString(Argument.ARG_COMPANY, viewModel.routeKey?.company)
                    putString(Argument.ARG_ROUTE_NO, viewModel.routeKey?.routeNo)
                    putLong(Argument.ARG_TYPE_CODE, viewModel.routeKey?.typeCode?: RouteType.NONE)
                })
                true
            }
            R.id.menu_add_shortcut -> {
                val intent = newIntent<RouteActivity>(0, Bundle {
                    putString(Constants.Argument.ARG_COMPANY, viewModel.routeKey?.company)
                    putString(Constants.Argument.ARG_ROUTE_NO, viewModel.routeKey?.routeNo)
                    putLong(Constants.Argument.ARG_TYPE_CODE, viewModel.routeKey?.typeCode?: RouteType.NONE)
                    putLong(Constants.Argument.ARG_GOTO_BOUND, -1L)
                    putLong(Constants.Argument.ARG_GOTO_SEQ, -1L)
                })

                val theme = getTheme(viewModel.routeKey?.company?: "", viewModel.routeKey?.typeCode?: RouteType.NONE)
                val shortcutResId = when (theme) {
                    R.style.AppTheme_Kmb -> R.drawable.ic_shortcut_bus_kmb
                    R.style.AppTheme_Lwb -> R.drawable.ic_shortcut_bus_lwb
                    R.style.AppTheme_Nwfb  -> R.drawable.ic_shortcut_bus_nwfb
                    R.style.AppTheme_Ctb -> R.drawable.ic_shortcut_bus_ctb
                    R.style.AppTheme_Nlb -> R.drawable.ic_shortcut_bus_nlb
                    else -> R.drawable.ic_shortcut_bus_night
                }

                Utils.createShortcut(
                    this,
                    TAG + "_" + viewModel.routeKey?.routeStr,
                    viewModel.routeKey!!.getCompanyName() + " " + viewModel.routeKey!!.routeNo,
                    shortcutResId,
                    intent
                )
                true
            }
            R.id.menu_settings -> true
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
