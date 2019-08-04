package hoo.etahk.view.fh

import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mcxiaoke.koi.ext.Bundle
import com.mcxiaoke.koi.ext.newIntent
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.constants.Argument
import hoo.etahk.common.constants.SharePrefs
import hoo.etahk.common.extensions.createShortcut
import hoo.etahk.common.extensions.getExtra
import hoo.etahk.common.extensions.tag
import hoo.etahk.common.helper.SharedPrefsHelper
import hoo.etahk.model.data.Route
import hoo.etahk.model.misc.BaseMisc
import hoo.etahk.model.relation.RouteFavouriteEx
import hoo.etahk.model.relation.RouteHistoryEx
import hoo.etahk.view.base.NavActivity
import hoo.etahk.view.route.RouteActivity
import kotlinx.android.synthetic.main.activity_fh.recycler_view
import kotlinx.android.synthetic.main.activity_fh.toolbar
import kotlinx.android.synthetic.main.activity_fh_nav.bnv
import kotlinx.android.synthetic.main.activity_fh_nav.nav
import org.jetbrains.anko.startActivity

class FHActivity : NavActivity() {

    private var favouriteAdapter: FavouriteAdapter = FavouriteAdapter()
    private var historyAdapter: HistoryAdapter = HistoryAdapter()
    private lateinit var viewModel: FHViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fh_nav)

        setSupportActionBar(toolbar)

        favouriteAdapter.context = this
        historyAdapter.context = this

        viewModel = ViewModelProviders.of(this).get(FHViewModel::class.java)
        viewModel.currentType = Constants.MiscType.valueOf(getExtra(Argument.ARG_MISC_TYPE))

        super.initNavigationDrawer()

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.itemAnimator = DefaultItemAnimator()
        recycler_view.addItemDecoration(
            DividerItemDecoration(
                this,
                (recycler_view.layoutManager as LinearLayoutManager).orientation
            )
        )

        showContent()

        bnv.selectedItemId = if (viewModel.currentType == Constants.MiscType.ROUTE_HISTORY) R.id.nav_history else R.id.nav_favourite
        bnv.setOnNavigationItemSelectedListener { item ->
            viewModel.currentType = when (item.itemId) {
                R.id.nav_favourite -> Constants.MiscType.ROUTE_FAVOURITE
                R.id.nav_history -> Constants.MiscType.ROUTE_HISTORY
                else -> Constants.MiscType.NONE
            }
            showContent()
        }

        subscribeUiChanges()
    }

    private fun showContent(): Boolean {
        return when (viewModel.currentType) {
            Constants.MiscType.ROUTE_FAVOURITE -> {
                toolbar.title = getString(R.string.title_favourite)
                recycler_view.adapter = favouriteAdapter
                true
            }
            Constants.MiscType.ROUTE_HISTORY -> {
                toolbar.title = getString(R.string.title_history)
                recycler_view.adapter = historyAdapter
                true
            }
            else -> false
        }
    }

    fun showCompaniesPopupMenu(view: View, route: Route) {
        val popup = PopupMenu(this, view, Gravity.END)

        popup.inflate(R.menu.popup_bus_jointly)

        popup.menu.findItem(R.id.popup_company_kmb_lwb).title = getString(
            R.string.view_company_route,
            Utils.getStringResourceByName(route.routeKey.company.toLowerCase()),
            route.routeKey.routeNo
        )
        popup.menu.findItem(R.id.popup_company_nwfb_ctb).title = getString(
            R.string.view_company_route,
            Utils.getStringResourceByName(route.anotherCompany.toLowerCase()),
            route.routeKey.routeNo
        )

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.popup_company_kmb_lwb -> {
                    startActivity<RouteActivity>(
                        Argument.ARG_COMPANY to route.routeKey.company,
                        Argument.ARG_ROUTE_NO to route.routeKey.routeNo,
                        Argument.ARG_TYPE_CODE to route.routeKey.typeCode,
                        Argument.ARG_ANOTHER_COMPANY to route.anotherCompany,
                        Argument.ARG_GOTO_BOUND to -1L,
                        Argument.ARG_GOTO_SEQ to -1L
                    )
                }
                R.id.popup_company_nwfb_ctb -> {
                    startActivity<RouteActivity>(
                        Argument.ARG_COMPANY to route.anotherCompany,
                        Argument.ARG_ROUTE_NO to route.routeKey.routeNo,
                        Argument.ARG_TYPE_CODE to route.routeKey.typeCode,
                        Argument.ARG_ANOTHER_COMPANY to route.routeKey.company,
                        Argument.ARG_GOTO_BOUND to -1L,
                        Argument.ARG_GOTO_SEQ to -1L
                    )
                }
            }
            true
        }
        popup.show()
    }

    fun showRoutePopupMenu(view: View, route: Route, misc: BaseMisc) {
        val pref = SharedPrefsHelper.get(R.string.pref_bus_jointly, SharePrefs.BUS_JOINTLY_ALWAYS_ASK)
        val companyDetailsByPref = route.companyDetailsByPref

        val popup = PopupMenu(this, view, Gravity.END)

        popup.inflate(R.menu.popup_fh)

        val viewItem = popup.menu.findItem(R.id.popup_view)
        if (route.companyDetails.size <= 1 || pref == SharePrefs.BUS_JOINTLY_ALWAYS_ASK) {
            viewItem.isVisible = false
        } else {
            viewItem.title = getString(
                R.string.view_company_route,
                Utils.getStringResourceByName(companyDetailsByPref[1].toLowerCase()),
                route.routeKey.routeNo
            )
        }

        if (viewModel.currentType == Constants.MiscType.ROUTE_FAVOURITE) {
            popup.menu.findItem(R.id.popup_add_fav).isVisible = false
        }

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.popup_view -> {
                    startActivity<RouteActivity>(
                        Argument.ARG_COMPANY to companyDetailsByPref[1],
                        Argument.ARG_ROUTE_NO to route.routeKey.routeNo,
                        Argument.ARG_TYPE_CODE to route.routeKey.typeCode,
                        Argument.ARG_ANOTHER_COMPANY to companyDetailsByPref[0],
                        Argument.ARG_GOTO_BOUND to -1L,
                        Argument.ARG_GOTO_SEQ to -1L
                    )
                }
                R.id.popup_remove -> {
                    viewModel.deleteMisc(misc)
                    when (viewModel.currentType) {
                        Constants.MiscType.ROUTE_FAVOURITE -> {
                            Snackbar.make(view, R.string.msg_remove_from_favourite_success, Snackbar.LENGTH_SHORT).show()
                        }
                        Constants.MiscType.ROUTE_HISTORY -> {
                            Snackbar.make(view, R.string.msg_remove_from_history_success, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
                R.id.popup_add_fav -> {
                    viewModel.insertRouteFavourite(route)
                    Snackbar.make(view, R.string.msg_add_to_favourite_success, Snackbar.LENGTH_SHORT).show()
                }
            }
            true
        }
        popup.show()
    }

    private fun subscribeUiChanges() {
        viewModel.favouritePagedList?.observe(this, Observer<PagedList<RouteFavouriteEx>> {
            //logd(it.toString())
            favouriteAdapter.submitList(it)
        })

        viewModel.historyPagedList?.observe(this, Observer<PagedList<RouteHistoryEx>> {
            //logd(it.toString())
            historyAdapter.submitList(it)
        })
    }

    override fun onResume() {
        nav.menu.findItem(R.id.nav_fh).isChecked = true
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_fh, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.menu_add_shortcut -> {
                val intent = newIntent<FHActivity>(0, Bundle {
                    putString(Argument.ARG_MISC_TYPE, viewModel.currentType.toString())
                })

                when (viewModel.currentType) {
                    Constants.MiscType.ROUTE_FAVOURITE -> {
                        createShortcut(
                            tag() + "_" + viewModel.currentType.toString(),
                            R.string.sc_favourite_s,
                            R.string.sc_favourite_l,
                            R.drawable.ic_shortcut_favourite,
                            intent
                        )
                    }
                    Constants.MiscType.ROUTE_HISTORY -> {
                        createShortcut(
                            tag() + "_" + viewModel.currentType.toString(),
                            R.string.sc_favourite_s,
                            R.string.sc_favourite_l,
                            R.drawable.ic_shortcut_history,
                            intent
                        )
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
