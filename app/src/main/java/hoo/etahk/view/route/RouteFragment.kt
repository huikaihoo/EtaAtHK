package hoo.etahk.view.route

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop
import hoo.etahk.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_recycler.view.*

class RouteFragment : BaseFragment() {

    companion object {
        private const val TAG = "RouteFragment"
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_BOUND = "bound"

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
        mRouteFragmentViewModel =
                ViewModelProviders.of(this).get(RouteFragmentViewModel::class.java)

        if (mRouteFragmentViewModel.routeKey == null)
            mRouteFragmentViewModel.routeKey =
                    mRouteViewModel.routeKey?.copy(bound = arguments!!.getLong(ARG_BOUND))

        mRootView = inflater.inflate(R.layout.fragment_recycler, container, false)

        mRootView.recycler_view.layoutManager = LinearLayoutManager(activity)
        mRootView.recycler_view.adapter = mRouteStopsAdapter
        mRootView.recycler_view.itemAnimator = DefaultItemAnimator()
        mRootView.recycler_view.addItemDecoration(
            DividerItemDecoration(
                activity,
                (mRootView.recycler_view.layoutManager as LinearLayoutManager).orientation
            )
        )

        mRootView.refresh_layout.setColorSchemeColors(Utils.getThemeColorPrimary(activity!!))
        mRootView.refresh_layout.isRefreshing = mRouteStopsAdapter.dataSource.isEmpty()
        mRootView.refresh_layout.setOnRefreshListener {
            mRouteViewModel.stopTimer()
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
                mRouteFragmentViewModel.updateStops(it)
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
            val size = it?.size ?: 0
            val last = mRouteViewModel.getLastUpdateTime().value ?: 0L

            var networkErrorCount = 0
            var updatedCount = 0

            it?.forEach { item ->
                if (item.etaStatus == Constants.EtaStatus.NETWORK_ERROR)
                    networkErrorCount++
                if (item.etaUpdateTime >= 0L && item.etaUpdateTime >= last)
                    updatedCount++
            }

            Log.d(TAG, "NE=$networkErrorCount U=$updatedCount T=$size")

            it?.let { mRouteStopsAdapter.dataSource = it }

            if (size == updatedCount) {
                mRootView.refresh_layout.isRefreshing = false

                if (mRouteFragmentViewModel.etaStatus == Constants.EtaStatus.LOADING) {
                    mRouteFragmentViewModel.etaStatus = Constants.EtaStatus.SUCCESS
                    mRouteViewModel.startTimer()
                }
            }

            // TODO ("Show Network Error Message based on Network Error")
        })
    }
}
