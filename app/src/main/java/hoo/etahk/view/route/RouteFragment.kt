package hoo.etahk.view.route

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop
import hoo.etahk.model.repo.StopsRepo
import kotlinx.android.synthetic.main.fragment_route.view.*

class RouteFragment : Fragment() {

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private val ARG_BOUND = "bound"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(direction: Long): RouteFragment {
            val fragment = RouteFragment()
            val args = Bundle()
            args.putLong(ARG_BOUND, direction)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var mRootView: View
    private lateinit var mRouteViewModel: RouteViewModel
    private lateinit var mRouteFragmentViewModel: RouteFragmentViewModel
    private var mRouteStopsAdapter: RouteStopsAdapter = RouteStopsAdapter()
    private var mSubscribeStops = false
    private var mIgnoreTimer = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mRouteStopsAdapter.context = this

        mRouteViewModel = ViewModelProviders.of(activity!!).get(RouteViewModel::class.java)
        mRouteFragmentViewModel = ViewModelProviders.of(this).get(RouteFragmentViewModel::class.java)

        if (mRouteFragmentViewModel.routeKey == null)
            mRouteFragmentViewModel.routeKey = mRouteViewModel.routeKey?.copy(bound = arguments!!.getLong(ARG_BOUND))

        mRootView = inflater.inflate(R.layout.fragment_route, container, false)

        mRootView.recycler_view.layoutManager = LinearLayoutManager(activity)
        mRootView.recycler_view.adapter = mRouteStopsAdapter

        mRootView.refresh_layout.isRefreshing = true
        mRootView.refresh_layout.setOnRefreshListener {
            mRouteViewModel.period = 0
            mRouteViewModel.period = Constants.SharePrefs.DEFAULT_ETA_AUTO_REFRESH
        }

        subscribeUiChanges()

        return mRootView
    }

    fun updateEta(stops: List<Stop>) {
        mRootView.refresh_layout.isRefreshing = true
        mRouteFragmentViewModel.updateEta(stops)
    }

    private fun subscribeUiChanges() {
        mIgnoreTimer = true

        mRouteFragmentViewModel.getChildRoutes().observe(this, Observer<List<Route>> {
            it?.let {
                // TODO("Add Eta updater")
                if (!mRouteFragmentViewModel.hasGetStopsFromRemote && it.isNotEmpty()) {
                    mRouteFragmentViewModel.hasGetStopsFromRemote = true
                    StopsRepo.getStopsFromRemote(it[0], false)
                }
                if (!mSubscribeStops && it.isNotEmpty()) {
                    mSubscribeStops = true
                    mRouteFragmentViewModel.subscribeStopsToRepo()
                    subscribeStopsChanges()
                }
            }
        })

        mRouteViewModel.getLastUpdateTime().observe(this, Observer<Long> {
            if (it != null) {
                // Ignore the update for 1st time / on rotation
                if (mIgnoreTimer) {
                    mIgnoreTimer = false
                } else {
                    //Log.d("XXX", "time = $it")
                    mRootView.refresh_layout.isRefreshing = true
                    mRouteFragmentViewModel.updateAllEta(it)
                }
            }
        })
    }

    private fun subscribeStopsChanges() {
        mRouteFragmentViewModel.getStops().observe(this, Observer<List<Stop>> {
            val size = it?.size?: 0
            val last = mRouteViewModel.getLastUpdateTime().value?: 0
            var count = 0

            it?.forEach { item ->
                if(item.etaUpdateTime >= 0L && item.etaUpdateTime >= last)
                    count++
            }

            if (size == count || mRouteStopsAdapter.dataSource.size != size) {
                it?.let { mRouteStopsAdapter.dataSource = it }

                if (size == count)
                    mRootView.refresh_layout.isRefreshing = false

                Log.d("XXX", "[GoGo] $count $size ${mRouteStopsAdapter.dataSource.size}")
            } else {
                //Log.d("XXX", "[Wait] = $count")
            }
        })
    }
}
