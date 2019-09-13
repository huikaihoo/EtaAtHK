package hoo.etahk.remote.connection

import com.mcxiaoke.koi.HASH
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.tools.ParentRoutesMap
import hoo.etahk.common.tools.Separator
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.StringLang
import hoo.etahk.remote.api.GovApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class GovConnection(private val gov: GovApi): BaseConnection {

    /***************
     * Shared
     ***************/
    fun getSystemCode(): String {
        var random = (0 until 10000).random().toString()
        random += "0".repeat(5 - random.length)

        val timestamp = Utils.getCurrentTimestamp().toString()
        val timestampStr = (timestamp.substring(2, 3) + timestamp.substring(9, 10)
                + timestamp.substring(4, 5) + timestamp.substring(6, 7)
                + timestamp.substring(3, 4) + timestamp.substring(0, 1)
                + timestamp.substring(8, 9) + timestamp.substring(7, 8)
                + timestamp.substring(5, 6) + timestamp.substring(1, 2))

        return timestampStr + HASH.sha256((timestampStr + "mmtydwtd" + random).toByteArray()).toLowerCase() + random
    }

    override fun getEtaRoutes(company: String): List<String>? {
        return null
    }

    /**
     * Get List of Parent Routes
     *
     * @param company company code
     * @return map of route no to its parent route
     */
    override fun getParentRoutes(company: String): ParentRoutesMap? {
        val t = Utils.getCurrentTimestamp()
        val temp = HashMap<String, MutableList<Route>>()    // (Company + routeNo) to Route
        val temp2 = HashMap<String, Route>()
        val result = ParentRoutesMap()

        try {
            val response = gov.getParentRoutes(syscode = getSystemCode()).execute()
            if (response.isSuccessful) {
                val separator = Separator(
                    "\\|\\*\\|".toRegex(),
                    "\\|\\|".toRegex(),
                    Constants.Route.GOV_ROUTE_RECORD_SIZE
                )

                //logd("onResponse ${separator.columnSize}")
                separator.original = response.body()?.string() ?: ""
                separator.result.forEach {
                    val route = toRoute(it, t)
                    val key = route.routeKey.company + route.routeKey.routeNo
                    if (temp.contains(key)) {
                        val routes = temp[key]!!
                        routes.add(route)
                        temp[key] = routes
                    } else {
                        temp[key] = mutableListOf(route)
                    }
                    //result.putAll(routes.associate{ Pair(it.routeKey.company + it.routeKey.routeNo, it) })
                }

                for ((key, routes) in temp) {
                    if (routes.size == 1) {
                        temp2[key] = routes[0]
                    } else if (routes.size > 1) {
                        temp2[key] = mergeVariantRoutes(routes)
                    }
                }

                result.addAll(temp2.values)
                logd("onResponse separator.result ${separator.result.size}")
                logd("onResponse ${result.size}")
            }
        } catch (e: Exception) {
            loge("getParentRoutes failed!", e)
        }

        return result
    }

    private fun mergeVariantRoutes(routes: List<Route>): Route {
        // 1. Return Non-Special
        routes.forEach {
            if (it.specialCode == 0L || it.specialCode == 2L)
                return it
        }

        // 2. Return direction > 1
        routes.forEach {
            if (it.direction > 1L)
                return it
        }

        // 3. Return direction == 0
        routes.forEach {
            if (it.direction == 0L)
                return it
        }

        // 4. Special Handle (multiple one way non-circular special routes: merge to one)
        // TODO("Need to Support English")
        val newFrom = routes[0].from
        val newTo = routes[0].to
        var newDirection = 1L

        for (i in routes.indices) {
            if (i == 0)
                continue

            val from = routes[i].from
            val to = routes[i].to

            if (newFrom.value == from.value) {
                if (to.value != to.value)
                    newTo.value += " / " + to.value
            } else if (newTo.value == to.value) {
                newFrom.value += " / " + from.value
            } else if (newFrom.value == to.value) {
                newDirection = 2L
                if (to.value != from.value)
                    newTo.value += " / " + from.value
            } else if (newTo.value == from.value) {
                newDirection = 2L
                newFrom.value += " / " + to.value
            }
        }

        return routes[0].copy(direction = newDirection, from = newFrom, to = newTo)
    }

    private fun toRoute(records: List<String>, t: Long): Route {
        val companies = records[Constants.Route.GOV_ROUTE_RECORD_COMPANIES].split("\\+".toRegex())
        val routeNo = records[Constants.Route.GOV_ROUTE_RECORD_ROUTE_NO].replace("[ ]+.*".toRegex(), "").trim()

        val direction = when {
            records[Constants.Route.GOV_ROUTE_RECORD_BOUND_COUNT].toInt() > 1 -> records[Constants.Route.GOV_ROUTE_RECORD_BOUND_COUNT].toLong()
            records[Constants.Route.GOV_ROUTE_RECORD_CIRCULAR].toInt() > 0 -> 0L
            else -> 1L
        }

        return Route(
            routeKey = RouteKey(company = companies[0],
                routeNo = routeNo,
                bound = 0L,
                variant = 0L),
            direction = direction,
            specialCode = records[Constants.Route.GOV_ROUTE_RECORD_SPECIAL].toLong(),
            companyDetails = companies,
            from = StringLang.newInstance(records[Constants.Route.GOV_ROUTE_RECORD_FROM]),
            to = StringLang.newInstance(records[Constants.Route.GOV_ROUTE_RECORD_TO]),
            details = StringLang.newInstance(records[Constants.Route.GOV_ROUTE_RECORD_DETAILS]),
            updateTime = t
        )

    }

    @Deprecated("Use 'getParentRoutes(company: String): HashMap<String, Route>?' instead.")
    fun getParentRoutesOld(company: String): HashMap<String, Route>? {
        gov.getParentRoutes(syscode = getSystemCode())
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        GlobalScope.launch(Dispatchers.Default) {
                            val t = Utils.getCurrentTimestamp()
                            val routes = mutableListOf<Route>()
                            val separator = Separator("\\|\\*\\|".toRegex(), "\\|\\|".toRegex(), Constants.Route.GOV_ROUTE_RECORD_SIZE)

                            //logd("onResponse ${separator.columnSize}")
                            separator.original = response.body()?.string() ?: ""
                            separator.result.forEach {
                                routes.addAll(toRoutes(it, t))
                            }
                            logd("onResponse ${separator.result.size}")

                            if (routes.size > 0) {
                                routes.sort()
                                for (i in routes.indices) {
                                    routes[i].displaySeq = i + 1L
                                }

                                AppHelper.db.parentRouteDao().insertOrUpdate(routes, t)
                            }
                        }
                    }
                })
        return null
    }

    @Deprecated("Use 'toRoute(records: List<String>, t: Long): Route' instead.")
    private fun toRoutes(records: List<String>, t: Long): List<Route> {
        val routes: MutableList<Route> = mutableListOf()
        val companies = records[Constants.Route.GOV_ROUTE_RECORD_COMPANIES].split("\\+".toRegex())

        val direction = when {
            records[Constants.Route.GOV_ROUTE_RECORD_BOUND_COUNT].toInt() > 1 -> records[Constants.Route.GOV_ROUTE_RECORD_BOUND_COUNT].toLong()
            records[Constants.Route.GOV_ROUTE_RECORD_CIRCULAR].toInt() > 0 -> 0L
            else -> 1L
        }

        companies.forEachIndexed { i, company ->
            if (!company.isBlank()) {
                val route = Route(
                        routeKey = RouteKey(company = company,
                                routeNo = records[Constants.Route.GOV_ROUTE_RECORD_ROUTE_NO],
                                bound = 0L,
                                variant = i.toLong()),
                        direction = direction,
                        specialCode = records[Constants.Route.GOV_ROUTE_RECORD_SPECIAL].toLong(),
                        companyDetails = companies,
                        from = StringLang.newInstance(records[Constants.Route.GOV_ROUTE_RECORD_FROM]),
                        to = StringLang.newInstance(records[Constants.Route.GOV_ROUTE_RECORD_TO]),
                        details = StringLang.newInstance(records[Constants.Route.GOV_ROUTE_RECORD_DETAILS]),
                        updateTime = t
                )
                routes.add(route)
            }
        }

        return routes.toList()
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

    override fun getTimetableUrl(route: Route): String? {
        return null
    }

    override fun getTimetable(route: Route): String? {
        return null
    }

    override fun updateEta(stop: Stop) {
        return
    }

    override fun updateEta(stops: List<Stop>) {
        return
    }
}