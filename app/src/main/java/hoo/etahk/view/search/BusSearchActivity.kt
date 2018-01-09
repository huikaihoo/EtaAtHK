package hoo.etahk.view.search

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.ActivityManager
import android.arch.lifecycle.ViewModelProviders
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Constants.RouteType
import hoo.etahk.common.tools.ThemeColor
import kotlinx.android.synthetic.main.activity_search_nav_tab.*
import kotlinx.android.synthetic.main.activity_search_tab.*

class BusSearchActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        private const val TAG = "BusSearchActivity"
        private const val ARG_SELECTED_TAB = "selected_tab"

        val availableIndices = listOf(0, 1, 2, 3, 4, 5)

        val searchList = listOf(
                listOf(RouteType.NONE),
                listOf(RouteType.BUS_KL_NT, RouteType.BUS_KL_NT_NIGHT),
                listOf(RouteType.BUS_HKI, RouteType.BUS_HKI_NIGHT),
                listOf(RouteType.BUS_CROSS_HARBOUR, RouteType.BUS_CROSS_HARBOUR_NIGHT),
                listOf(RouteType.BUS_AIRPORT_LANTAU, RouteType.BUS_AIRPORT_LANTAU_NIGHT),
                listOf(RouteType.BUS_KL_NT_NIGHT, RouteType.BUS_HKI_NIGHT, RouteType.BUS_CROSS_HARBOUR_NIGHT, RouteType.BUS_AIRPORT_LANTAU_NIGHT))

        val colorList = listOf(
                ThemeColor(R.color.colorAll, R.color.colorAllDark, R.color.colorAllAccent),
                ThemeColor(R.color.colorKmb, R.color.colorKmbDark, R.color.colorKmbAccent),
                ThemeColor(R.color.colorHki, R.color.colorHkiDark, R.color.colorHkiAccent),
                ThemeColor(R.color.colorCrossHarbour, R.color.colorCrossHarbourDark, R.color.colorCrossHarbourAccent),
                ThemeColor(R.color.colorNlb, R.color.colorNlbDark, R.color.colorNlbAccent),
                ThemeColor(R.color.colorNight, R.color.colorNightDark, R.color.colorNightAccent))

        val titleList = listOf(
                R.string.all,
                R.string.kln_nt,
                R.string.hki,
                R.string.cross_harbour,
                R.string.airport_lantau,
                R.string.overnight)
    }
    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mBusSearchPagerAdapter: BusSearchPagerAdapter? = null
    private lateinit var mBusSearchViewModel: BusSearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_nav_tab)

        setSupportActionBar(toolbar)

        mBusSearchViewModel = ViewModelProviders.of(this).get(BusSearchViewModel::class.java)
        mBusSearchViewModel.updateParentRoutes()

        if (mBusSearchViewModel.selectedTabPosition == -1)
            mBusSearchViewModel.selectedTabPosition = 0 // TODO(" Pass argument to Activity to set the default open tab")
        changeColor(mBusSearchViewModel.selectedTabPosition, mBusSearchViewModel.selectedTabPosition)

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mBusSearchPagerAdapter = BusSearchPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mBusSearchPagerAdapter

        tabs.setupWithViewPager(container)
        tabs.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(container) {
            override fun onTabSelected(tab: TabLayout.Tab) {
                super.onTabSelected(tab)

                val oldTabPosition = mBusSearchViewModel.selectedTabPosition
                val newTabPosition = tab.position

                if (oldTabPosition in 0..searchList.size) {
                    changeColor(oldTabPosition, newTabPosition)
                }

                mBusSearchViewModel.selectedTabPosition = newTabPosition
            }
        })
        tabs.getTabAt(mBusSearchViewModel.selectedTabPosition)?.select()

        supportActionBar?.title = getString(R.string.title_bus_search)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own actionXX", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = object: ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if (slideOffset == 0f) {
                    // drawer closed: Disable status bar translucence
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    changeColor(mBusSearchViewModel.selectedTabPosition, mBusSearchViewModel.selectedTabPosition)
                } else if (slideOffset != 0f) {
                    // started opening: Enable status bar translucency
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

                }
                super.onDrawerSlide(drawerView, slideOffset)
            }
        }
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        nav.setNavigationItemSelectedListener(this)

        subscribeUiChanges()
    }

    private fun subscribeUiChanges() {
//        mBusSearchViewModel?.getFollowStops()?.observe(this, Observer<List<Stop>> { stops ->
//            Snackbar.make(main_content, "Model Updated" + stops?.size, Snackbar.LENGTH_LONG)
//        .setAction("Action", null).show() } )
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        when (item.itemId) {
            R.id.action_settings ->
                return true
            else ->
                return super.onOptionsItemSelected(item)
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

        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun changeColor(from: Int, to: Int) {
        when (to in 0..searchList.size) {
            true -> {
                // Change Task list color
                setTaskDescription(ActivityManager.TaskDescription(null, null, colorList[to].colorPrimaryDark))
                when (from in 0..searchList.size || from == to) {
                    true -> {
                        /**
                         * Source: https://stackoverflow.com/questions/28547820/how-can-i-change-the-action-bar-colour-with-animation
                         */
                        // Index changes > change color with animation
                        val colorAnimations = listOf(
                                ValueAnimator.ofObject(ArgbEvaluator(), colorList[from].colorPrimary, colorList[to].colorPrimary),
                                ValueAnimator.ofObject(ArgbEvaluator(), colorList[from].colorPrimaryDark, colorList[to].colorPrimaryDark),
                                ValueAnimator.ofObject(ArgbEvaluator(), colorList[from].colorPrimaryAccent, colorList[to].colorPrimaryAccent)
                        )

                        colorAnimations[0].addUpdateListener { animator ->
                            appbar.setBackgroundColor(animator.animatedValue as Int)
                            toolbar.setBackgroundColor(animator.animatedValue as Int)
                            //drawer.setStatusBarBackgroundColor(animator.animatedValue as Int)
                        }

                        colorAnimations[1].addUpdateListener { animator ->
                            window.statusBarColor = animator.animatedValue as Int
                        }

                        colorAnimations[2].addUpdateListener { animator ->
                            fab.backgroundTintList = ColorStateList.valueOf(animator.animatedValue as Int)
                        }

                        colorAnimations.forEach {
                            it.duration = Constants.Time.ANIMATION_TIME
                            it.startDelay = 0
                            it.start()
                        }


                    }
                    false -> {
                        // Invalid from index / No index changes > change color without animation
                        appbar.setBackgroundColor(colorList[to].colorPrimary)
                        toolbar.setBackgroundColor(colorList[to].colorPrimary)
                        window.statusBarColor = colorList[to].colorPrimaryDark
                    }
                }
            }
            // Invalid to index > return
            false -> return
        }
    }
}
