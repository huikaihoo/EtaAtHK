package hoo.etahk.view.base

import android.annotation.SuppressLint
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.navigation.NavigationView
import com.mcxiaoke.koi.ext.find
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.startActivity
import hoo.etahk.view.App
import hoo.etahk.view.fh.FHActivity
import hoo.etahk.view.follow.FollowActivity
import hoo.etahk.view.search.BusSearchActivity
import hoo.etahk.view.settings.SettingsActivity
import kotlinx.android.synthetic.main.nav_header.view.*

abstract class NavActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var navViewModel: NavViewModel

    private val nav: NavigationView
        get() = find(R.id.nav)

    private val drawer: DrawerLayout
        get() = find(R.id.drawer)

    private val toolbar: Toolbar
        get() = find(R.id.toolbar)

    @SuppressLint("SetTextI18n")
    fun initNavigationDrawer() {
        val toggle = object: ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if (slideOffset == 0f) {
                    // drawer closed: Disable status bar translucence
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
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

        navViewModel = ViewModelProviders.of(this).get(NavViewModel::class.java)
        navViewModel.subscribeToRepo()

        val tv = nav.getHeaderView(0).nav_last_update

        if (tv.text.isBlank()) {
            val strDate = Utils.getDateTimeString(navViewModel.getLastUpdate().value)
            if (!strDate.isBlank()) {
                tv.text = App.instance.getString(R.string.last_update) + strDate
            }
        }

        navViewModel.getLastUpdate().observe(this, Observer<Long> {
            it?.let {
                val strDate = Utils.getDateTimeString(it)
                if (!strDate.isBlank()) {
                    tv?.text = App.instance.getString(R.string.last_update) + strDate
                } else {
                    tv?.text = ""
                }
            }
        })
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item The selected item
     *
     * @return true to display the item as the selected item
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_follow -> {
                startActivity<FollowActivity>(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            R.id.nav_fh -> {
                startActivity<FHActivity>(
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
                    Constants.Argument.ARG_MISC_TYPE to Constants.MiscType.ROUTE_FAVOURITE.toString()
                )
            }
            R.id.nav_bus -> {
                startActivity<BusSearchActivity>(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            R.id.nav_tram -> {

            }
            R.id.nav_mtr -> {

            }
            R.id.nav_settings -> {
                startActivity<SettingsActivity>(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
        }

        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}