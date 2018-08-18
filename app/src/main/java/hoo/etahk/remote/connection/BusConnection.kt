package hoo.etahk.remote.connection

import hoo.etahk.common.Constants.Company
import hoo.etahk.common.Utils
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error

object BusConnection : BaseConnection, AnkoLogger {

    override fun getEtaRoutes(company: String): List<String>? {
        return null
    }

    /*********************
     * Get Parent Routes *
     *********************/
    override fun getParentRoutes(company: String): HashMap<String, Route>? {
        val t = Utils.getCurrentTimestamp()

        // 1. Get Result from Multiple Sources
        val parentRoutesResult = HashMap<String, HashMap<String, Route>?>()
        parentRoutesResult.putAll(mapOf(Company.GOV to null, Company.NWFB to null))

        val etaRoutesResult = HashMap<String, List<String>?>()
        etaRoutesResult.putAll(mapOf(Company.KMB to null, Company.NWFB to null))

        try {
            runBlocking {
                val jobs = arrayListOf<Job>()

                parentRoutesResult.keys.forEach { company ->
                    jobs += launch(CommonPool) {
                        parentRoutesResult[company] = ConnectionHelper.getParentRoutes(company)
                    }
                }

                etaRoutesResult.keys.forEach { company ->
                    jobs += launch(CommonPool) {
                        etaRoutesResult[company] = ConnectionHelper.getEtaRoutes(company)
                    }
                }

                jobs.forEach { it.join() }
            }
        } catch (e: Exception) {
            error("getParentRoutes failed!", e)
        }

        debug("onResponse ${parentRoutesResult[Company.GOV]?.size}")
        debug("onResponse ${parentRoutesResult[Company.NWFB]?.size}")

        parentRoutesResult.values.forEach { if (it == null || it.isEmpty()) return null }
        //etaRoutesResult.values.forEach { if (it == null) return null }

        // 2. Merge NWFB Parents Routes to Gov Parents Routes
        val govResult = parentRoutesResult[Company.GOV]!!

        for((key, nwfbRoute) in parentRoutesResult[Company.NWFB]!!) {
            if (govResult.contains(key)) {
                val govRoute = govResult[key]!!
                govRoute.direction = nwfbRoute.direction
                govRoute.info.boundIds = nwfbRoute.info.boundIds
                govResult.put(key, govRoute)
            } else {
                govResult.put(key, nwfbRoute)
            }
        }

        // 3. Update ETA indicator to Gov Parents Routes
        for((etaCompany, etaList) in etaRoutesResult) {
            etaList?.forEach { routeNo ->
                val key = etaCompany + routeNo
                if (govResult.contains(key)) {
                    val govRoute = govResult[key]!!
                    govRoute.eta = true
                    govResult.put(key, govRoute)
                } else {
                    val newRoute = ConnectionHelper.getParentRoute(RouteKey(etaCompany, routeNo, -1L, -1L))
                    if (newRoute != null)
                        govResult.put(key, newRoute)
                }
            }
        }

        // 4. Convert Gov Parents Routes to list and sort it
        val routes = govResult.values.toMutableList()
        if (routes.size > 0) {
            routes.sort()
            for (i in routes.indices) {
                routes[i].displaySeq = i + 1L
                routes[i].updateTime = t
            }

            AppHelper.db.parentRouteDao().insertOrUpdate(routes, t)
        }

        return null
    }

    override fun getParentRoute(routeKey: RouteKey): Route? {
        return null
    }

    override fun getChildRoutes(parentRoute: Route) {
        return
    }

    override fun getStops(route: Route, needEtaUpdate: Boolean) {
        return
    }

    override fun updateEta(stop: Stop) {
        return
    }

    override fun updateEta(stops: List<Stop>) {
        return
    }
}
