package hoo.etahk.view.follow

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
import hoo.etahk.model.relation.ItemAndStop
import hoo.etahk.model.relation.LocationAndGroups
import hoo.etahk.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_recycler.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class FollowFragment : BaseFragment() {

    companion object {
        private const val TAG = "FollowFragment"
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_POSITION = "position"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(position: Int): FollowFragment {
            val fragment = FollowFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, position)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var rootView: View
    private lateinit var followViewModel: FollowViewModel
    private lateinit var followFragmentViewModel: FollowFragmentViewModel
    private var followItemsAdapter: FollowItemsAdapter = FollowItemsAdapter()
    private var subscribeItems = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        followItemsAdapter.context = this

        followViewModel= ViewModelProviders.of(activity!!).get(FollowViewModel::class.java)
        followFragmentViewModel = ViewModelProviders.of(this).get(FollowFragmentViewModel::class.java)

//        if (followFragmentViewModel.groupId == null)
//            followFragmentViewModel.groupId = arguments!!.getInt(ARG_POSITION).toLong()

        rootView = inflater.inflate(R.layout.fragment_recycler_fast_scroll, container, false)

        rootView.recycler_view.layoutManager = LinearLayoutManager(activity)
        rootView.recycler_view.adapter = followItemsAdapter
        rootView.recycler_view.itemAnimator = DefaultItemAnimator()
        rootView.recycler_view.addItemDecoration(
            DividerItemDecoration(
                activity,
                (rootView.recycler_view.layoutManager as LinearLayoutManager).orientation
            )
        )

        rootView.refresh_layout.setColorSchemeColors(Utils.getThemeColorPrimary(activity!!))
        rootView.refresh_layout.isRefreshing = followItemsAdapter.dataSource.isEmpty()
        followFragmentViewModel.isRefreshingAll = followItemsAdapter.dataSource.isEmpty()
        rootView.refresh_layout.setOnRefreshListener {
            followViewModel.stopTimer()
        }

        subscribeUiChanges()

        return rootView
    }

    fun updateEta(items: List<ItemAndStop>) {
        launch(UI) {
            rootView.refresh_layout.isRefreshing = true
            items.forEach { it.stop?.isLoading = true }
            followItemsAdapter.notifyDataSetChanged()
        }

        followFragmentViewModel.updateEta(items)
    }

    private fun subscribeUiChanges() {
        Log.d(TAG, "subscribeUiChanges")
//        followViewModel.spinnerChangeTime.observe(this, Observer<Long> {
//            val t = followViewModel.getFollowLocations().value!![followViewModel.selectedLocationPosition]
//            val position = arguments!!.getInt(ARG_POSITION)
//            Log.d(TAG, "subscribeUiChanges XX")
//            if (position < t.groups.size && (t.groups[position].Id?: 0L) > 0L ) {
//                Log.d(TAG, "subscribeUiChanges XX ${t.groups[position].Id}")
//                followFragmentViewModel.subscribeToRepo(t.groups[position].Id!!)
//                subscribeItemsChanges()
//            }
//        })
        followViewModel.getSelectedLocation().observe(this, Observer<LocationAndGroups> {
            it?.let {
                val position = arguments!!.getInt(ARG_POSITION)
                //Log.d(TAG, "subscribeUiChanges XX")
                if (position < it.groups.size && it.groups[position].Id?: 0L > 0L ) {
                    if (followFragmentViewModel.groupId != it.groups[position].Id) {
                        //Log.d(TAG, "subscribeUiChanges XX ${it.groups[position].Id}")
                        followFragmentViewModel.removeObservers(this)
                        followFragmentViewModel.groupId = it.groups[position].Id
                        subscribeItemsChanges()
                    }
//                    if (!subscribeItems) {
//                        subscribeItems = true
//
//                    }
                }
            }
        })
    }

    private fun subscribeItemsChanges() {
        Log.d(TAG, "subscribeItemsChanges")
        followFragmentViewModel.getFollowItems().observe(this, Observer<List<ItemAndStop>> {
            val size = it?.size ?: 0
            val last = followViewModel.getLastUpdateTime().value ?: 0L

            var errorCount = 0
            var updatedCount = 0

            it?.forEach { item ->
                item.stop?.let { stop ->
                    if (stop.etaStatus != Constants.EtaStatus.SUCCESS) {
                        errorCount++
                    }
                    if (stop.etaUpdateTime >= 0L && stop.etaUpdateTime >= last) {
                        updatedCount++
                    }
                }
            }

            Log.d(TAG, "F=$errorCount U=$updatedCount T=$size")

            it?.let { followItemsAdapter.dataSource = it }

            if (size == updatedCount) {
                rootView.refresh_layout.isRefreshing = false

                if (followFragmentViewModel.isRefreshingAll) {
                    Log.d(TAG, "Start timer")
                    followFragmentViewModel.isRefreshingAll = false
                    followViewModel.startTimer()
                }
            }

            // TODO ("Show Network Error Message based on Network Error")
        })

        followViewModel.getLastUpdateTime().observe(this, Observer<Long> {
            if (it != null) {
                val items = followFragmentViewModel.getFollowItems().value
                if (items != null && items.isNotEmpty() && !followFragmentViewModel.isRefreshingAll){
                    followFragmentViewModel.isRefreshingAll = true
                    updateEta(items)
                }
            }
        })
    }

}
