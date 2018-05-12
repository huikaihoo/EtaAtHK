package hoo.etahk.view.follow

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.model.relation.LocationAndGroups
import kotlinx.android.synthetic.main.activity_follow.*
import kotlinx.android.synthetic.main.activity_follow_nav.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class FollowActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        private const val TAG = "FollowActivity"
    }
    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private lateinit var spinnerAdapter: FollowSpinnerAdapter
    private lateinit var pagerAdapter: FollowPagerAdapter
    private lateinit var viewModel: FollowViewModel
    private var onTabSelectedListener: TabLayout.ViewPagerOnTabSelectedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_nav)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        viewModel = ViewModelProviders.of(this).get(FollowViewModel::class.java)
        viewModel.durationInMillis = Constants.SharePrefs.DEFAULT_ETA_AUTO_REFRESH * Constants.Time.ONE_SECOND_IN_MILLIS
        viewModel.enableSorting.value = false

        // Setup Spinner
        spinnerAdapter = FollowSpinnerAdapter(this)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val beforePosition = viewModel.selectedLocationPosition
                if (beforePosition != position) {
                    if (viewModel.keepSpinnerSelection) {
                        spinner.setSelection(viewModel.selectedLocationPosition)
                    } else {
                        viewModel.selectedLocationPosition = position
                        updateFragments(spinnerAdapter.dataSource[position])
                    }
                }
                viewModel.keepSpinnerSelection = false
            }

            override fun onNothingSelected(parent: AdapterView<out Adapter>?) {

            }
        }

        // Setup Fragment
        pagerAdapter = FollowPagerAdapter(supportFragmentManager)
        container.adapter = pagerAdapter
        tabs.setupWithViewPager(container)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        // Setup Progressbar
        progress_bar.max = viewModel.durationInMillis.toInt()
        progress_bar.progress = 0

        viewModel.initLocationsAndGroups()

        subscribeUiChanges()
    }

    private fun subscribeUiChanges() {
        viewModel.getFollowLocations().observe(this, Observer<List<LocationAndGroups>> {
            it?.let {
                viewModel.keepSpinnerSelection = true
                spinnerAdapter.dataSource = it

                if (viewModel.selectedLocationPosition >= it.size) {
                    viewModel.selectedLocationPosition = 0
                } else {
                    viewModel.selectedLocationPosition = viewModel.selectedLocationPosition
                    container.offscreenPageLimit = it.size
                }

                if (it.isNotEmpty())
                    updateFragments(it[viewModel.selectedLocationPosition])
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

    private fun updateFragments(locationAndGroups: LocationAndGroups) {
        onTabSelectedListener?.let { tabs.removeOnTabSelectedListener(it) }

        onTabSelectedListener = object : TabLayout.ViewPagerOnTabSelectedListener(container) {
            override fun onTabSelected(tab: TabLayout.Tab) {
                super.onTabSelected(tab)
                if (tabs.tabCount > 0)
                    locationAndGroups.selectedGroupPosition = tab.position
                Log.d(TAG, "S ${locationAndGroups.location.Id} ${locationAndGroups.selectedGroupPosition}")
            }
        }
        pagerAdapter.dataSource = locationAndGroups
        tabs.getTabAt(locationAndGroups.selectedGroupPosition)?.select()
        tabs.addOnTabSelectedListener(onTabSelectedListener!!)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_follow, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.menu_sort_items -> {
                item.isChecked = !item.isChecked
                viewModel.enableSorting.value = item.isChecked
                true
            }
            R.id.menu_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
