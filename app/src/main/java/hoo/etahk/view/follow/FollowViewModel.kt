package hoo.etahk.view.follow

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import hoo.etahk.model.relation.LocationAndGroups
import hoo.etahk.model.repo.FollowRepo
import hoo.etahk.view.base.TimerViewModel

class FollowViewModel : TimerViewModel() {
    private var followLocations: LiveData<List<LocationAndGroups>>? = null
    private var selectedLocation = MutableLiveData<LocationAndGroups>()
    //val spinnerChangeTime = MutableLiveData<Long>()

    var selectedLocationPosition: Int = 0
        set(value) {
            field = value
            //spinnerChangeTime.value = System.currentTimeMillis()
            followLocations?.value?.let {
                selectedLocation.value = it[selectedLocationPosition]
            }
        }
    //var selectedTabPosition: Int = 0

    init {
        subscribeToRepo()
    }

    fun getFollowLocations(): LiveData<List<LocationAndGroups>> {
        return followLocations!!
    }

    fun getSelectedLocation(): LiveData<LocationAndGroups> {
        return selectedLocation
    }

    private fun subscribeToRepo() {
        followLocations = FollowRepo.getLocations()
    }
}