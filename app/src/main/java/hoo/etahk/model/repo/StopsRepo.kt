package hoo.etahk.model.repo

import android.arch.lifecycle.LiveData
import hoo.etahk.common.Constants
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
    fun updateEta(stops: List<Stop>?, sameCompany: Boolean = true) {
        launch(CommonPool) {
            if (stops != null && stops.isNotEmpty()) {
                if (sameCompany) {
                    ConnectionHelper.updateEta(stops)
                } else {
                    val map = stops.groupBy {
                            when (it.routeKey.company) {
                                Constants.Company.LWB -> Constants.Company.KMB
                                Constants.Company.CTB -> Constants.Company.NWFB
                                else -> it.routeKey.company
                             }
                         }
                    map.forEach { company, stopsByCompany ->
                        ConnectionHelper.updateEta(stopsByCompany)
                    }
                }
            }
        }
    }

    // Followed Stops
    fun getFollowedStops(): LiveData<List<Stop>> {
        return AppHelper.db.stopDao().selectAll()
    }
}