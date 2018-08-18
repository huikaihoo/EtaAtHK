package hoo.etahk.view.fh

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.mcxiaoke.koi.ext.Bundle
import com.mcxiaoke.koi.ext.newIntent
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.model.relation.RouteFavouriteEx
import hoo.etahk.model.relation.RouteHistoryEx
import hoo.etahk.view.base.NavActivity
import kotlinx.android.synthetic.main.activity_fh.*
import kotlinx.android.synthetic.main.activity_fh_nav.*

class FHActivity : NavActivity() {

    companion object {
        private const val TAG = "FHActivity"
    }

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
        viewModel.currentType = Constants.MiscType.valueOf(intent.extras.getString(Constants.Argument.ARG_MISC_TYPE))

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

    private fun subscribeUiChanges() {
        viewModel.favouritePagedList?.observe(this, Observer<PagedList<RouteFavouriteEx>> {
            //Log.d(TAG, it.toString())
            favouriteAdapter.submitList(it)
        })

        viewModel.historyPagedList?.observe(this, Observer<PagedList<RouteHistoryEx>> {
            //Log.d(TAG, it.toString())
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
                    putString(Constants.Argument.ARG_MISC_TYPE, viewModel.currentType.toString())
                })

                when (viewModel.currentType) {
                    Constants.MiscType.ROUTE_FAVOURITE -> {
                        Utils.createShortcut(
                            this,
                            TAG + "_" + viewModel.currentType.toString(),
                            R.string.sc_favourite_s,
                            R.string.sc_favourite_l,
                            R.drawable.ic_shortcut_favourite,
                            intent
                        )
                    }
                    Constants.MiscType.ROUTE_HISTORY -> {
                        Utils.createShortcut(
                            this,
                            TAG + "_" + viewModel.currentType.toString(),
                            R.string.sc_favourite_s,
                            R.string.sc_favourite_l,
                            R.drawable.ic_shortcut_history,
                            intent
                        )
                    }
                }
                true
            }
            R.id.menu_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
