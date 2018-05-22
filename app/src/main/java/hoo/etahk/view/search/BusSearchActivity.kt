package hoo.etahk.view.search

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.PendingIntent
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.widget.SearchView
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Constants.OrderBy
import hoo.etahk.common.Constants.RouteType
import hoo.etahk.common.tools.ThemeColor
import hoo.etahk.view.base.NavActivity
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.activity_search_nav.*

class BusSearchActivity : NavActivity() {

    companion object {
        private const val TAG = "BusSearchActivity"
        private const val ARG_SELECTED_TAB = "selected_tab"

        val availableIndices = listOf(0, 1, 2, 3, 4, 5)

        val searchList = listOf(
                // ALL
                BusRoutesConfig(R.string.all,
                        listOf(RouteType.BUS_KL_NT, RouteType.BUS_KL_NT_NIGHT,
                                RouteType.BUS_HKI, RouteType.BUS_HKI_NIGHT,
                                RouteType.BUS_CROSS_HARBOUR, RouteType.BUS_CROSS_HARBOUR_NIGHT,
                                RouteType.BUS_AIRPORT_LANTAU, RouteType.BUS_AIRPORT_LANTAU_NIGHT),
                        OrderBy.BUS,
                        ThemeColor(R.color.colorAll, R.color.colorAllDark, R.color.colorAllAccent)),
                // KLN / NT
                BusRoutesConfig(R.string.kln_nt,
                        listOf(RouteType.BUS_KL_NT, RouteType.BUS_KL_NT_NIGHT),
                        OrderBy.TYPE_SEQ,
                        ThemeColor(R.color.colorKmb, R.color.colorKmbDark, R.color.colorKmbAccent)),
                // HK Island
                BusRoutesConfig(R.string.hki,
                        listOf(RouteType.BUS_HKI, RouteType.BUS_HKI_NIGHT),
                        OrderBy.TYPE_SEQ,
                        ThemeColor(R.color.colorHki, R.color.colorHkiDark, R.color.colorHkiAccent)),
                // Cross Harbour
                BusRoutesConfig(R.string.cross_harbour,
                        listOf(RouteType.BUS_CROSS_HARBOUR, RouteType.BUS_CROSS_HARBOUR_NIGHT),
                        OrderBy.TYPE_SEQ,
                        ThemeColor(R.color.colorCrossHarbour, R.color.colorCrossHarbourDark, R.color.colorCrossHarbourAccent)),
                // Lantau + Airport
                BusRoutesConfig(R.string.airport_lantau,
                        listOf(RouteType.BUS_AIRPORT_LANTAU, RouteType.BUS_AIRPORT_LANTAU_NIGHT),
                        OrderBy.TYPE_SEQ,
                        ThemeColor(R.color.colorNlb, R.color.colorNlbDark, R.color.colorNlbAccent)),
                // Overnight
                BusRoutesConfig(R.string.overnight,
                        listOf(RouteType.BUS_KL_NT_NIGHT, RouteType.BUS_HKI_NIGHT, RouteType.BUS_CROSS_HARBOUR_NIGHT, RouteType.BUS_AIRPORT_LANTAU_NIGHT),
                        OrderBy.SEQ,
                        ThemeColor(R.color.colorNight, R.color.colorNightDark, R.color.colorNightAccent))
        )
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
    private var pagerAdapter: BusSearchPagerAdapter? = null
    private lateinit var viewModel: BusSearchViewModel

    private var searchMenuItem: MenuItem? = null
    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_nav)

        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(BusSearchViewModel::class.java)
        viewModel.updateParentRoutes()

        if (viewModel.selectedTabPosition == -1)
            viewModel.selectedTabPosition = 0 // TODO(" Pass argument to Activity to set the default open tab")
        changeColor(viewModel.selectedTabPosition, viewModel.selectedTabPosition)

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        pagerAdapter = BusSearchPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = pagerAdapter
        container.offscreenPageLimit = searchList.size

