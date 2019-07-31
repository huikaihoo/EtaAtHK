package hoo.etahk.view.follow

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import hoo.etahk.model.data.FollowGroup
import hoo.etahk.model.data.FollowLocation
import hoo.etahk.model.relation.LocationAndGroups
import hoo.etahk.transfer.repo.FollowRepo
import hoo.etahk.transfer.repo.RoutesRepo
import hoo.etahk.view.base.TimerViewModel

class FollowViewModel : TimerViewModel() {
    private var updateChecked = false
    private var followLocations: LiveData<List<LocationAndGroups>>? = null
    val selectedLocation = MutableLiveData<LocationAndGroups>()
    val enableSorting = MutableLiveData<Boolean>()
    var lastLocation: Location? = null

    var isNearbyStops = false
    var isLocationIdUsed: Boolean = false
    var isSnackbarShowed: Boolean = false

    init {
        subscribeToRepo()
    }

    fun initLocationsAndGroups() {
        FollowRepo.initLocationsAndGroups()
    }

    fun needUpdateParentRoute(): Boolean {
        val result = !updateChecked && RoutesRepo.needUpdateParentRoutes()
        updateChecked = true
        return result
    }

    ///////////////////////////
    // Start of Menu Action
    fun insertLocation(name: String) {
        FollowRepo.insertLocation(name)
    }

    fun updateLocation(location: FollowLocation) {
        FollowRepo.updateLocation(location)
    }

    fun deleteLocation(location: FollowLocation) {
        FollowRepo.deleteLocation(location)
    }

    fun insertGroup(name: String) {
        selectedLocation.value?.let {
            it.location.Id?.let { locationId ->
                FollowRepo.insertGroup(locationId, name)
            }
        }
    }

    fun updateGroup(group: FollowGroup) {
        FollowRepo.updateGroup(group)
    }

    fun deleteGroup(group: FollowGroup) {
        FollowRepo.deleteGroup(group)
    }
    // End of Menu Action
    ///////////////////////////

    fun getFollowLocations(): LiveData<List<LocationAndGroups>> {
        return followLocations!!
    }

    private fun subscribeToRepo() {
        followLocations = FollowRepo.getLocations()
    }
}