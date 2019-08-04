package hoo.etahk.view.follow

import android.location.Location
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import hoo.etahk.model.custom.NearbyStop
import hoo.etahk.model.data.FollowItem
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.relation.ItemAndStop
import hoo.etahk.model.relation.LocationAndGroups
import hoo.etahk.transfer.repo.FollowRepo
import hoo.etahk.transfer.repo.MiscRepo
import hoo.etahk.transfer.repo.RoutesRepo
import hoo.etahk.transfer.repo.StopsRepo

class FollowFragmentViewModel : ViewModel() {
    var nearbyStops: LiveData<List<NearbyStop>>? = null
        private set
    private var followItems: LiveData<List<ItemAndStop>>? = null

    var isNearbyStops = false
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

    // Nearby Stops
    fun resetNearbyStops(position: Int, location: Location?, lifecycleOwner: LifecycleOwner) {
        removeObservers(lifecycleOwner)
        if (location != null) {
            nearbyStops = if (position == 0) StopsRepo.getNearbyStops(location) else StopsRepo.getNearbyStopsFav(location)
        }
    }

    fun insertRouteFavourite(route: Route) {
        val routeKey = RouteKey(
            company = route.companyDetails[0],
            routeNo = route.routeKey.routeNo,
            bound = 0L,
            variant = 0L
        )
        val anotherCompany = if (route.companyDetails.size > 1) route.companyDetails[1] else null
        MiscRepo.insertRouteFavourite(routeKey, anotherCompany)
    }

    fun insertFollowItem(groupId: Long, stop: Stop) {
        FollowRepo.insertItem(groupId, stop)
    }

    // Normal Group
    fun getParentRouteOnce(company: String, routeNo: String): Route {
        return RoutesRepo.getParentRouteOnce(company, routeNo)
    }

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

    fun updateEta(stops: List<Stop>) {
        StopsRepo.updateEta(stops, false)
    }

    fun removeObservers(lifecycleOwner: LifecycleOwner) {
        followItems?.removeObservers(lifecycleOwner)
        nearbyStops?.removeObservers(lifecycleOwner)
    }

    private fun subscribeToRepo() {
        if (!isNearbyStops) {
            followItems = FollowRepo.getItems(groupId!!)
        }
    }
}