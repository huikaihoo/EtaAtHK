package hoo.etahk.remote.connection

import com.google.firebase.perf.metrics.AddTrace
import com.google.gson.JsonSyntaxException
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Constants.Company
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.tools.ParentRoutesMap
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.EtaResult
import hoo.etahk.model.json.Info
import hoo.etahk.model.json.StringLang
import hoo.etahk.remote.api.GistApi
import hoo.etahk.remote.api.TramApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.koin.core.KoinComponent

open class TramConnection(
    private val tram: TramApi,
    private val tramEta: TramApi,
    private val gist: GistApi): BaseConnection, KoinComponent {

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
     * @return map of route no to list of parent routes
     */
    override fun getParentRoutes(company: String): ParentRoutesMap? {
        val t = Utils.getCurrentTimestamp()
        val result = ParentRoutesMap()

        try {
            val response = tram.getDatabase().execute()

            if (response.isSuccessful) {
                val rawLines = (response.body()?.string() ?: "").split("\n".toRegex()).map {
                    it.trim().trimEnd(',').trim()
                }

                // 1. Get Parent Route
                val parentRoute = toParentRoute(t)
                result.add(parentRoute)
                AppHelper.db.childRouteDao().insertOrUpdate(listOf(parentRoute), t)

                // 2. Get Child Routes and Stops
                var bound = 0L
                var childRoute: Route? = null
                val stops = mutableListOf<Stop>()

                for (line in rawLines) {
                    // Start of Child Route
                    val matchResult = "var stopsArray([a-zA-z]*)[ ]*=".toRegex().find(line)
                    if (matchResult != null && matchResult.groupValues.size >= 2) {
                        val boundStr = matchResult.groupValues[1].trim()
                        if (boundStr.isNotBlank()) {
                            bound++
                            childRoute = toChildRoute(parentRoute, bound, 1L, StringLang.newInstance(boundStr), t)
                            AppHelper.db.childRouteDao().insertOrUpdate(listOf(childRoute), t)
                        }
                    }

                    // End of Child Route
                    if (line.matches("][ ]*;".toRegex())) {
                        if (childRoute != null && stops.isNotEmpty()) {
                            AppHelper.db.stopDao().insertOrUpdate(childRoute.routeKey, stops, t)
                        }
                        childRoute = null
                        stops.clear()
                    }

                    // Get Stop
                    if ( childRoute != null &&
                         line.isNotBlank() &&
                         line.startsWith('[') &&
                         line.endsWith(']') &&
                         !line.matches("//".toRegex()) ) {
                        try {
                            val records = AppHelper.gson.fromJson(line, Array<String>::class.java)
                            if (records.size >= Constants.Stop.TRAM_STOP_RECORD_SIZE) {
                                stops.add(toStop(childRoute, stops.size.toLong(), records, t))
                            }
                        } catch (e: JsonSyntaxException) {
                            loge("Not a valid tram stop record [$line]")
                        }
                    }
                }

                logd("Finish Update")
            }
        } catch (e: Exception) {
            loge("getParentRoutes failed!", e)
        }
        
        return result
    }

    private fun toParentRoute(t: Long): Route {
        return Route(
            routeKey = RouteKey(company = Company.TRAM,
                routeNo = "TRAM",
                bound = 0L,
                variant = 0L),
            direction = 2L,
            specialCode = 0L,
            companyDetails = listOf(Company.TRAM),
            updateTime = t
        )
    }

    private fun toChildRoute(parentRoute: Route,
                             bound: Long,
                             variant: Long,
                             to: StringLang,
                             t: Long): Route
    {
        return Route(
            routeKey = parentRoute.routeKey.copy(bound = bound, variant = variant),
            direction = parentRoute.childDirection,
            specialCode = parentRoute.specialCode,
            companyDetails = parentRoute.companyDetails,
            to = to,
            displaySeq = parentRoute.displaySeq,
            typeSeq = parentRoute.typeSeq,
            updateTime = t
        )
    }

    private fun toStop(route: Route, seq: Long, records: Array<String>, t: Long): Stop {
        return Stop(
            routeKey = route.routeKey.copy(),
            seq = seq,
            name = StringLang(records[Constants.Stop.TRAM_STOP_RECORD_NAME_TC], records[Constants.Stop.TRAM_STOP_RECORD_NAME_EN], records[Constants.Stop.TRAM_STOP_RECORD_NAME_SC]),
            to = route.to,
            details = StringLang.newInstance(records[Constants.Stop.TRAM_STOP_RECORD_STOP_ID]),
            latitude = records[Constants.Stop.TRAM_STOP_RECORD_LATITUDE].toDouble(),
            longitude = records[Constants.Stop.TRAM_STOP_RECORD_LONGITUDE].toDouble(),
            fare = 2.6,
            info = Info(stopId = records[Constants.Stop.TRAM_STOP_RECORD_STOP_ID]),
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
        if (needEtaUpdate) {
            val stops = AppHelper.db.stopDao().selectOnce(
                route.routeKey.company,
                route.routeKey.routeNo,
                route.routeKey.bound,
                route.routeKey.variant)
            updateEta(stops)
        }
        return
    }

    /**
     * Get url of timetable of route
     *
     * @param route Child Route
     */
    override fun getTimetableUrl(route: Route): String? {
        return tram.getTimetable().request().url().toString()
    }

    override fun getTimetable(route: Route): String? {
        return null
    }

    override fun updateEta(stop: Stop) {
        return
    }

    @AddTrace(name = "TramConnection_updateEta")
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
                                tramEta.getEta(stop.info.stopId).execute()

                            if (response.isSuccessful) {
                                val responseStr = response.body()?.string()
                                val etaResults = mutableListOf<EtaResult>()
                                val tramResponse = Jsoup.parse(responseStr).body().getElementsByTag("metadata")

                                tramResponse.forEach {
                                    etaResults.add(toEtaResult(stop, it))
                                    //logd(it)
                                }

                                if (responseStr.isNullOrBlank() || tramResponse.isEmpty()) {
                                    etaResults.add(toEtaResult(stop, AppHelper.getString(R.string.eta_msg_no_eta_info)))
                                }

                                if (etaResults.isNotEmpty()) {
                                    stop.etaStatus = Constants.EtaStatus.SUCCESS
                                    stop.etaResults = etaResults
                                }
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

    private fun toEtaResult(stop: Stop, element: Element): EtaResult {
        val etaTime = Utils.dateStrToTimestamp(element.attr(Constants.Eta.TRAM_ETA_RECORD_ETA_TIME), "MMM dd yyyy  h:mma")
        val msg = Utils.timestampToTimeStr(etaTime) + " " + AppHelper.getString(R.string.to) + element.attr(Constants.Eta.TRAM_ETA_RECORD_DEST_TC)

        return EtaResult(
            company = stop.routeKey.company,
            etaTime = etaTime,
            msg = Utils.timeStrToMsg(msg),
            scheduleOnly = false,
            gps = true)
    }
}