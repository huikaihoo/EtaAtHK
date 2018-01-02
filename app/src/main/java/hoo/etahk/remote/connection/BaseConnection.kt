package hoo.etahk.remote.connection

import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop

interface BaseConnection {
    fun getChildRoutes(parentRoute: Route)
    fun getStops(route: Route, needEtaUpdate: Boolean)
    @Deprecated("Use 'updateEta(List<Stop>): Unit' instead.")
    fun updateEta(stop: Stop)
    fun updateEta(stops: List<Stop>)
}