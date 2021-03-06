package hoo.etahk.view.route

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.constants.Argument
import hoo.etahk.common.extensions.getExtra
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.view.AlertDialogBuilder
import hoo.etahk.model.data.FollowGroup
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop
import hoo.etahk.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_recycler_fast_scroll.view.recycler_view
import kotlinx.android.synthetic.main.fragment_recycler_fast_scroll.view.refresh_layout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RouteFragment : BaseFragment() {

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_BOUND = "bound"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(bound: Long): RouteFragment {
            val fragment = RouteFragment()
            val args = Bundle()
            args.putLong(ARG_BOUND, bound)
            fragment.arguments = args
            return fragment
        }
    }

    val childRoutes
        get() = fragmentViewModel.getChildRoutes().value
    val selectedIndex
        get() = fragmentViewModel.selectedIndex.value ?: 0
    var isGotoSeqUsed
        get() = viewModel.isGotoSeqUsed
        set(value) { viewModel.isGotoSeqUsed = value }

    private lateinit var rootView: View
    private lateinit var viewModel: RouteViewModel
    private lateinit var fragmentViewModel: RouteFragmentViewModel
    private var routeStopsAdapter: RouteStopsAdapter = RouteStopsAdapter()
    private var subscribeStops = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        routeStopsAdapter.context = this

        viewModel = ViewModelProvider(activity!!).get(RouteViewModel::class.java)
        fragmentViewModel = ViewModelProvider(this).get(RouteFragmentViewModel::class.java)

        if (fragmentViewModel.routeKey == null)
            fragmentViewModel.routeKey =
                    viewModel.routeKey?.copy(bound = arguments!!.getLong(ARG_BOUND))

        rootView = inflater.inflate(R.layout.fragment_recycler_fast_scroll, container, false)

        rootView.recycler_view.layoutManager =
                LinearLayoutManager(activity)
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

    fun updateSpinnerIndex(index: Int) {
        val routes = childRoutes
        if (!routes.isNullOrEmpty() && fragmentViewModel.selectedIndex.value != index) {
            rootView.refresh_layout.isRefreshing = true
            fragmentViewModel.updateStops(routes, index)
            fragmentViewModel.selectedIndex.value = index
        }
    }

    fun updateEta(stops: List<Stop>) {
        GlobalScope.launch(Dispatchers.Main) {
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
                R.id.popup_add_item -> {
                    val locationAndGroups = fragmentViewModel.getAllFollowLocations()
                    val groupList = mutableListOf<FollowGroup>()
                    locationAndGroups.forEach { groupList.addAll(it.groups) }

                    val displayList = Array(groupList.size) { i -> groupList[i].locationName + getString(R.string.to_middle) + groupList[i].name }
                    val checkedList = BooleanArray(groupList.size){ false }
                    var checkedCnt = 0

                    lateinit var positiveButton: Button
                    val dialog = AlertDialogBuilder(activity!!)
                        .setTitle(R.string.title_add_item_to)
                        .setMultiChoiceItems(displayList, checkedList) { dialog, position, checked ->
                            checkedList[position] = checked
                            checkedCnt += if (checked) 1 else -1
                            positiveButton.isEnabled = (checkedCnt > 0)
                        }
                        .setPositiveButton(android.R.string.ok) { dialog, which ->
                            for (i in checkedList.indices) {
                                if (checkedList[i]) {
                                    fragmentViewModel.insertFollowItem(groupList[i].Id!!, stop)
                                }
                            }
                            Snackbar.make(this@RouteFragment.rootView, R.string.msg_add_to_follow_stop_success, Snackbar.LENGTH_SHORT).show()
                        }
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
        fragmentViewModel.getChildRoutes().observe(viewLifecycleOwner, Observer<List<Route>> {
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
        fragmentViewModel.getStops().observe(viewLifecycleOwner, Observer<List<Stop>> {
            val size = it?.size ?: 0
            val last = viewModel.getLastUpdateTime().value ?: 0L

            var errorCount = 0
            var updatedCount = 0
            var oldCount = 0

            it.forEach { item ->
                if (item.etaUpdateTime < last){
                    oldCount++
                    item.displayEta = false
                } else {
                    if (item.etaStatus != Constants.EtaStatus.SUCCESS) {
                        errorCount++
                    }
                    if (item.etaUpdateTime >= 0L) {
                        updatedCount++
                    }
                }
            }

            logd("E=$errorCount U=$updatedCount O=$oldCount T=$size last=$last")

            routeStopsAdapter.dataSource = it

            if (size > 0 && !viewModel.isGotoSeqUsed) {
                val gotoBound = activity!!.getExtra<Long>(Argument.ARG_GOTO_BOUND)
                val gotoSeq = activity!!.getExtra<Long>(Argument.ARG_GOTO_SEQ)
                if (gotoBound == fragmentViewModel.routeKey!!.bound && gotoSeq > 0) {
                    GlobalScope.launch(Dispatchers.Main) {
                        for (i in it?.indices!! ) {
                            if (it[i].seq == gotoSeq) {
                                logd("GotoSeqUsed $gotoSeq $i")
                                val layoutManager = rootView.recycler_view.layoutManager as LinearLayoutManager
                                layoutManager.scrollToPositionWithOffset(i, 0)
                            }
                        }
                    }
                }
            }
            if (size == updatedCount && errorCount <= 0) {
                rootView.refresh_layout.isRefreshing = false

                if (fragmentViewModel.isRefreshingAll) {
                    fragmentViewModel.isRefreshingAll = false
                    viewModel.startTimer()
                }
            }

            // TODO ("Show Network Error Message based on Network Error")
        })

        viewModel.getLastUpdateTime().observe(viewLifecycleOwner, Observer<Long> {
            val stops = fragmentViewModel.getStops().value
            if (!stops.isNullOrEmpty() && !fragmentViewModel.isRefreshingAll){
                fragmentViewModel.isRefreshingAll = true
                updateEta(stops)
            }
        })
    }
}
