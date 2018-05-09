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
import kotlinx.android.synthetic.main.fragment_recycler_fast_scroll.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

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

    private lateinit var rootView: View
    private lateinit var routeViewModel: RouteViewModel
    private lateinit var routeFragmentViewModel: RouteFragmentViewModel
    private var routeStopsAdapter: RouteStopsAdapter = RouteStopsAdapter()
    private var subscribeStops = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        routeStopsAdapter.context = this

        routeViewModel = ViewModelProviders.of(activity!!).get(RouteViewModel::class.java)
        routeFragmentViewModel =
                ViewModelProviders.of(this).get(RouteFragmentViewModel::class.java)

        if (routeFragmentViewModel.routeKey == null)
            routeFragmentViewModel.routeKey =
                    routeViewModel.routeKey?.copy(bound = arguments!!.getLong(ARG_BOUND))

        rootView = inflater.inflate(R.layout.fragment_recycler_fast_scroll, container, false)

        rootView.recycler_view.layoutManager = LinearLayoutManager(activity)
        rootView.recycler_view.adapter = routeStopsAdapter
        rootView.recycler_view.itemAnimator = DefaultItemAnimator()
        rootView.recycler_view.addItemDecoration(
            DividerItemDecoration(
                activity,
                (rootView.recycler_view.layoutManager as LinearLayoutManager).orientation
            )
        )

        rootView.refresh_layout.setColorSchemeColors(Utils.getThemeColorPrimary(activity!!))
        rootView.refresh_layout.isRefreshing = routeStopsAdapter.dataSource.isEmpty()
        rootView.refresh_layout.setOnRefreshListener {
            routeViewModel.stopTimer()
        }

        subscribeUiChanges()

        return rootView
    }

    fun updateEta(stops: List<Stop>) {
        launch(UI) {
            rootView.refresh_layout.isRefreshing = true
            stops.forEach { it.isLoading = true }
            routeStopsAdapter.notifyDataSetChanged()
        }
        routeFragmentViewModel.updateEta(stops)
    }

    private fun subscribeUiChanges() {
        routeFragmentViewModel.getChildRoutes().observe(this, Observer<List<Route>> {
            it?.let {
                routeFragmentViewModel.updateStops(it)
                if (!subscribeStops && it.isNotEmpty()) {
                    subscribeStops = true
                    routeFragmentViewModel.subscribeStopsToRepo()
                    subscribeStopsChanges()
                }
            }
        })
    }

    private fun subscribeStopsChanges() {
        routeFragmentViewModel.getStops().observe(this, Observer<List<Stop>> {
            val size = it?.size ?: 0
            val last = routeViewModel.getLastUpdateTime().value ?: 0L

            var errorCount = 0
            var updatedCount = 0

            it?.forEach { item ->
                if (item.etaStatus != Constants.EtaStatus.SUCCESS) {
                    errorCount++
                }
                if (item.etaUpdateTime >= 0L && item.etaUpdateTime >= last) {
                    updatedCount++
                }
            }

            Log.d(TAG, "F=$errorCount U=$updatedCount T=$size")

            if (size == errorCount) {
                rootView.refresh_layout.isRefreshing = true
            } else {
                it?.let { routeStopsAdapter.dataSource = it }

                if (size == updatedCount && errorCount <= 0) {
                    rootView.refresh_layout.isRefreshing = false

                    if (routeFragmentViewModel.isRefreshingAll) {
                        routeFragmentViewModel.isRefreshingAll = false
                        routeViewModel.startTimer()
                    }
                }
            }

            // TODO ("Show Network Error Message based on Network Error")
        })

        routeViewModel.getLastUpdateTime().observe(this, Observer<Long> {
            if (it != null) {
                val stops = routeFragmentViewModel.getStops().value
                if (stops != null && stops.isNotEmpty() && !routeFragmentViewModel.isRefreshingAll){
                    routeFragmentViewModel.isRefreshingAll = true
                    updateEta(stops)
                }
            }
        })
    }
}
