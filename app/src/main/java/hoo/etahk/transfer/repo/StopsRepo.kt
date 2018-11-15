package hoo.etahk.transfer.repo

import androidx.lifecycle.LiveData
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop
import hoo.etahk.model.relation.RouteAndStops
import kotlinx.coroutines.*

object StopsRepo {

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
        GlobalScope.launch(Dispatchers.Default) {
            if (AppHelper.db.stopDao().lastUpdate(
                    route.routeKey.company,
                    route.routeKey.routeNo,
                    route.routeKey.bound,
                    route.routeKey.variant) < Utils.getValidUpdateTimestamp()) {
                logd("updateStops ${route.routeKey.company} ${route.routeKey.routeNo} ${route.routeKey.bound} ${route.routeKey.variant}")
                ConnectionHelper.getStops(route, needEtaUpdate)
            } else if (needEtaUpdate) {
                updateEta(route)
            }
        }
    }

    fun updateEta(route: Route) {
        val stops = AppHelper.db.stopDao().selectOnce(
                        route.routeKey.company,
                        route.routeKey.routeNo,
                        route.routeKey.bound,
                        route.routeKey.variant)
        updateEta(stops)
    }

    // ETA
    fun updateEta(stops: List<Stop>?, sameCompany: Boolean = true) {
        GlobalScope.launch(Dispatchers.Default) {
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
}