package hoo.etahk.view.follow

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.constants.Argument
import hoo.etahk.common.constants.SharePrefs
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.view.AlertDialogBuilder
import hoo.etahk.common.view.ItemTouchHelperCallback
import hoo.etahk.model.data.FollowGroup
import hoo.etahk.model.data.FollowItem
import hoo.etahk.model.data.Stop
import hoo.etahk.model.relation.ItemAndStop
import hoo.etahk.model.relation.LocationAndGroups
import hoo.etahk.view.base.BaseAdapter
import hoo.etahk.view.base.BaseFragment
import hoo.etahk.view.route.RouteActivity
import kotlinx.android.synthetic.main.fragment_recycler.view.recycler_view
import kotlinx.android.synthetic.main.fragment_recycler.view.refresh_layout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.startActivity

class FollowFragment : BaseFragment() {

    companion object {
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

    var isItemsDisplaySeqChanged = false
    val lastLocation
        get() = viewModel.lastLocation

    private lateinit var rootView: View
    private lateinit var viewModel: FollowViewModel
    private lateinit var fragmentViewModel: FollowFragmentViewModel
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var itemTouchHelperCallback: ItemTouchHelperCallback
    private var nearbyStopsAdapter: NearbyStopsAdapter = NearbyStopsAdapter()
    private var followItemsAdapter: FollowItemsAdapter = FollowItemsAdapter()

    private val activeAdapter: BaseAdapter<FollowFragment,*>
        get() = (if (fragmentViewModel.isNearbyStops) nearbyStopsAdapter else followItemsAdapter)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        followItemsAdapter.context = this
        nearbyStopsAdapter.context = this

        viewModel= ViewModelProviders.of(activity!!).get(FollowViewModel::class.java)
        fragmentViewModel = ViewModelProviders.of(this).get(FollowFragmentViewModel::class.java)

//        if (fragmentViewModel.groupId == null)
//            fragmentViewModel.groupId = extras!!.getInt(ARG_POSITION).toLong()

        // Recycler View
        rootView = inflater.inflate(R.layout.fragment_recycler_fast_scroll, container, false)

        rootView.recycler_view.layoutManager = LinearLayoutManager(activity)
        rootView.recycler_view.adapter = activeAdapter
        rootView.recycler_view.itemAnimator = DefaultItemAnimator()
        rootView.recycler_view.addItemDecoration(
            DividerItemDecoration(
                activity,
                (rootView.recycler_view.layoutManager as LinearLayoutManager).orientation
            )
        )
        itemTouchHelperCallback = ItemTouchHelperCallback(followItemsAdapter,
            enableDrag = false,
            enableSwipe = false
        )
        itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rootView.recycler_view)

        // Refresh Layout
        rootView.refresh_layout.setColorSchemeColors(Utils.getThemeColorPrimary(activity!!))
        rootView.refresh_layout.isRefreshing = false
        fragmentViewModel.isRefreshingAll = activeAdapter.dataSource.isEmpty()
        rootView.refresh_layout.setOnRefreshListener {
            isItemsDisplaySeqChanged = false
            viewModel.stopTimer()
        }

        subscribeUiChanges()

