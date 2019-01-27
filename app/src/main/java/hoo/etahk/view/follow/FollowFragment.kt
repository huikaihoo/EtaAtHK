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
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.view.AlertDialogBuilder
import hoo.etahk.common.view.ItemTouchHelperCallback
import hoo.etahk.model.data.FollowGroup
import hoo.etahk.model.data.FollowItem
import hoo.etahk.model.relation.ItemAndStop
import hoo.etahk.model.relation.LocationAndGroups
import hoo.etahk.view.base.BaseFragment
import hoo.etahk.view.route.RouteActivity
import kotlinx.android.synthetic.main.fragment_recycler.view.*
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

    private lateinit var rootView: View
    private lateinit var viewModel: FollowViewModel
    private lateinit var fragmentViewModel: FollowFragmentViewModel
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var itemTouchHelperCallback: ItemTouchHelperCallback
    private var followItemsAdapter: FollowItemsAdapter = FollowItemsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        followItemsAdapter.context = this

        viewModel= ViewModelProviders.of(activity!!).get(FollowViewModel::class.java)
        fragmentViewModel = ViewModelProviders.of(this).get(FollowFragmentViewModel::class.java)

//        if (fragmentViewModel.groupId == null)
//            fragmentViewModel.groupId = extras!!.getInt(ARG_POSITION).toLong()

        // Recycler View
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
        itemTouchHelperCallback = ItemTouchHelperCallback(followItemsAdapter, false, false)
        itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rootView.recycler_view)

        // Refresh Layout
        rootView.refresh_layout.setColorSchemeColors(Utils.getThemeColorPrimary(activity!!))
        rootView.refresh_layout.isRefreshing = false
        fragmentViewModel.isRefreshingAll = followItemsAdapter.dataSource.isEmpty()
        rootView.refresh_layout.setOnRefreshListener {
            isItemsDisplaySeqChanged = false
            viewModel.stopTimer()
        }

        subscribeUiChanges()

        return rootView
    }

    fun updateEta(items: List<ItemAndStop>) {
        GlobalScope.launch(Dispatchers.Main) {
            rootView.refresh_layout.isRefreshing = true
            items.forEach { it.stop?.isLoading = true }
            followItemsAdapter.notifyDataSetChanged()
        }

        fragmentViewModel.updateEta(items)
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
                            .setTitle(R.string.title_select_groups_to_move)
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
            it?.let {
                val position = arguments!!.getInt(ARG_POSITION)
                logd("subscribeUiChanges $position")
                if (position < it.groups.size && it.groups[position].Id?: 0L > 0L ) {
                    if (fragmentViewModel.groupId != it.groups[position].Id) {
                        //logd("subscribeUiChanges XX ${it.groups[position].Id}")
                        fragmentViewModel.removeObservers(this)
                        fragmentViewModel.groupId = it.groups[position].Id
                        subscribeItemsChanges()
                    }
                }
            }
        })
    }

    private fun subscribeItemsChanges() {
        fragmentViewModel.getFollowItems().observe(viewLifecycleOwner, Observer<List<ItemAndStop>> {
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

        viewModel.getLastUpdateTime().observe(viewLifecycleOwner, Observer<Long> {
            isItemsDisplaySeqChanged = false
            if (it != null) {
                val items = fragmentViewModel.getFollowItems().value
                if (items != null && items.isNotEmpty() && !fragmentViewModel.isRefreshingAll) {
                    fragmentViewModel.isRefreshingAll = true
                    updateEta(items)
                }
            }
        })
    }

}
