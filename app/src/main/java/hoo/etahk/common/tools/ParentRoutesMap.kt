package hoo.etahk.common.tools

import hoo.etahk.common.Utils
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey

class ParentRoutesMap(val ignoreConstraint: Boolean = false) {
    private val map = HashMap<String, MutableList<Route>>()

    var size = 0
        private set

    fun add(route: Route) {
        route.from.tc = Utils.phaseFromTo(route.from.tc)
        route.to.tc = Utils.phaseFromTo(route.to.tc)

        if (ignoreConstraint || get(route.routeKey) == null) {
            if (map.contains(route.routeKey.routeNo)) {
                map[route.routeKey.routeNo]!!.add(route)
            } else {
                map[route.routeKey.routeNo] = mutableListOf(route)
            }
            size++
        }
    }

    fun addAll(routes: Collection<Route>) {
        routes.forEach { add(it) }
    }

    fun get(routeKey: RouteKey): Route? {
        return get(routeKey.company, routeKey.routeNo)
    }

    fun get(company: String, routeNo: String): Route? {
        map[routeNo]?.forEach {
            if (it.companyDetails.contains(company))
                return it
        }
        return null
    }

    fun get(routeNo: String): MutableList<Route>? {
        return map[routeNo]
    }

    fun getAll(): List<Route> {
        return map.values.toList().flatten()
    }
}