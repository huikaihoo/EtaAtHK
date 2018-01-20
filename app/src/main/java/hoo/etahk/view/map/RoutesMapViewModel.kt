package hoo.etahk.view.map

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.relation.RouteAndStops
import hoo.etahk.model.repo.StopsRepo

class RoutesMapViewModel : ViewModel() {
    private var routeAndStopsList: LiveData<List<RouteAndStops>>? = null

    var selectedRoutePosition = 0

    var routeKey: RouteKey? = null
        set(value) {
            field = value
            if (value != null)
                subscribeToRepo()
        }

    fun getRouteAndStopsList(): LiveData<List<RouteAndStops>> {
        return routeAndStopsList!!
    }

    private fun subscribeToRepo() {
        routeAndStopsList = StopsRepo.getRouteStops(routeKey!!.company, routeKey!!.routeNo)
    }
}