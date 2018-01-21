package hoo.etahk.view.route

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.mcxiaoke.koi.ext.Bundle
import com.mcxiaoke.koi.ext.startActivity
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Constants.Argument
import hoo.etahk.common.Constants.Company
import hoo.etahk.common.Constants.RouteType
import hoo.etahk.common.Utils
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.view.map.RoutesMapsActivity
import kotlinx.android.synthetic.main.activity_route.*

class RouteActivity : AppCompatActivity() {

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
                else -> R.style.AppTheme_Night
            }
        }
    }

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mRoutePagerAdapter: RoutePagerAdapter? = null
    private lateinit var mRouteViewModel: RouteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getTheme(intent.extras.getString(Argument.ARG_COMPANY), intent.extras.getLong(Argument.ARG_TYPE_CODE)))
        window.navigationBarColor = Utils.getThemeColorDark(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)

        setSupportActionBar(toolbar)

        mRouteViewModel = ViewModelProviders.of(this).get(RouteViewModel::class.java)
        mRouteViewModel.routeKey = RouteKey(intent.extras.getString(Argument.ARG_COMPANY), intent.extras.getString(Argument.ARG_ROUTE_NO), -1L, -1L)
        mRouteViewModel.period = Constants.SharePrefs.DEFAULT_ETA_AUTO_REFRESH

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mRoutePagerAdapter = RoutePagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mRoutePagerAdapter

        tabs.setupWithViewPager(container)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = mRouteViewModel.routeKey!!.getCompanyName() + " " + mRouteViewModel.routeKey!!.routeNo

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
        mRouteViewModel.getParentRoute().observe(this, Observer<Route> {
            it?.let {
                mRouteViewModel.updateChildRoutes(it)
                mRoutePagerAdapter?.dataSource = it
                setActionBarSubtitle(it)
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
                    putString(Argument.ARG_COMPANY, mRouteViewModel.routeKey?.company)
                    putString(Argument.ARG_ROUTE_NO, mRouteViewModel.routeKey?.routeNo)
                    putLong(Argument.ARG_TYPE_CODE, mRouteViewModel.routeKey?.typeCode?: RouteType.NONE)
                })
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
