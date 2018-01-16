package hoo.etahk.remote.connection

import android.util.Log
import com.mcxiaoke.koi.HASH
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.common.tools.Separator
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.StringLang
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

object GovConnection: BaseConnection{

    private const val TAG = "GovConnection"

    /***************
     * Shared
     ***************/
    fun getSystemCode(): String {
        val hexes = "0123456789ABCDEF"
        val random = String.format("%05d", Random().nextInt(10000))
        val timestamp = Utils.getCurrentTimestamp().toString()
        val timestampStr = (timestamp.substring(2, 3) + timestamp.substring(9, 10)
                + timestamp.substring(4, 5) + timestamp.substring(6, 7)
                + timestamp.substring(3, 4) + timestamp.substring(0, 1)
                + timestamp.substring(8, 9) + timestamp.substring(7, 8)
                + timestamp.substring(5, 6) + timestamp.substring(1, 2))

        val raw = HASH.sha256Bytes((timestampStr + "mmtydwtd" + random).toByteArray())
        val hex = StringBuilder(raw.size * 2)

        for (b in raw) {
            hex.append(hexes[(b.toInt() and 240) shr 4]).append(hexes[b.toInt() and 15])
        }

        return timestampStr + hex.toString().toLowerCase() + random
    }

    override fun getEtaRoutes(company: String): List<String>? {
        return null
    }

    /*********************
     * Get Parent Routes *
     *********************/
    override fun getParentRoutes(company: String): HashMap<String, Route>? {
        val t = Utils.getCurrentTimestamp()
        val temp = HashMap<String, MutableList<Route>>()
        val result = HashMap<String, Route>()

        val response = ConnectionHelper.gov.getParentRoutes(syscode = getSystemCode()).execute()
        if (response.isSuccessful) {
            val separator = Separator("\\|\\*\\|".toRegex(), "\\|\\|".toRegex(), Constants.Route.GOV_ROUTE_RECORD_SIZE)

            //Log.d(TAG, "onResponse ${separator.columnSize}")
            separator.original = response.body()?.string() ?: ""
            separator.result.forEach {
                toRoutes(it, t).forEach { route ->
                    val key = route.routeKey.company + route.routeKey.routeNo
                    if (temp.contains(key)) {
                        val routes = temp[key]!!
                        routes.add(route)
                        temp.put(key, routes)
                    } else {
                        temp.put(key, mutableListOf(route))
                    }
                }
                //result.putAll(routes.associate{ Pair(it.routeKey.company + it.routeKey.routeNo, it) })
            }
            Log.d(TAG, "onResponse ${separator.result.size}")

            for((key, routes) in temp) {
                if (routes.size == 1) {
                    result.put(key, routes[0])
                } else if (routes.size > 1){
                    result.put(key, mergeVariantRoutes(routes))
                }
            }
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

    fun getParentRoutesOld(company: String): HashMap<String, Route>? {
        Log.d(TAG, "Start")
        ConnectionHelper.gov.getParentRoutes(syscode = getSystemCode())
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        launch(CommonPool) {
                            val t = Utils.getCurrentTimestamp()
                            val routes = mutableListOf<Route>()
                            val separator = Separator("\\|\\*\\|".toRegex(), "\\|\\|".toRegex(), Constants.Route.GOV_ROUTE_RECORD_SIZE)

                            //Log.d(TAG, "onResponse ${separator.columnSize}")
                            separator.original = response.body()?.string() ?: ""
                            separator.result.forEach {
                                routes.addAll(toRoutes(it, t))
                            }
                            Log.d(TAG, "onResponse ${separator.result.size}")

                            if (routes.size > 0) {
                                routes.sort()
                                for (i in routes.indices) {
                                    routes[i].seq = i + 1L
                                }

                                AppHelper.db.parentRoutesDao().insertOrUpdate(routes, t)
                            }
                        }
                    }
                })
        return null
    }

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

    override fun updateEta(stop: Stop) {
        return
    }

    override fun updateEta(stops: List<Stop>) {
        return
    }
}