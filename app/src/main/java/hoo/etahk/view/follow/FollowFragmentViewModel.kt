package hoo.etahk.view.follow

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import hoo.etahk.model.data.FollowItem
import hoo.etahk.model.data.Stop
import hoo.etahk.model.relation.ItemAndStop
import hoo.etahk.model.relation.LocationAndGroups
import hoo.etahk.model.repo.FollowRepo
import hoo.etahk.model.repo.StopsRepo

class FollowFragmentViewModel : ViewModel() {
    private var followItems: LiveData<List<ItemAndStop>>? = null

    var isEtaInit = false

    var groupId: Long? = null
        set(value) {
            field = value
            if (value != null) {
                isEtaInit = false
                subscribeToRepo()
            }
        }

    var isRefreshingAll: Boolean = false

    fun getAllFollowLocations(): List<LocationAndGroups> {
        return FollowRepo.getLocationsOnce()
    }

    fun getFollowItems(): LiveData<List<ItemAndStop>> {
        return followItems!!
    }

    fun updateFollowItems(items: List<FollowItem>, newDisplaySeq: Boolean = false) {
        FollowRepo.updateItems(items, newDisplaySeq)
    }

    fun deleteFollowItem(item: FollowItem) {
        FollowRepo.deleteItem(item)
    }

    fun updateEta(items: List<ItemAndStop>) {
        val stops = mutableListOf<Stop>()
        items.forEach{item -> stops.add(item.stop!!)}
        StopsRepo.updateEta(stops, false)
        //FollowRepo.updateEta(items)
    }

    fun removeObservers(lifecycleOwner: LifecycleOwner) {
        followItems?.let{
            it.removeObservers(lifecycleOwner)
        }
    }

    private fun subscribeToRepo() {
        followItems = FollowRepo.getItems(groupId!!)
    }

}