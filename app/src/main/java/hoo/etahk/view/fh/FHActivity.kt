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
import hoo.etahk.R
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
        bnv.setOnNavigationItemSelectedListener { item ->
            viewModel.currentItemId = item.itemId
            showContent()
        }

        subscribeUiChanges()
    }

    private fun showContent(): Boolean {
        return when (viewModel.currentItemId) {
            R.id.nav_favourite -> {
                toolbar.title = getString(R.string.title_favourite)
                recycler_view.adapter = favouriteAdapter
                true
            }
            R.id.nav_history -> {
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
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.menu_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
