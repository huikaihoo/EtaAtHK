package hoo.etahk.remote.connection

import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.EtaResult

interface BaseConnection {
    /**
     * Get List of Routes No that support ETA
     *
     * @param company company code
     * @return list of route no
     */
    fun getEtaRoutes(company: String = ""): List<String>?

    /**
     * Get List of Parent Routes
     *
     * @param company company code
     * @return map of route no to its parent route
     */
    fun getParentRoutes(company: String = ""): HashMap<String, Route>?

    /**
     * Get Parent Route by Route No
     *
     * @param routeKey key of parent route
     * @return parent route
     */
    fun getParentRoute(routeKey: RouteKey): Route?

    /**
     * Get Child Route by Parent Route and update into DB
     *
     *  @param parentRoute parent route
     */
    fun getChildRoutes(parentRoute: Route)

    /**
     * Get list of stops and path by Child Route and update into DB
     *
     * @param route Child Route
     * @param needEtaUpdate update eta of stops as well if true
     */
    fun getStops(route: Route, needEtaUpdate: Boolean)

    @Deprecated("Use 'updateEta(List<Stop>): Unit' instead.")
    fun updateEta(stop: Stop)

    /**
     * Get Eta of list of stops and update into DB
     *
     * @param stops list of stops
     */
    fun updateEta(stops: List<Stop>)

    /**
     * Convert Eta result with message only (without time) to EtaResult
     *
     * @param stop stop where eta result belongs to
     * @param msg message of eta result
     * @return EtaResult that only contain message only
     */
    fun toEtaResult(stop: Stop, msg: String): EtaResult {
        return EtaResult(
            company = stop.routeKey.company,
            etaTime = -1L,
            msg = msg,
            scheduleOnly = false,
            distance = -1L)
    }
}