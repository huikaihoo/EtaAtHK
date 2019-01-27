package hoo.etahk.view.fh

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mcxiaoke.koi.ext.Bundle
import com.mcxiaoke.koi.ext.newIntent
import getValue
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.extensions.createShortcut
import hoo.etahk.common.extensions.extras
import hoo.etahk.common.extensions.getExtra
import hoo.etahk.common.extensions.tag
import hoo.etahk.model.relation.RouteFavouriteEx
import hoo.etahk.model.relation.RouteHistoryEx
import hoo.etahk.view.base.NavActivity
import kotlinx.android.synthetic.main.activity_fh.*
import kotlinx.android.synthetic.main.activity_fh_nav.*

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
        viewModel.currentType = Constants.MiscType.valueOf(getExtra(Constants.Argument.ARG_MISC_TYPE))

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
                    putString(Constants.Argument.ARG_MISC_TYPE, viewModel.currentType.toString())
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
