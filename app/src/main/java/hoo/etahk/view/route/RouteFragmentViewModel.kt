package hoo.etahk.view.route

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.relation.LocationAndGroups
import hoo.etahk.transfer.repo.FollowRepo
import hoo.etahk.transfer.repo.RoutesRepo
import hoo.etahk.transfer.repo.StopsRepo

class RouteFragmentViewModel : ViewModel() {
    private var childRoutes: LiveData<List<Route>>? = null
    private var stops: LiveData<List<Stop>>? = null
    private var hasUpdateStops = false

    var routeKey: RouteKey? = null
        set(value) {
            field = value
            if (value != null)
                subscribeChildRoutesToRepo()
        }
    var selectedIndex: Int = 0
        set(value) {
            if (field != value) {
                field = value
                //subscribeStopsToRepo()
            }
        }
    var isRefreshingAll: Boolean = false

    fun getAllFollowLocations(): List<LocationAndGroups> {
        return FollowRepo.getLocationsOnce()
    }

    fun insertFollowItem(groupId: Long, stop: Stop) {
        FollowRepo.insertItem(groupId, stop)
    }

    fun getChildRoutes(): LiveData<List<Route>> {
        return childRoutes!!
    }

    fun getStops(): LiveData<List<Stop>> {
        return stops!!
    }

    fun updateStops(childRoutes: List<Route>, needEtaUpdate: Boolean = true) {
        if (!hasUpdateStops && childRoutes.isNotEmpty()) {
            hasUpdateStops = true
            isRefreshingAll = needEtaUpdate
            StopsRepo.updateStops(childRoutes[0], needEtaUpdate)
        }
    }

    fun updateEta(stops: List<Stop>) {
        StopsRepo.updateEta(stops)
    }

    private fun subscribeChildRoutesToRepo() {
        childRoutes = RoutesRepo.getChildRoutes(routeKey!!.company, routeKey!!.routeNo, routeKey!!.bound)
    }

    fun subscribeStopsToRepo() {
        stops = StopsRepo.getStops(childRoutes!!.value!![selectedIndex])
    }
}