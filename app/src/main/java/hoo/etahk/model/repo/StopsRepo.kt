package hoo.etahk.model.repo

import android.arch.lifecycle.LiveData
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

object StopsRepo {

    private val TAG = "StopsRepo"

    // Stops by RouteKey
    fun getStops(route: Route): LiveData<List<Stop>> {
        // TODO("Only get data not expired")
        return AppHelper.db.stopsDao().select(
                route.routeKey.company,
                route.routeKey.routeNo,
                route.routeKey.bound,
                route.routeKey.variant,
                0)
    }

    fun getStopsFromRemote(route: Route, needEtaUpdate: Boolean){
        launch(CommonPool) {
            // TODO("Only get from remote when data outdated")
            ConnectionHelper.getStops(route, needEtaUpdate)
            // if data is not outdated
            // updateEta(ListOfStops)
        }
    }

    // ETA
    fun updateEta(stops: List<Stop>?) {
        launch(CommonPool) {
            stops?.forEach { stop ->
                ConnectionHelper.updateEta(stop)
            }
        }
    }

    // Followed Stops
    fun getFollowedStops(): LiveData<List<Stop>> {
        return AppHelper.db.stopsDao().selectAll()
    }
}