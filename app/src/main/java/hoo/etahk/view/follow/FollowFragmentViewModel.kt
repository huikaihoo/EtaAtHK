package hoo.etahk.view.follow

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import hoo.etahk.model.data.Stop
import hoo.etahk.model.relation.ItemAndStop
import hoo.etahk.model.repo.FollowRepo
import hoo.etahk.model.repo.StopsRepo

class FollowFragmentViewModel : ViewModel() {
    private var followItems: LiveData<List<ItemAndStop>>? = null

    var groupId: Long? = null
        set(value) {
            field = value
            if (value != null)
                subscribeToRepo()

        }

    var isRefreshingAll: Boolean = false

    fun getFollowItems(): LiveData<List<ItemAndStop>> {
        return followItems!!
    }

    fun updateEta(items: List<ItemAndStop>) {
        val stops = mutableListOf<Stop>()
        items.forEach{item -> stops.add(item.stop!!)}
        StopsRepo.updateEta(stops)
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