        return rootView
    }

    fun updateEtaByStops(stops: List<Stop>) {
        logd("updateEtaByStops")
        GlobalScope.launch(Dispatchers.Main) {
            rootView.refresh_layout.isRefreshing = true
            stops.forEach { it.isLoading = true }
            nearbyStopsAdapter.notifyDataSetChanged()
        }

        fragmentViewModel.updateEta(stops)
    }

    fun updateEta(items: List<ItemAndStop>) {
        GlobalScope.launch(Dispatchers.Main) {
            rootView.refresh_layout.isRefreshing = true
            items.forEach { it.stop?.isLoading = true }
            followItemsAdapter.notifyDataSetChanged()
        }

        val stops = mutableListOf<Stop>()
        items.forEach{ item ->
            if (item.stop != null) {
                stops.add(item.stop!!)
            }
        }
        fragmentViewModel.updateEta(stops)
    }

    fun updateItemsDisplaySeq(items: List<ItemAndStop>) {
        GlobalScope.launch(Dispatchers.Default) {
            val updatedItems = mutableListOf<FollowItem>()

            for (i in items.indices) {
                val item = items[i].item
                item.displaySeq = i + 1L
                updatedItems.add(items[i].item)
            }

            fragmentViewModel.updateFollowItems(updatedItems.toList())
        }
    }

    fun showItemPopupMenu(view: View, stop: Stop) {
        val popup = PopupMenu(activity!!, view, Gravity.END)
        popup.inflate(R.menu.popup_follow_nearby_stop)

        val route = fragmentViewModel.getParentRouteOnce(
            stop.routeKey.company,
            stop.routeKey.routeNo
        )

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.popup_view -> {
                    activity?.startActivity<RouteActivity>(
                        Argument.ARG_COMPANY to stop.routeKey.company,
                        Argument.ARG_ROUTE_NO to stop.routeKey.routeNo,
                        Argument.ARG_TYPE_CODE to stop.routeKey.typeCode,
                        Argument.ARG_ANOTHER_COMPANY to route.anotherCompany,
                        Argument.ARG_GOTO_BOUND to stop.routeKey.bound,
                        Argument.ARG_GOTO_SEQ to stop.seq
                    )
                }
                R.id.popup_add_fav -> {
                    fragmentViewModel.insertRouteFavourite(route)
                    Snackbar.make(view, R.string.msg_add_to_favourite_success, Snackbar.LENGTH_SHORT).show()
                }
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
                            Snackbar.make(view, R.string.msg_add_to_follow_stop_success, Snackbar.LENGTH_SHORT).show()
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

    fun showItemPopupMenu(view: View, item: ItemAndStop) {
        val showPopup = !(viewModel.enableSorting.value ?: true)

        if (showPopup) {
            val popup = PopupMenu(activity!!, view, Gravity.END)
            popup.inflate(R.menu.popup_follow_item)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.popup_view -> {
                        item.stop?.let {
                            val route = fragmentViewModel.getParentRouteOnce(it.routeKey.company, it.routeKey.routeNo)

                            activity?.startActivity<RouteActivity>(
                                Argument.ARG_COMPANY to it.routeKey.company,
                                Argument.ARG_ROUTE_NO to it.routeKey.routeNo,
                                Argument.ARG_TYPE_CODE to it.routeKey.typeCode,
                                Argument.ARG_ANOTHER_COMPANY to route.anotherCompany,
                                Argument.ARG_GOTO_BOUND to it.routeKey.bound,
                                Argument.ARG_GOTO_SEQ to it.seq
                            )
                        }
                    }
                    R.id.popup_move -> {
                        val locationAndGroups = fragmentViewModel.getAllFollowLocations()
                        val groupList = mutableListOf<FollowGroup>()
                        locationAndGroups.forEach { groupList.addAll(it.groups) }

                        var removeIndex = -1
                        for (i in groupList.indices) {
                            if (groupList[i].Id == fragmentViewModel.groupId) {
                                removeIndex = i
                                break
                            }
                        }
                        if (removeIndex >= 0)
                            groupList.removeAt(removeIndex)

                        val displayList = Array(groupList.size) { i -> groupList[i].locationName + getString(R.string.to_middle) + groupList[i].name }
                        var selectedIndex = -1

                        lateinit var positiveButton: Button
                        val dialog = AlertDialogBuilder(activity!!)
                            .setTitle(R.string.title_move_item_to)
                            .setSingleChoiceItems(displayList, selectedIndex) { dialog, which ->
                                selectedIndex = which
                                positiveButton.isEnabled = (selectedIndex >= 0)
                            }
                            .setPositiveButton(android.R.string.ok) { dialog, which ->
                                item.item.groupId = groupList[selectedIndex].Id!!
                                fragmentViewModel.updateFollowItems(listOf(item.item), true)
                                Snackbar.make(view, R.string.msg_move_follow_stop_success, Snackbar.LENGTH_SHORT).show()
                            }
                            .setNegativeButton(android.R.string.cancel, null)
                            .show()

                        positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        positiveButton.isEnabled = false
                    }
                    R.id.popup_remove -> {
                        AlertDialogBuilder(activity!!)
                            .setTitle(R.string.title_conform_delete_item)
                            .setMessage(R.string.content_conform_delete)
                            .setPositiveButton(android.R.string.ok) { dialog, which ->
                                fragmentViewModel.deleteFollowItem(item.item)
                                Snackbar.make(view, R.string.msg_one_item_removed, Snackbar.LENGTH_SHORT).show()
                            }
                            .setNegativeButton(android.R.string.cancel, null)
                            .show()
                    }
                }
                true
            }
            popup.show()
        }
    }

    private fun subscribeUiChanges() {
        viewModel.enableSorting.observe(viewLifecycleOwner, Observer<Boolean> {
            it?.let { itemTouchHelperCallback.enableDrag = it }
        })

        viewModel.selectedLocation.observe(viewLifecycleOwner, Observer<LocationAndGroups> {
            val position = arguments!!.getInt(ARG_POSITION)
            logd("subscribeUiChanges $position")

            if (position < it.groups.size && it.groups[position].Id?: 0L > 0L ) {
                if (fragmentViewModel.groupId != it.groups[position].Id) {
                    //logd("subscribeUiChanges XX ${it.groups[position].Id}")
                    fragmentViewModel.removeObservers(this)
                    fragmentViewModel.isNearbyStops = it.location.pin
                    fragmentViewModel.groupId = it.groups[position].Id
                    subscribeItemsChanges(position)
                }
            }
        })
    }

    private fun subscribeItemsChanges(position: Int) {
        if (fragmentViewModel.isNearbyStops) {
            rootView.recycler_view.adapter = nearbyStopsAdapter
            fragmentViewModel.resetNearbyStops(position, viewModel.lastLocation, this)

            // Nearby Stops
            fragmentViewModel.nearbyStops?.observe(this, Observer {

                if (!isItemsDisplaySeqChanged) {
                    val size = it?.size ?: 0
                    val last = viewModel.getLastUpdateTime().value ?: 0L

                    var errorCount = 0
                    var updatedCount = 0

                    it.map { it.stop }.forEach { stop ->
                        if (stop.etaStatus != Constants.EtaStatus.SUCCESS) {
                            errorCount++
                        }
                        if (stop.etaUpdateTime >= 0L && stop.etaUpdateTime >= last) {
                            updatedCount++
                        }
                    }

                    logd("F=$errorCount U=$updatedCount T=$size")

                    // Sort and filter Nearby stops
                    val stops = if (lastLocation == null) it else it.filter {nearbyStop ->
                        viewModel.lastLocation!!.distanceTo(nearbyStop.stop.location) < SharePrefs.DEFAULT_NEARBY_STOPS_DISTANCE
                    }.sortedBy { nearbyStop ->
                        viewModel.lastLocation!!.distanceTo(nearbyStop.stop.location)
                    }

                    // Mark the stops that need to show header (stops compare with previous one has different position)
                    stops.forEachIndexed { i, stop ->
                        // TODO("Better way to group bus stops")
                        if (i == 0 || stop.stop.location.distanceTo(stops[i-1].stop.location) > SharePrefs.DEFAULT_SAME_STOP_DISTANCE) {
                            stop.showHeader = true
                        }
                    }
                    nearbyStopsAdapter.dataSource = stops

                    if (!fragmentViewModel.isEtaInit && size > 0) {
                        fragmentViewModel.isEtaInit = true
                        fragmentViewModel.isRefreshingAll = true
                        updateEtaByStops(it.map{ it.stop })
                    } else if (size == updatedCount) {
                        rootView.refresh_layout.isRefreshing = false

                        if (fragmentViewModel.isRefreshingAll) {
                            logd("Start timer")
                            fragmentViewModel.isRefreshingAll = false
                            viewModel.startTimer()
                        }
                    }

                    // TODO ("Show Network Error Message based on Network Error")
                }
            })
        } else {
            rootView.recycler_view.adapter = followItemsAdapter

            // Normal Group
            fragmentViewModel.getFollowItems().observe(this, Observer<List<ItemAndStop>> {
                if (!isItemsDisplaySeqChanged) {
                    val size = it?.size ?: 0
                    val last = viewModel.getLastUpdateTime().value ?: 0L

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

                    logd("F=$errorCount U=$updatedCount T=$size")

                    it?.let { followItemsAdapter.dataSource = it }

                    if (!fragmentViewModel.isEtaInit && size > 0) {
                        fragmentViewModel.isEtaInit = true
                        fragmentViewModel.isRefreshingAll = true
                        updateEta(it!!)
                    } else if (size == updatedCount) {
                        rootView.refresh_layout.isRefreshing = false

                        if (fragmentViewModel.isRefreshingAll) {
                            logd("Start timer")
                            fragmentViewModel.isRefreshingAll = false
                            viewModel.startTimer()
                        }
                    }

                    // TODO ("Show Network Error Message based on Network Error")
                }
            })
        }

        logd("b4 getLastUpdateTime")
        viewModel.getLastUpdateTime().removeObservers(this)
        viewModel.getLastUpdateTime().observe(this, Observer<Long> {
            logd("getLastUpdateTime $it")
            isItemsDisplaySeqChanged = false

            if (fragmentViewModel.isNearbyStops) {
                val stops = fragmentViewModel.nearbyStops?.value?.map{ it.stop }
                if (stops != null && stops.isNotEmpty() && !fragmentViewModel.isRefreshingAll) {
                    fragmentViewModel.isRefreshingAll = true
                    updateEtaByStops(stops)
                }
            } else {
                val items = fragmentViewModel.getFollowItems().value
                if (items != null && items.isNotEmpty() && !fragmentViewModel.isRefreshingAll) {
                    fragmentViewModel.isRefreshingAll = true
                    updateEta(items)
                }
            }
        })
    }

}
