package hoo.etahk.view.route

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hoo.etahk.R
import hoo.etahk.model.data.Stop
import kotlinx.android.synthetic.main.fragment_route.*
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

        val rootView = inflater.inflate(R.layout.fragment_route, container, false)

        rootView.recycler_view.layoutManager = LinearLayoutManager(activity)
        rootView.recycler_view.adapter = mRouteAdapter

        subscribeUiChanges()

        return rootView
    }

    fun updateEta(stops: List<Stop>) {
        mRouteFragmentViewModel.updateEta(stops)
    }

    private fun subscribeUiChanges() {
        mIgnoreTimer = true

        mRouteFragmentViewModel.getStops().observe(this, Observer<List<Stop>> {
            it?.let { mRouteAdapter.dataSource = it }
        })

        mRouteViewModel.getLastUpdateTime().observe(this, Observer<Long> {
            Snackbar.make(constraintLayout, "Auto Refresh Ignore=$mIgnoreTimer", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            // Ignore the update for 1st time / on rotation
            if (mIgnoreTimer) {
                mIgnoreTimer = false
            } else {
                mRouteFragmentViewModel.updateAllEta()
            }
        })
    }
}