        tabs.setupWithViewPager(container)
        tabs.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(container) {
            override fun onTabSelected(tab: TabLayout.Tab) {
                super.onTabSelected(tab)

                val oldTabPosition = viewModel.selectedTabPosition
                val newTabPosition = tab.position

                if (oldTabPosition in 0..searchList.size) {
                    changeColor(oldTabPosition, newTabPosition)
                }

                viewModel.selectedTabPosition = newTabPosition
            }
        })
        tabs.getTabAt(viewModel.selectedTabPosition)?.select()

        supportActionBar?.title = getString(R.string.title_bus_search)

        fab.setOnClickListener {
            appbar.setExpanded(true, true)
            searchMenuItem?.expandActionView()
            searchView?.requestFocus()
        }

        super.initNavigationDrawer()

        subscribeUiChanges()
    }

    override fun onResume() {
        nav.menu.findItem(R.id.nav_bus).isChecked = true
        super.onResume()
    }

    private fun subscribeUiChanges() {
//        viewModel?.getFollowStops()?.observe(this, Observer<List<Stop>> { stops ->
//            Snackbar.make(main_content, "Model Updated" + stops?.size, Snackbar.LENGTH_LONG)
//        .setAction("Action", null).show() } )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_search, menu)

        // Set up SearchMenuItem
        searchMenuItem = menu.findItem(R.id.menu_search)

        searchMenuItem?.isVisible = false
        searchMenuItem?.setOnActionExpandListener( object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                fab.hide()
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                fab.show()
                return true
            }
        })

        searchView = searchMenuItem?.actionView as SearchView
        searchView?.inputType = (InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS or
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            /**
             * Called when the user submits the query. This could be due to a key press on the
             * keyboard or due to pressing a submit button.
             * The listener can override the standard behavior by returning true
             * to indicate that it has handled the submit request. Otherwise return false to
             * let the SearchView handle the submission by launching any associated intent.
             *
             * @param query the query text that is to be submitted
             *
             * @return true if the query has been handled by the listener, false to let the
             * SearchView perform the default action.
             */
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            /**
             * Called when the query text is changed by the user.
             *
             * @param newText the new content of the query text field.
             *
             * @return false if the SearchView should perform the default action of showing any
             * suggestions if available, true if the action was handled by the listener.
             */
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchText.value = newText?.trim()?.toUpperCase()
                return false
            }

        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.menu_add_shortcut -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val shortcutManager = getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager

                    //check if device supports Pin Shortcut or not
                    if (shortcutManager.isRequestPinShortcutSupported) {
                        // Assumes there's already a shortcut with the ID "open_website".
                        // The shortcut must be enabled.
                        val pinShortcutInfo = ShortcutInfo.Builder(this, "bus").build()

                        // Create the PendingIntent object only if your app needs to be notified
                        // that the user allowed the shortcut to be pinned. Note that, if the
                        // pinning operation fails, your app isn't notified. We assume here that the
                        // app has implemented a method called createShortcutResultIntent() that
                        // returns a broadcast intent.
                        val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo)

                        // Configure the intent so that your app's broadcast receiver gets
                        // the callback successfully.
                        val successCallback = PendingIntent.getBroadcast(this, 0, pinnedShortcutCallbackIntent, 0)

                        //finally ask user to add the shortcut to home screen
                        shortcutManager.requestPinShortcut(
                            pinShortcutInfo,
                            successCallback.intentSender
                        )
                    }
                }
                true
            }
            R.id.menu_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun changeColor(from: Int, to: Int) {
        when (to in 0..searchList.size) {
            true -> {
                // Change Task list color
                super.setTaskDescription(searchList[to].color.colorPrimaryDark)
                when (from in 0..searchList.size || from == to) {
                    true -> {
                        /**
                         * Source: https://stackoverflow.com/questions/28547820/how-can-i-change-the-action-bar-colour-with-animation
                         */
                        // Index changes > change color with animation
                        val colorAnimations = listOf(
                                ValueAnimator.ofObject(ArgbEvaluator(), searchList[from].color.colorPrimary, searchList[to].color.colorPrimary),
                                ValueAnimator.ofObject(ArgbEvaluator(), searchList[from].color.colorPrimaryDark, searchList[to].color.colorPrimaryDark),
                                ValueAnimator.ofObject(ArgbEvaluator(), searchList[from].color.colorPrimaryAccent, searchList[to].color.colorPrimaryAccent)
                        )

                        colorAnimations[0].addUpdateListener { animator ->
                            appbar.setBackgroundColor(animator.animatedValue as Int)
                            toolbar.setBackgroundColor(animator.animatedValue as Int)
                            //drawer.setStatusBarBackgroundColor(animator.animatedValue as Int)
                        }

                        colorAnimations[1].addUpdateListener { animator ->
                            window.statusBarColor = animator.animatedValue as Int
                            window.navigationBarColor = animator.animatedValue as Int
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
                        appbar.setBackgroundColor(searchList[to].color.colorPrimary)
                        toolbar.setBackgroundColor(searchList[to].color.colorPrimary)
                        window.statusBarColor = searchList[to].color.colorPrimaryDark
                    }
                }
            }
            // Invalid to index > return
            false -> return
        }
    }
}
