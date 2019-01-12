package hoo.etahk.remote.connection

import hoo.etahk.common.Constants.Company
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.DB
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.model.custom.ParentRoutesMap
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import kotlinx.coroutines.*
import java.util.*

object BusConnection : BaseConnection {

    override fun getEtaRoutes(company: String): List<String>? {
        return null
    }

    /**
     * Get List of Parent Routes
     *
     * @param company company code
     * @return map of route no to list of parent routes
     */
    override fun getParentRoutes(company: String): ParentRoutesMap? {
        val t = Utils.getCurrentTimestamp()

        // 1. Get Result from Multiple Sources
        val parentRoutesResult: SortedMap<String, ParentRoutesMap?> =
            sortedMapOf(Company.GOV to null, Company.KMB to null, Company.NLB to null, Company.NWFB to null)

        val etaRoutesResult: SortedMap<String, List<String>?> =
            sortedMapOf(Company.KMB to null, Company.NWFB to null)

        try {
            runBlocking {
                val jobs = arrayListOf<Job>()

                parentRoutesResult.keys.forEach { company ->
                    jobs += GlobalScope.launch(Dispatchers.DB) {
                        parentRoutesResult[company] = ConnectionHelper.getParentRoutes(company)
                    }
                }

                etaRoutesResult.keys.forEach { company ->
                    jobs += GlobalScope.launch(Dispatchers.DB) {
                        etaRoutesResult[company] = ConnectionHelper.getEtaRoutes(company)
                    }
                }

                jobs.forEach { it.join() }
            }
        } catch (e: Exception) {
            loge("getParentRoutes failed!", e)
        }

        parentRoutesResult.keys.forEach {
            logd("onResponse $it = ${parentRoutesResult[it]?.size}")
        }

        parentRoutesResult.values.forEach { if (it == null || it.size == 0) return null }
        //etaRoutesResult.values.forEach { if (it == null) return null }

        // 2. Merge Others Parents Routes into Gov Parents Routes
        val govResult = parentRoutesResult[Company.GOV]!!
        val newResult = ParentRoutesMap()

        parentRoutesResult.filter { it.key != Company.GOV && it.value != null }.forEach { (company, parentRoutesMap) ->
            for(companyRoute in parentRoutesMap!!.getAll()) {
                val govRoute = govResult.get(companyRoute.routeKey)
                val newRouteList = newResult.get(companyRoute.routeKey.routeNo)
                var newRoute: Route? = null

                // Check if any route inside newRouteList is same as companyRoute
                if (govRoute == null && company == Company.NWFB && !newRouteList.isNullOrEmpty()) {
                    newRouteList.forEach {
                        if ( it.companyDetails.size == 1 &&
                             ( it.routeKey.company == Company.KMB ||
                               ( it.routeKey.company == Company.LWB && companyRoute.routeKey.company == Company.CTB ) ) ) {
                            // Compare from / to
                            if ( Utils.isLocationMatch(it.from.value, companyRoute.from.value) ||
                                 Utils.isLocationMatch(it.to.value, companyRoute.to.value) ||
                                 Utils.isLocationMatch(it.from.value, companyRoute.to.value) ||
                                 Utils.isLocationMatch(it.to.value, companyRoute.from.value) ) {
                                it.companyDetails = listOf(it.companyDetails[0], companyRoute.routeKey.company)
                                newRoute = it
                            }
                        }
                    }
                }

                when {
                    govRoute != null -> mergeRoute(company, govRoute, companyRoute)
                    newRoute != null -> mergeRoute(company, newRoute!!, companyRoute)
                    else -> newResult.add(companyRoute)
                }
            }
        }

        // 3. Update ETA indicator to Gov Parents Routes
        for((etaCompany, etaList) in etaRoutesResult) {
            etaList?.forEach { routeNo ->
                val govRoute = govResult.get(etaCompany, routeNo)
                val newRoute = newResult.get(etaCompany, routeNo)

                if (govRoute != null) {
                    govRoute.eta = true
                } else if (newRoute != null) {
                    newRoute.eta = true
                } else {
                    val etaRoute = ConnectionHelper.getParentRoute(RouteKey(etaCompany, routeNo, -1L, -1L))
                    if (etaRoute != null)
                        newResult.add(etaRoute)
                }
            }
        }

        // 4. Merge Result into list
        val routes = govResult.getAll().toMutableList()
        routes.addAll(newResult.getAll())

        logd("govResult = ${govResult.size}")
        logd("newResult = ${newResult.size}")
        logd("merged = ${routes.size}")

        if (routes.size > 0) {
            // 5. Sort Parents Routes list
            routes.sort()
            for (i in routes.indices) {
                routes[i].displaySeq = i + 1L
                routes[i].updateTime = t
            }

            // 6. create variant for multi company routes
            val variantRoutes = mutableListOf<Route>()
            routes.filter { it.companyDetails.size > 1 }.forEach {
                val variantRoute = it.copy()
                variantRoute.routeKey = it.routeKey.copy(
                    company = it.companyDetails[1],
                    variant = 1L)
                it.info = variantRoute.info.copy(boundIds = listOf())
                variantRoutes.add(variantRoute)
            }
            routes.addAll(variantRoutes)

            logd("variantRoutes = ${variantRoutes.size}")
            logd("merged + variantRoutes = ${routes.size}")

            // 7. Insert into Database
            AppHelper.db.parentRouteDao().insertOrUpdate(routes, t)
            logd("Finish Update")
        }

        // return result for unit test only
        if (Utils.isUnitTest) {
            val busResult = ParentRoutesMap(ignoreConstraint = true)
            busResult.addAll(routes)
            logd("busResult = ${busResult.size}")
            return busResult
        }

        return null
    }

    private fun mergeRoute(company: String, baseRoute: Route, companyRoute: Route) {
        baseRoute.direction = companyRoute.direction // TODO: need to remove when offline data is ready
        when (company) {
            Company.KMB ->  {
                if ( companyRoute.boundCount > 1 &&
                    ( Utils.isLocationMatch(baseRoute.from.value, companyRoute.to.value) ||
                      Utils.isLocationMatch(companyRoute.from.value, baseRoute.to.value) ) ) {
                    with(baseRoute.from) {
                        baseRoute.from = baseRoute.to
                        baseRoute.to = this
                    }
                }
            }
            Company.NLB -> {
                baseRoute.from = companyRoute.from
                baseRoute.to = companyRoute.to
                baseRoute.details = companyRoute.details
            }
            Company.NWFB -> {
                if ( !baseRoute.companyDetails.contains(companyRoute.routeKey.company) ) {
                    baseRoute.companyDetails = listOf(companyRoute.info.boundIds[0], companyRoute.routeKey.company)
                }
                if ( companyRoute.info.boundIds.size > 1 &&
                    ( Utils.isLocationMatch(baseRoute.from.value, companyRoute.to.value) ||
                      Utils.isLocationMatch(companyRoute.from.value, baseRoute.to.value) ) ) {
                    baseRoute.info.boundIds = listOf(companyRoute.info.boundIds[1], companyRoute.info.boundIds[0])
                } else {
                    baseRoute.info.boundIds = companyRoute.info.boundIds
                }
            }
        }
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
