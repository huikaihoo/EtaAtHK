package hoo.etahk.remote.connection

import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop

interface BaseConnection {
    /**
     * Get List of Routes No that support ETA
     */
    fun getEtaRoutes(company: String = ""): List<String>?

    /**
     * Get List of Parent Routes
     */
    fun getParentRoutes(company: String = ""): HashMap<String, Route>?

    /**
     * Get Parent Route by Route No
     */
    fun getParentRoute(routeKey: RouteKey): Route?

    fun getChildRoutes(parentRoute: Route)
    fun getStops(route: Route, needEtaUpdate: Boolean)
    @Deprecated("Use 'updateEta(List<Stop>): Unit' instead.")
    fun updateEta(stop: Stop)
    fun updateEta(stops: List<Stop>)
}