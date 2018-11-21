package hoo.etahk.remote.connection

import hoo.etahk.common.Constants.Company
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import kotlinx.coroutines.*

object BusConnection : BaseConnection {

    override fun getEtaRoutes(company: String): List<String>? {
        return null
    }

    /**
     * Get List of Parent Routes
     *
     * @param company company code
     * @return map of route no to its parent route
     */
    override fun getParentRoutes(company: String): HashMap<String, Route>? {
        val t = Utils.getCurrentTimestamp()

        // 1. Get Result from Multiple Sources
        val parentRoutesResult = HashMap<String, HashMap<String, Route>?>()
        parentRoutesResult.putAll(mapOf(Company.GOV to null, Company.NWFB to null, Company.NLB to null))

        val etaRoutesResult = HashMap<String, List<String>?>()
        etaRoutesResult.putAll(mapOf(Company.KMB to null, Company.NWFB to null))

        try {
            runBlocking {
                val jobs = arrayListOf<Job>()

                parentRoutesResult.keys.forEach { company ->
                    jobs += GlobalScope.launch(Dispatchers.Default) {
                        parentRoutesResult[company] = ConnectionHelper.getParentRoutes(company)
                    }
                }

                etaRoutesResult.keys.forEach { company ->
                    jobs += GlobalScope.launch(Dispatchers.Default) {
                        etaRoutesResult[company] = ConnectionHelper.getEtaRoutes(company)
                    }
                }

                jobs.forEach { it.join() }
            }
        } catch (e: Exception) {
            loge("getParentRoutes failed!", e)
        }

        parentRoutesResult.keys.forEach { company ->
            logd("onResponse $company = ${parentRoutesResult[company]?.size}")
        }

        parentRoutesResult.values.forEach { if (it == null || it.isEmpty()) return null }
        //etaRoutesResult.values.forEach { if (it == null) return null }

        // 2. Merge Others Parents Routes into Gov Parents Routes
        val govResult = parentRoutesResult[Company.GOV]!!

        parentRoutesResult.forEach { (company, parentRoutes) ->
            if (company != Company.GOV && parentRoutes != null) {
                for((key, companyRoute) in parentRoutes) {
                    if (govResult.contains(key)) {
                        val govRoute = govResult[key]!!
                        govRoute.direction = companyRoute.direction
                        when (company) {
                            Company.NWFB -> {
                                govRoute.info.boundIds = companyRoute.info.boundIds
                            }
                            Company.NLB -> {
                                govRoute.from = companyRoute.from
                                govRoute.to = companyRoute.to
                                govRoute.details = companyRoute.details
                            }
                        }
                        govResult[key] = govRoute
                    } else {
                        govResult[key] = companyRoute
                    }
                }
            }
        }

        // 3. Update ETA indicator to Gov Parents Routes
        for((etaCompany, etaList) in etaRoutesResult) {
            etaList?.forEach { routeNo ->
                val key = etaCompany + routeNo
                if (govResult.contains(key)) {
                    val govRoute = govResult[key]!!
                    govRoute.eta = true
                    govResult[key] = govRoute
                } else {
                    val newRoute = ConnectionHelper.getParentRoute(RouteKey(etaCompany, routeNo, -1L, -1L))
                    if (newRoute != null)
                        govResult[key] = newRoute
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
