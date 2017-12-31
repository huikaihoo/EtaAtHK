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
import hoo.etahk.model.data.Stop
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
    private var mRouteAdapter: RouteAdapter = RouteAdapter()
    private var mIgnoreTimer = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mRouteAdapter.context = this

        mRouteViewModel = ViewModelProviders.of(activity!!).get(RouteViewModel::class.java)
        mRouteFragmentViewModel = ViewModelProviders.of(this).get(RouteFragmentViewModel::class.java)

        if (mRouteFragmentViewModel.routeKey == null)
            mRouteFragmentViewModel.routeKey = mRouteViewModel.routeKey?.copy(bound = arguments!!.getLong(ARG_BOUND))

        mRootView = inflater.inflate(R.layout.fragment_route, container, false)

        mRootView.recycler_view.layoutManager = LinearLayoutManager(activity)
        mRootView.recycler_view.adapter = mRouteAdapter

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

        mRouteFragmentViewModel.getStops().observe(this, Observer<List<Stop>> {
            val size = it?.size?: 0
            val last = mRouteViewModel.getLastUpdateTime().value?: 0
            var count = 0

            it?.forEach { item ->
                if(item.etaUpdateTime >= last)
                    count++
            }

            if (size == count || mRouteAdapter.dataSource.size != size) {
                it?.let { mRouteAdapter.dataSource = it }

                if (size == count)
                    mRootView.refresh_layout.isRefreshing = false

                Log.d("XXX", "GoGo = $count")
            } else {
                Log.d("XXX", "Wait = $count")
            }
        })

        mRouteViewModel.getLastUpdateTime().observe(this, Observer<Long> {
//            Snackbar.make(constraintLayout, "Auto Refresh Ignore=$mIgnoreTimer", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
            // Ignore the update for 1st time / on rotation
            if (mIgnoreTimer) {
                mIgnoreTimer = false
            } else {
                mRootView.refresh_layout.isRefreshing = true
                mRouteFragmentViewModel.updateAllEta()
            }
        })
    }
}
