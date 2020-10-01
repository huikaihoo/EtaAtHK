package hoo.etahk.remote.connection

import android.util.Base64
import com.google.android.gms.common.util.Hex
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.perf.metrics.AddTrace
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.constants.SharedPrefs
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.extensions.yn
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.tools.ParentRoutesMap
import hoo.etahk.model.data.Path
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.EtaResult
import hoo.etahk.model.json.Info
import hoo.etahk.model.json.StringLang
import hoo.etahk.remote.api.GistApi
import hoo.etahk.remote.api.KmbApi
import hoo.etahk.remote.request.KmbEtaReq
import hoo.etahk.remote.request.KmbEtaV2Req
import hoo.etahk.remote.response.GistDatabaseRes
import hoo.etahk.remote.response.KmbBoundVariantRes
import hoo.etahk.remote.response.KmbEtaRes
import hoo.etahk.remote.response.KmbStopsRes
import hoo.etahk.remote.response.KmbTimetableRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import java.math.BigInteger
import java.util.Random
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

open class KmbConnection(
    private val kmb: KmbApi,
    private val kmbEta: KmbApi,
    private val gist: GistApi): BaseConnection, KoinComponent {

    /***************
     * Shared
     ***************/
    fun getEtaReq(routeNo: String, bound: String, variant: String, stopId: String, seq: String): KmbEtaReq {
        val timestamp = Utils.getCurrentTimestamp()
        val timeStr = Utils.getDateTimeString(timestamp, "yyyy-MM-dd HH:mm:ss", "UTC") + ".00."
        val sep = "--31${timeStr}13--"

        val str = routeNo + sep + bound + sep + variant + sep + stopId.replace("-", "") + sep + seq + sep + (timestamp * 1000).toString()
        return KmbEtaReq(
            token = "EA" + Base64.encodeToString(str.toByteArray(), Base64.NO_WRAP),
            t = timeStr
        )
    }

    private fun encrypt(content: String, random: BigInteger): String {
        val randomStr = random.toString(16)
        val str = "0".repeat(32 - randomStr.length) + randomStr

        // Encrypt Random String
        val keySpec = SecretKeySpec(Hex.stringToBytes("801C26C9AFB352FA4DF8C009BAB0FA72"), "AES")
        val ivParameterSpec = IvParameterSpec(Hex.stringToBytes(str))

        val instance = Cipher.getInstance("AES/CTR/NoPadding")
        instance.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec)

        return Hex.bytesToStringUppercase(instance.doFinal(content.toByteArray()))
    }

    fun getEtaV2Req(routeNo: String, bound: String, variant: String, seq: String): KmbEtaV2Req {
        val t = Utils.getDateTimeString(Utils.getCurrentTimestamp() + 2, "yyyy-MM-dd'T'HH:mm:ss'Z'", "UTC")
        val random = BigInteger(50, Random())
        val apiKey = encrypt(t, random)
        val queryString = "?lang=tc&route=${routeNo}&bound=${bound}&stop_seq=${seq}&service_type=${variant}&vendor_id=qdb9jfu6bccb8ffs&apiKey=${apiKey}&ctr=${random}"
        val d = encrypt(queryString, random)

        return KmbEtaV2Req(
            d = d,
            ctr = random.toString()
        )
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
        val gistId = SharedPrefs.gistIdKmb

        try {
            val response = gist.getGist(gistId).execute()

            logd("gistId = $gistId; isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val gistFile = response.body()?.files?.get(company.toLowerCase())
                val gistDatabaseRes =
                    if (gistFile != null) toGistDatabaseRes(company.toLowerCase(), gistFile, t, gist) else GistDatabaseRes()

                logd("gistDatabaseRes.isValid = ${gistDatabaseRes.isValid}")

                if (gistDatabaseRes.isValid) {
                    result.addAll(gistDatabaseRes.parentRoutes)

                    val childRoutesMap = gistDatabaseRes.childRoutes.groupBy {
                        RouteKey(
                            company = it.routeKey.company,
                            routeNo = it.routeKey.routeNo,
                            bound = it.routeKey.bound,
                            variant = 0L
                        )
                    }
                    childRoutesMap.forEach { (routeKey, childRoutes) ->
                        AppHelper.db.childRouteDao().insertOrUpdate(childRoutes, t)
                    }
                    logd("After insert child routes")

                    if (gistDatabaseRes.stops.isNotEmpty()) {
                        AppHelper.db.stopDao().insertOnDeleteOld(listOf(Constants.Company.KMB, Constants.Company.LWB), gistDatabaseRes.stops)
                    }
                }

                logd("response ${result.size}")
            }
        } catch (e: Exception) {
            loge("getParentRoutes failed!", e)
        }

        return result
    }

    override fun getParentRoute(routeKey: RouteKey): Route? {
        return null
    }

    /**
     * Get Child Route by Parent Route and update into DB
     *
     *  @param parentRoute parent route
     */
    override fun getChildRoutes(parentRoute: Route) {
        for (bound in 1..parentRoute.boundCount) {
            try {
                val prefix = "[${parentRoute.routeKey.routeNo}][$bound]"

                val response = kmb.getBoundVariant(
                            route = parentRoute.routeKey.routeNo,
                            bound = bound.toString()).execute()

                logd("$prefix isSuccessful = ${response.isSuccessful}")

                if (response.isSuccessful) {
                    GlobalScope.launch(Dispatchers.Default) {
                        val t = Utils.getCurrentTimestamp()
                        val kmbBoundVariantRes = response.body()
                        //logd(kmbBoundVariantRes.toString())

                        if (kmbBoundVariantRes?.data?.routes != null && kmbBoundVariantRes.data.routes.isNotEmpty()) {
                            val routes = mutableListOf<Route>()
                            (kmbBoundVariantRes.data.routes).forEach {
                                //logd("${it?.bound} == $bound")
                                assert(it!!.bound!! == bound)
                                routes.add(toChildRoute(parentRoute, it, t))
                            }

                            logd("$prefix response childroutes ${routes.size}")
                            AppHelper.db.childRouteDao().insertOrUpdate(routes, t)
                        }
                    }
                }
            } catch (e: Exception) {
                loge("getChildRoutes failed!", e)
            }
        }
    }

    private fun toChildRoute(parentRoute: Route, route: KmbBoundVariantRes.Route, t: Long): Route {
        return Route(
                routeKey = parentRoute.routeKey.copy(bound = route.bound!!, variant = route.serviceType?.trim()?.toLong()?: 1),
                direction = parentRoute.childDirection,
                specialCode = parentRoute.specialCode,
                companyDetails = parentRoute.companyDetails,
                from = StringLang(route.originChi?: "", route.originEng?: ""),
                to = StringLang(route.destinationChi?: "", route.destinationEng?: ""),
                details = StringLang(route.descChi?: "", route.descEng?: ""),
                displaySeq = parentRoute.displaySeq,
                typeSeq = parentRoute.typeSeq,
                updateTime = t
        )
    }

    /**
     * Get list of stops and path by Child Route and update into DB
     *
     * @param route Child Route
     * @param needEtaUpdate update eta of stops as well if true
     */
    override fun getStops(route: Route, needEtaUpdate: Boolean) {
        val prefix = "[${route.routeKey}]"

        try {
            val response = kmb.getStops(
                    route = route.routeKey.routeNo,
                    bound = route.routeKey.bound.toString(),
                    serviceType = route.routeKey.variant.toString()).execute()

            logd("$prefix isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                GlobalScope.launch(Dispatchers.Default) {
                    val t = Utils.getCurrentTimestamp()
                    val kmbStopsRes = response.body()
                    //logd(kmbStopsRes.toString())

                    // Add Paths to database
                    if (kmbStopsRes?.data?.route?.lineGeometry != null && kmbStopsRes.data.route.lineGeometry.isNotEmpty()) {
                        val paths = mutableListOf<Path>()

                        val strPaths = kmbStopsRes.data.route.lineGeometry.replace("{paths:", "")
                            .replace("}", "")
                        val arrPaths =
                            AppHelper.gson.fromJson(strPaths, Array<Array<DoubleArray>>::class.java)

                        var seq = 0L
                        arrPaths.forEachIndexed { section, arrPaths2 ->
                            arrPaths2.forEach { point ->
                                if (point.size == 2) {
                                    val latLng = Utils.hk1980GridToLatLng(point[1], point[0])
                                    paths.add(toPath(route, latLng, seq++, section.toLong(), t))
                                }
                            }
                        }

                        logd("$prefix paths response ${paths.size}")
                        AppHelper.db.pathDao().insertOrUpdate(route, paths, t)
                    }

                    // Add Stops to database
                    if (kmbStopsRes?.data?.routeStops != null && kmbStopsRes.data.routeStops.isNotEmpty()) {
                        val stops = mutableListOf<Stop>()
                        (kmbStopsRes.data.routeStops).forEach {
                            stops.add(toStop(route, it!!, t))
                        }

                        logd("$prefix stops response ${stops.size}")
                        AppHelper.db.stopDao().insertOrUpdate(route.routeKey, stops, t)

                        if (needEtaUpdate)
                            updateEta(stops)
                    }
                }
            }
        } catch (e: Exception) {
            loge("getStops failed!", e)
        }
    }

    private fun toPath(route: Route, latLng: LatLng, seq: Long, section: Long, t: Long): Path {
        val path = Path(
            routeKey = route.routeKey.copy(),
            seq = seq,
            section = section,
            updateTime = t
        )
        path.latLng = latLng
        return path
    }

    private fun toStop(route: Route, routeStop: KmbStopsRes.RouteStop, t: Long): Stop {
        val stop = Stop(
                routeKey = route.routeKey.copy(),
                seq = routeStop.seq?: 0,
                name = StringLang(routeStop.cName?: "", routeStop.eName?: "", routeStop.sCName?: ""),
                to = route.to,
                details = StringLang(routeStop.cLocation?: "", routeStop.eLocation?: "", routeStop.sCLocation?: ""),
                fare = routeStop.airFare?: 0.0,
                info = Info(stopId = routeStop.bsiCode ?: ""),
                updateTime = t
        )
        if (routeStop.x != null && routeStop.y != null)
            stop.latLng = Utils.hk1980GridToLatLng(routeStop.y, routeStop.x)
        return stop
    }

    /**
     * Get url of timetable of route
     *
     * @param route Child Route
     */
    override fun getTimetableUrl(route: Route): String? {
        return null
    }

    /**
     * Get timetable of route
     *
     * @param route Child Route
     * @return timetable of child route
     */
    override fun getTimetable(route: Route): String? {
        val prefix = "[${route.routeKey}]"
        var result = ""

        try {
            val response = kmb.getTimetable(
                    route = route.routeKey.routeNo,
                    bound = route.routeKey.bound.toString()).execute()

            logd("$prefix isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                var lastDayType = ""
                val kmbTimetableRes = response.body()

                kmbTimetableRes?.data?.toList()?.sortedBy { it.first }?.forEach { pair ->
                    var variantHeader = true
                    pair.second?.forEach {
                        it?.let { rec ->
                            result += toTimetableMarkdown(
                                route = route,
                                rec = rec,
                                variantHeader = variantHeader,
                                tableHeader = rec.dayType != lastDayType,
                                thematicBreak = result.isNotEmpty())
                            variantHeader = false
                            lastDayType = rec.dayType.orEmpty()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            loge("getTimetable failed!", e)
        }

        //logd(result)
        return result
    }

    private fun toTimetableMarkdown(route: Route, rec: KmbTimetableRes.Timetable, variantHeader: Boolean, tableHeader: Boolean, thematicBreak: Boolean): String {
        var result = ""

        val boundText = (route.routeKey.bound == 1L).yn(rec.boundText1, rec.boundText2)
        val boundTime = (route.routeKey.bound == 1L).yn(rec.boundTime1, rec.boundTime2)

        if (!boundText.isNullOrBlank()) {
            // From To Header
            if (variantHeader) {
                if (thematicBreak) {
                    result += "\n***\n"
                }
                val variant = rec.serviceTypeChi.isNullOrBlank().yn(AppHelper.getString(R.string.normal_route), rec.serviceTypeChi)
                result += "## $variant\n"
            } else if (tableHeader) {
                result += "***"
            }

            // Table Header
            if (tableHeader || variantHeader) {
                result += "\n\n${toHeader(rec.dayType.orEmpty())}|"
                if (!boundTime.isNullOrBlank()) {
                    result += AppHelper.getString(R.string.headway_mins)
                }
                result += "\n---|"
                if (!boundTime.isNullOrBlank()) {
                    result += "---"
                }
                result += "\n"
            }

            // Table Content
            result += "$boundText|"
            if (!boundTime.isNullOrBlank()) {
                result += boundTime
            }
            result += "\n"
        }

        return result
    }

    private fun toHeader(dayType: String): String {
        val resId = when (dayType.trim()) {
            "MF" -> R.string.mon_to_fri
            "MS" -> R.string.mon_to_sat
            "S" -> R.string.sat
            "H" -> R.string.sun_holidays
            "D" -> R.string.daily
            else -> null
        }

        return if (resId != null) AppHelper.getString(resId) else ""
    }

    /**
     * Get Eta of list of stops and update into DB
     *
     * @param stops list of stops
     */
    @AddTrace(name = "KmbConnection_updateEta")
    override fun updateEta(stops: List<Stop>) {
        val t = Utils.getCurrentTimestamp()

        try {
            runBlocking {
                val jobs = arrayListOf<Job>()

                stops.forEach { stop ->
                    jobs += GlobalScope.launch(Dispatchers.Default) {
                        stop.etaStatus = Constants.EtaStatus.FAILED
                        stop.etaUpdateTime = t
                        try {
                            val kmbEtaReq = getEtaReq(
                                routeNo = stop.routeKey.routeNo,
                                bound = stop.routeKey.bound.toString(),
                                variant = stop.routeKey.variant.toString(),
                                stopId = stop.info.stopId,
                                seq = stop.seq.toString()
                            )

                            val response =
                                kmbEta.getEta(
                                    token = kmbEtaReq.token,
                                    t = kmbEtaReq.t,
                                    lang = "1"
                                ).execute()

                            if (response.isSuccessful) {
                                val kmbEtaRes = response.body()
                                //logd(kmbEtaRes.toString())

                                val etaResults = mutableListOf<EtaResult>()

                                if (kmbEtaRes?.data?.response != null && kmbEtaRes.data.response.isNotEmpty()) {
                                    (kmbEtaRes.data.response).forEach {
                                        if (it.t?.isNotBlank() == true) {
                                            etaResults.add(toEtaResult(stop, it))
                                        } else if (etaResults.isEmpty()) {
                                            etaResults.add(toEtaResult(stop, AppHelper.getString(R.string.eta_msg_no_eta_info)))
                                        }
                                    }
                                    //logd(AppHelper.gson.toJson(etaResults))
                                } else {
                                    etaResults.add(toEtaResult(stop, AppHelper.getString(R.string.eta_msg_no_eta_info)))
                                }

                                if (etaResults.isNotEmpty()) {
                                    stop.etaStatus = Constants.EtaStatus.SUCCESS
                                    stop.etaResults = etaResults
                                    //logd(AppHelper.gson.toJson(stop.etaResults))
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

    override fun updateEta(stop: Stop) {
        return
    }

    private fun toEtaResult(stop: Stop, response: KmbEtaRes.Response): EtaResult {
        val ignoreGps = response.t?.contains("[新|城]巴".toRegex()) ?: false
        var distance = response.dis ?: -1L
        if (distance <= 0L) {
            distance += 1L
        }

        return EtaResult(
                company = stop.routeKey.company,
                etaTime = Utils.timeStrToTimestamp(response.t ?: ""),
                msg = Utils.timeStrToMsg(response.t ?: ""),
                scheduleOnly = Utils.isScheduledOnly(response.t ?: ""),
                gps = ignoreGps || (response.ei != null && distance > 0L),
                variant = response.busServiceType,
                wifi = (response.wifi != null && response.wifi == "Y"),     // changed to store wheelchair
                capacity = Utils.phaseCapacity(response.ol ?: ""),
                distance = distance
        )
    }
}