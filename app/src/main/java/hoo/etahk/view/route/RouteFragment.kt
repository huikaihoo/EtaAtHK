package hoo.etahk.view.route

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.model.data.FollowGroup
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop
import hoo.etahk.view.App
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
    private lateinit var viewModel: RouteViewModel
    private lateinit var fragmentViewModel: RouteFragmentViewModel
    private var routeStopsAdapter: RouteStopsAdapter = RouteStopsAdapter()
    private var subscribeStops = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        routeStopsAdapter.context = this

        viewModel = ViewModelProviders.of(activity!!).get(RouteViewModel::class.java)
        fragmentViewModel =
                ViewModelProviders.of(this).get(RouteFragmentViewModel::class.java)

        if (fragmentViewModel.routeKey == null)
            fragmentViewModel.routeKey =
                    viewModel.routeKey?.copy(bound = arguments!!.getLong(ARG_BOUND))

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
            viewModel.stopTimer()
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
        fragmentViewModel.updateEta(stops)
    }

    fun showStopPopupMenu(view: View, stop: Stop) {
        val popup = PopupMenu(activity!!, view, Gravity.END)
        popup.inflate(R.menu.popup_route_stop)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.popup_add -> {
                    val locationAndGroups = fragmentViewModel.getAllFollowLocations()
                    val groupList = mutableListOf<FollowGroup>()
                    locationAndGroups.forEach { groupList.addAll(it.groups) }

                    val displayList = Array(groupList.size) { i -> groupList[i].locationName + App.instance.getString(R.string.to_middle) + groupList[i].name }
                    val checkedList = BooleanArray(groupList.size){ false }
                    var checkedCnt = 0

                    lateinit var positiveButton: Button
                    val dialog = AlertDialog.Builder(activity!!)
                        .setTitle(R.string.title_select_groups_to_add)
                        .setMultiChoiceItems(displayList, checkedList, { dialog, position, checked ->
                            checkedList[position] = checked
                            checkedCnt += if (checked) 1 else -1
                            positiveButton.isEnabled = (checkedCnt > 0)
                        })
                        .setPositiveButton(android.R.string.ok, { dialog, which ->
                            for(i in checkedList.indices) {
                                if (checkedList[i]) {
                                    fragmentViewModel.insertFollowItem(groupList[i].Id!!, stop)
                                }
                            }
                            Snackbar.make(view, R.string.msg_add_to_follow_stop_success, Snackbar.LENGTH_SHORT).show()
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()

                    positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    positiveButton.isEnabled = false
                }
            }
            true
        }
        popup.show()
    }

    private fun subscribeUiChanges() {
        fragmentViewModel.getChildRoutes().observe(this, Observer<List<Route>> {
            it?.let {
                fragmentViewModel.updateStops(it)
                if (!subscribeStops && it.isNotEmpty()) {
                    subscribeStops = true
                    fragmentViewModel.subscribeStopsToRepo()
                    subscribeStopsChanges()
                }
            }
        })
    }

    private fun subscribeStopsChanges() {
        fragmentViewModel.getStops().observe(this, Observer<List<Stop>> {
            val size = it?.size ?: 0
            val last = viewModel.getLastUpdateTime().value ?: 0L

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

                    if (fragmentViewModel.isRefreshingAll) {
                        fragmentViewModel.isRefreshingAll = false
                        viewModel.startTimer()
                    }
                }
            }

            // TODO ("Show Network Error Message based on Network Error")
        })

        viewModel.getLastUpdateTime().observe(this, Observer<Long> {
            if (it != null) {
                val stops = fragmentViewModel.getStops().value
                if (stops != null && stops.isNotEmpty() && !fragmentViewModel.isRefreshingAll){
                    fragmentViewModel.isRefreshingAll = true
                    updateEta(stops)
                }
            }
        })
    }
}
