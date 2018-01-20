package hoo.etahk.model.repo

import android.arch.lifecycle.LiveData
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop
import hoo.etahk.model.relation.RouteAndStops
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

object StopsRepo {

    private const val TAG = "StopsRepo"

    // RouteStops
    fun getRouteStops(company: String, routeNo: String): LiveData<List<RouteAndStops>> {
        return AppHelper.db.routeStopsDao().select(company, routeNo)
    }

    fun getRouteStops(company: String, routeNo: String, bound: Long): LiveData<List<RouteAndStops>> {
        return AppHelper.db.routeStopsDao().select(company, routeNo, bound)
    }

    // Stops by RouteKey
    fun getStops(route: Route): LiveData<List<Stop>> {
        return AppHelper.db.stopDao().select(
                route.routeKey.company,
                route.routeKey.routeNo,
                route.routeKey.bound,
                route.routeKey.variant)
    }

    fun updateStops(route: Route, needEtaUpdate: Boolean){
        launch(CommonPool) {
            // TODO("Only get from remote when data outdated")
            ConnectionHelper.getStops(route, needEtaUpdate)
        }
    }

    // ETA
    fun updateEta(stops: List<Stop>?) {
        launch(CommonPool) {
            if (stops != null && stops.isNotEmpty())
                ConnectionHelper.updateEta(stops)
        }
    }

    // Followed Stops
    fun getFollowedStops(): LiveData<List<Stop>> {
        return AppHelper.db.stopDao().selectAll()
    }
}