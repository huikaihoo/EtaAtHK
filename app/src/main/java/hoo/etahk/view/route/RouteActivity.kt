package hoo.etahk.view.route

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import hoo.etahk.R
import hoo.etahk.common.Constants.Eta
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import kotlinx.android.synthetic.main.activity_route.*

class RouteActivity : AppCompatActivity() {

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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)

        setSupportActionBar(toolbar)

        mRouteViewModel = ViewModelProviders.of(this).get(RouteViewModel::class.java)
        mRouteViewModel.routeKey = RouteKey("KMB", "5M", -1, -1)
        mRouteViewModel.period = Eta.ETA_AUTO_REFRESH

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mRoutePagerAdapter = RoutePagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mRoutePagerAdapter

        tabs.setupWithViewPager(container)

        //container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        //tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        fab.setOnClickListener { view ->
            mRouteViewModel.insertRoutes()
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        subscribeUiChanges()
    }

    private fun subscribeUiChanges() {
        mRouteViewModel.getRoute().observe(this, Observer<Route> {
            it?.let { mRoutePagerAdapter?.dataSource = it }
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
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
