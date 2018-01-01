package hoo.etahk.remote.connection

import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop

interface BaseConnection {
    fun getChildRoutes(parentRoute: Route)
    fun getStops(route: Route, needEtaUpdate: Boolean)
    fun updateEta(stop: Stop)
}