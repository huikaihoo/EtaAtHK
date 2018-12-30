package hoo.etahk.remote.connection

import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Constants.Company
import hoo.etahk.common.Constants.Time.ONE_MINUTE_IN_SECONDS
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.DB
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.EtaResult
import hoo.etahk.model.json.Info
import hoo.etahk.model.json.StringLang
import hoo.etahk.remote.request.NlbEtaReq
import hoo.etahk.remote.response.NlbDatabaseRes
import hoo.etahk.view.App
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.*

object NlbConnection: BaseConnection {

    /***************
     * Shared
     ***************/
    private fun getFromTo(routeName: String, index: Int): String {
        return routeName.split(">".toRegex())[index].trim()
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
    override fun getParentRoutes(company: String): HashMap<String, Route>? {
        val t = Utils.getCurrentTimestamp()
        val result = HashMap<String, Route>()
        val childRoutes = mutableListOf<Route>()

        val response = ConnectionHelper.nlb.getDatabase().execute()

        if (response.isSuccessful) {
            val nlbDatabaseRes = response.body()!!

            // 1. Get Parent and Child Routes
            val routesMap = (nlbDatabaseRes.routes ?: listOf()).groupBy{ it -> Company.NLB + it!!.routeNo }

            for((key, routes) in routesMap) {
                val routePair = toRoutePair(routes.sortedBy { it?.routeId?.toInt() ?: 0 }, t)
                result[key] = routePair.first
                childRoutes.addAll(routePair.second)
            }

            // 2. Insert Child Routes into DB
            val childRoutesMap = childRoutes.groupBy{
                RouteKey(company = it.routeKey.company,
                    routeNo = it.routeKey.routeNo,
                    bound = it.routeKey.bound,
                    variant = 0L)
            }
            childRoutesMap.forEach { (routeKey, childRoutes) ->
                AppHelper.db.childRouteDao().insertOrUpdate(childRoutes, t)
            }
            logd("After insert child routes")

            // 3. Get Stops
            val routeStopsMap = (nlbDatabaseRes.routeStops ?: listOf()).groupBy{ it!!.routeId!! }
            val stopsMap = (nlbDatabaseRes.stops ?: listOf()).groupBy{ it!!.stopId!! }
            childRoutes.forEach { route ->
                val stops = mutableListOf<Stop>()

                routeStopsMap[route.info.rdv]?.forEach { rawRouteStop ->
                    val rawStops = stopsMap[rawRouteStop?.stopId ?: ""]
                    if (!rawStops.isNullOrEmpty()) {
                        val rawStop = rawStops[0]!!
                        stops.add(toStop(route, rawRouteStop!!, rawStop, t))
                    }
                }

                GlobalScope.launch(Dispatchers.DB) {
                    if (stops.isNotEmpty()) {
                        AppHelper.db.stopDao().insertOrUpdate(route.routeKey, stops, t)
                    }
                }
            }

            logd("onResponse ${result.size}")
        }

        return result
    }

    private fun toRoutePair(rawRoutes: List<NlbDatabaseRes.Route?>, t: Long): Pair<Route, List<Route>> {
        val parentRoute = toRoute(rawRoutes, t)
        val childRoutes = mutableListOf<Route>()

        rawRoutes.forEach { rawRoute ->
            if (rawRoute != null) {
                var bound = if (childRoutes.isEmpty()) 1L else 2L
                var variant = 1L
                val from = StringLang(getFromTo(rawRoute.routeNameC!!, 0),
                    getFromTo(rawRoute.routeNameE!!, 0),
                    getFromTo(rawRoute.routeNameS!!, 0))
                val to = StringLang(getFromTo(rawRoute.routeNameC, 1),
                    getFromTo(rawRoute.routeNameE, 1),
                    getFromTo(rawRoute.routeNameS, 1))

                if (bound > 1) {
                    childRoutes.forEach { childRoute ->
                        if (childRoute.routeKey.variant == 1L) {
                            if (from.value == childRoute.from.value || to.value == childRoute.to.value) {
                                bound = childRoute.routeKey.bound
                            }
                        }
                        if (childRoute.routeKey.bound == bound){
                            variant = Math.max(variant, childRoute.routeKey.variant + 1)
                        }
                    }
                }

                childRoutes.add(toChildRoute(parentRoute, bound, variant, from, to, rawRoute, t))
            }
        }

        return Pair(parentRoute, childRoutes.toList())
    }

    private fun toRoute(rawRoutes: List<NlbDatabaseRes.Route?>, t: Long): Route {
        val rawRoute = rawRoutes[0] ?: NlbDatabaseRes.Route()

        val direction = when {
            rawRoute.routeNameC!!.contains("循環線".toRegex()) || rawRoute.routeNameE!!.contains("Circular".toRegex()) -> 0L
            rawRoutes.size > 1 -> 2L
            else -> 1L
        }

        return Route(
            routeKey = RouteKey(company = Company.NLB,
                routeNo = rawRoute.routeNo!!,
                bound = 0L,
                variant = 0L),
            direction = direction,
            specialCode = 0L,
            companyDetails = listOf(Company.NLB),
            from = StringLang(getFromTo(rawRoute.routeNameC, 0),
                getFromTo(rawRoute.routeNameE!!, 0),
                getFromTo(rawRoute.routeNameS!!, 0)),
            to = StringLang(getFromTo(rawRoute.routeNameC, 1),
                getFromTo(rawRoute.routeNameE, 1),
                getFromTo(rawRoute.routeNameS, 1)),
            details = StringLang(rawRoute.additionalDescriptionC ?: "",
                rawRoute.additionalDescriptionE ?: "",
                rawRoute.additionalDescriptionS ?: ""),
            updateTime = t
        )
    }

    private fun toChildRoute(parentRoute: Route,
                             bound: Long,
                             variant: Long,
                             from: StringLang,
                             to: StringLang,
                             rawRoute: NlbDatabaseRes.Route,
                             t: Long): Route
    {
        return Route(
            routeKey = parentRoute.routeKey.copy(bound = bound, variant = variant),
            direction = parentRoute.childDirection,
            specialCode = parentRoute.specialCode,
            companyDetails = parentRoute.companyDetails,
            from = from,
            to = to,
            details = StringLang.newInstance(rawRoute.additionalDescriptionC!!),
            info = Info(rdv = rawRoute.routeId!!),
            displaySeq = parentRoute.displaySeq,
            typeSeq = parentRoute.typeSeq,
            updateTime = t
        )
    }

    private fun toStop(route: Route,
                       rawRouteStop: NlbDatabaseRes.RouteStop,
                       rawStop: NlbDatabaseRes.Stop,
                       t: Long): Stop
    {
        return Stop(
            routeKey = route.routeKey.copy(),
            seq = rawRouteStop.stopSequence!!,
            name = StringLang(rawStop.stopNameC?: "", rawStop.stopNameE?: "", rawStop.stopNameS?: ""),
            to = route.to,
            details = StringLang((rawStop.stopLocationC?: ""), rawStop.stopLocationE?: "", rawStop.stopNameS?: ""),
            longitude = rawStop.longitude ?: 0.0,
            latitude = rawStop.latitude ?: 0.0,
            fare = rawRouteStop.fare ?: 0.0,
            info = Info(rdv = route.info.rdv,
                stopId = rawRouteStop.stopId ?: "",
                fareHoliday = rawRouteStop.fareHoliday ?: rawRouteStop.fare ?: -1.0,
                partial = rawRouteStop.someDepartureObserveOnly ?: 0L),
            updateTime = t
        )
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
        val t = Utils.getCurrentTimestamp()

        try {
            runBlocking {
                val jobs = arrayListOf<Job>()

                stops.forEach { stop ->
                    jobs += launch(Dispatchers.Default) {
                        stop.etaStatus = Constants.EtaStatus.FAILED
                        stop.etaUpdateTime = t
                        try {
                            val response =
                                ConnectionHelper.nlbEta.getEta(
                                    NlbEtaReq(routeId = stop.info.rdv,
                                        stopId = stop.info.stopId)
                                ).execute()

                            if (response.isSuccessful) {
                                val nlbEtaRes = response.body()
                                val etaResults = mutableListOf<EtaResult>()

                                if (nlbEtaRes?.estimatedArrivalTime?.html != null) {
                                    val elements =  Jsoup.parse(nlbEtaRes.estimatedArrivalTime.html).body().getElementsByTag("div")
                                    elements.forEach {
                                        val msg = it.text()
                                        // TODO("Need to Support English")
                                        if (msg.contains("沒有班次途經本站".toRegex())) {
                                            etaResults.add(toEtaResult(stop, App.instance.getString(R.string.eta_msg_not_in_service_hours)))
                                        } else if (!msg.contains("請提早到達巴士站候車".toRegex())) {
                                            etaResults.add(toEtaResult(stop, it))
                                        }
                                    }
                                    //logd(AppHelper.gson.toJson(etaResults))
                                    if (etaResults.isEmpty()) {
                                        etaResults.add(
                                            toEtaResult(stop, App.instance.getString(R.string.eta_msg_no_eta_info))
                                        )
                                    }
                                }

                                if (!etaResults.isEmpty()) {
                                    stop.etaStatus = Constants.EtaStatus.SUCCESS
                                    stop.etaResults = etaResults
                                }

                                stop.etaStatus = Constants.EtaStatus.SUCCESS
                                stop.etaResults = etaResults
                            }
                        } catch (e: Exception) {
                            loge("updateEta::stops.forEach failed!", e)
                            stop.etaStatus = Constants.EtaStatus.NETWORK_ERROR
                        }
                    }
                }
                jobs.forEach { it.join() }
            }

            AppHelper.db.stopDao().updateOnReplace(stops)
        } catch (e: Exception) {
            loge("updateEta failed!", e)
        }
    }

    private fun getEtaTime(msg: String): Long {
        val now = Utils.getCurrentTimestamp()

        return if (msg.contains("到達/離開")) {
            now
        } else {
            val matchResult = "([0-9]+)分鐘".toRegex().find(msg)
            if (!matchResult?.groupValues.isNullOrEmpty() && matchResult!!.groupValues.size >= 2) {
                now + matchResult.groupValues[1].toLong() * ONE_MINUTE_IN_SECONDS
            } else {
                -1L
            }
        }
    }

    private fun toEtaResult(stop: Stop, element: Element): EtaResult {
        var msg = element.text()
        val etaTime = getEtaTime(msg)
        val scheduledOnly = Utils.isScheduledOnly(msg)

        msg =  msg.replace("到達/離開".toRegex(), "")
                    .replace("([0-9]+)分鐘".toRegex(), "")
                    .trim()
        msg = Utils.timestampToTimeStr(etaTime) + " " + msg

        return EtaResult(
            company = stop.routeKey.company,
            etaTime = etaTime,
            msg = Utils.timeStrToMsg(msg),
            scheduleOnly = scheduledOnly,
            gps = etaTime >= 0L && !scheduledOnly)
    }
}