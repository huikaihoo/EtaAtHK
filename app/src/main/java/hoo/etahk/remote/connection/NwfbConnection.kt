package hoo.etahk.remote.connection

import android.util.Base64
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.perf.metrics.AddTrace
import com.mcxiaoke.koi.HASH
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Constants.Eta
import hoo.etahk.common.Utils
import hoo.etahk.common.Utils.timeStrToMsg
import hoo.etahk.common.constants.SharePrefs
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.common.tools.ParentRoutesMap
import hoo.etahk.common.tools.Separator
import hoo.etahk.model.data.Path
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.EtaResult
import hoo.etahk.model.json.Info
import hoo.etahk.model.json.StringLang
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object NwfbConnection: BaseConnection {

    /***************
     * Shared
     ***************/
    fun getSystemCode(): String {
        var random = (0 until 1000).random().toString()
        random += "0".repeat(4 - random.length)

        var timestamp = Utils.getCurrentTimestamp().toString()
        timestamp = timestamp.substring(timestamp.length - 6)

        return (timestamp + random + HASH.md5(timestamp + random + "firstbusmwymwy")).toUpperCase()
    }

    fun getSystemCode2(): String {
        // Get Random String
        var random = (0 until 10000).random().toString()
        random += "0".repeat(5 - random.length)

        val timestamp = Utils.getCurrentTimestamp().toString()
        val timestampStr = (timestamp.substring(2, 3) + timestamp.substring(9, 10)
                + timestamp.substring(4, 5) + timestamp.substring(6, 7)
                + timestamp.substring(3, 4) + timestamp.substring(0, 1)
                + timestamp.substring(8, 9) + timestamp.substring(7, 8)
                + timestamp.substring(5, 6) + timestamp.substring(1, 2))

        random = timestampStr + HASH.sha256((timestampStr + "siwmytnw" + random).toByteArray()).toLowerCase() + random

        // Encrypt Random String
        val keySpec = SecretKeySpec("siwmytnwinfomwyy".toByteArray(), "AES")
        val ivParameterSpec = IvParameterSpec("a20330efd3f6060e".toByteArray())

        val instance = Cipher.getInstance("AES/CBC/PKCS5Padding")
        instance.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec)

        val encryptedStr =  String(HASH.encodeHex(instance.doFinal(random.toByteArray()), false))

        // Process Encrypted String
        val bytes = ByteArray(encryptedStr.length / 2)
        var i = 0
        while (i < encryptedStr.length) {
            bytes[i / 2] = ((Character.digit(encryptedStr[i], 16) shl 4) + Character.digit(encryptedStr[i + 1], 16)).toByte()
            i += 2
        }

        return Base64.encodeToString(bytes, Base64.NO_WRAP).replace("=".toRegex(), "")
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
        val temp = HashMap<String, Route>()
        val result = ParentRoutesMap()

        try {
            val response = ConnectionHelper.nwfb.getParentRoutes(
                    m = SharePrefs.NWFB_API_PARAMETER_TYPE_ALL_BUS,
                    syscode = getSystemCode()).execute()

            if (response.isSuccessful) {
                val separator = Separator("\\|\\*\\|<br>".toRegex(), "\\|\\|".toRegex(), Constants.Route.NWFB_ROUTE_RECORD_SIZE)

                //logd("onResponse columnSize ${separator.columnSize}")
                separator.original = response.body()?.string() ?: ""
                separator.result.forEach {
                    val route = toRoute(it, t)
                    val key = route.routeKey.routeNo

                    if (temp.contains(key)) {
                        temp[key] = mergeRoute(it, temp[key]!!)
                    } else {
                        temp[key] = route
                    }
                }

                result.addAll(temp.values)
                logd("onResponse separator.result ${separator.result.size}")
                logd("onResponse ${result.size}")
            }
        } catch (e: Exception) {
            loge("getParentRoutes failed!", e)
        }

        return result
    }

    private fun toRoute(records: List<String>, t: Long): Route {
        val company = records[Constants.Route.NWFB_ROUTE_RECORD_COMPANY]

        return Route(
                routeKey = RouteKey(company = company,
                        routeNo = records[Constants.Route.NWFB_ROUTE_RECORD_ROUTE_NO],
                        bound = 0L,
                        variant = 0L),
                direction = records[Constants.Route.NWFB_ROUTE_RECORD_DIRECTION].toLong(),
                specialCode = 0L,
                companyDetails = listOf(company),
                from = StringLang.newInstance(records[Constants.Route.NWFB_ROUTE_RECORD_FROM]),
                to = StringLang.newInstance(records[Constants.Route.NWFB_ROUTE_RECORD_TO]),
                details = StringLang.newInstance(records[Constants.Route.NWFB_ROUTE_RECORD_DETAILS]),
                info = Info(boundIds = listOf(records[Constants.Route.NWFB_ROUTE_RECORD_INFO_BOUND_ID]),
                        bound = records[Constants.Route.NWFB_ROUTE_RECORD_INFO_BOUND]),
                updateTime = t
        )
    }

    private fun mergeRoute(records: List<String>, route: Route): Route {
        val boundIds = route.info.boundIds.toMutableList()
        boundIds.add(records[Constants.Route.NWFB_ROUTE_RECORD_INFO_BOUND_ID])
        route.info.boundIds = boundIds.toList()

        if (route.direction == 1L && route.info.boundIds.size > 1) {
            route.direction = route.info.boundIds.size.toLong()
        }

        return route
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
        parentRoute.info.boundIds.forEachIndexed { index, boundId ->
            try {
                val prefix = "[${parentRoute.routeKey.routeNo}][$boundId]"

                val response = ConnectionHelper.nwfb.getBoundVariant(
                        id = boundId,
                        l = "0",
                        syscode = getSystemCode()).execute()

                logd("$prefix isSuccessful = ${response.isSuccessful}")

                if (response.isSuccessful) {
                    GlobalScope.launch(Dispatchers.Default) {
                        val t = Utils.getCurrentTimestamp()
                        val responseStr = response.body()?.string()
                        //logd(responseStr)

                        if (!responseStr.isNullOrBlank()) {
                            val routes = mutableListOf<Route>()
                            val nwfbResponse = responseStr.split("<br>")

                            nwfbResponse.forEach {
                                val records = it.split("(\\|\\|)|(\\*\\*\\*)".toRegex())
                                if (records.size >= Constants.Route.NWFB_VARIANT_RECORD_SIZE) {
                                    routes.add(toChildRoute(parentRoute, (index + 1).toLong(), records, t))
                                }
                                //logd(it)
                            }

                            logd("$prefix childroutes response ${routes.size}")
                            AppHelper.db.childRouteDao().insertOrUpdate(routes, t)
                        }
                    }
                }
            } catch (e: Exception) {
                loge("getChildRoutes failed!", e)
            }
        }
    }

    private fun toChildRoute(parentRoute: Route, bound: Long, records: List<String>, t: Long): Route {
        val infoBound = records[Constants.Route.NWFB_VARIANT_RECORD_INFO_BOUND].trim()
        //val bound = if (parentRoute.boundCount <= 1L || infoBound == "O") 1L else 2L
        val from = if (bound == 1L) parentRoute.from else parentRoute.to
        val to = if (bound == 1L) parentRoute.to else parentRoute.from

        return Route(
                routeKey = parentRoute.routeKey.copy(bound = bound, variant = records[Constants.Route.NWFB_VARIANT_RECORD_VARIANT].toLong()),
                direction = parentRoute.childDirection,
                specialCode = parentRoute.specialCode,
                companyDetails = parentRoute.companyDetails,
                from = from,
                to = to,
                details = StringLang.newInstance(records[Constants.Route.NWFB_VARIANT_RECORD_DETAILS].trim()),
                info = Info(rdv = records[Constants.Route.NWFB_VARIANT_RECORD_RDV].trim(),
                        bound = infoBound,
                        startSeq = records[Constants.Route.NWFB_VARIANT_RECORD_START_SEQ].toLong(),
                        endSeq =  records[Constants.Route.NWFB_VARIANT_RECORD_END_SEQ].toLong()),
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
            val response = ConnectionHelper.nwfb.getPaths(
                    rdv = route.info.rdv,
                    bound = route.info.bound,
                    l = "0",
                    syscode = getSystemCode()).execute()

            logd("$prefix isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                GlobalScope.launch(Dispatchers.Default) {
                    val t = Utils.getCurrentTimestamp()
                    val responseStr = response.body()?.string()
                    //logd(responseStr)

                    if (!responseStr.isNullOrBlank()) {
                        val paths = mutableListOf<Path>()
                        val nwfbResponse = responseStr.split("\\|\\|".toRegex())

                        var seq = 0L
                        nwfbResponse.forEach {
                            val records = it.split(",")
                            if (records.size == 2) {
                                val latLng = LatLng(records[0].toDouble()+0.000194, records[1].toDouble()-0.000070)
                                paths.add(toPath(route, latLng, seq++, t))
                            }
                        }

                        logd("$prefix paths response ${paths.size}")
                        AppHelper.db.pathDao().insertOrUpdate(route, paths, t)
                    }
                }
            }
        } catch (e: Exception) {
            loge("getPaths failed!", e)
        }

        val info = "1|*|${route.routeKey.company}||${route.info.rdv}||${route.info.startSeq}||${route.info.endSeq}"
        //logd("info=[$info]")

        try {
            val response = ConnectionHelper.nwfb.getStops(
                    info = info,
                    l = "0",
                    syscode = getSystemCode()).execute()

            logd("$prefix isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                GlobalScope.launch(Dispatchers.Default) {
                    val t = Utils.getCurrentTimestamp()
                    val responseStr = response.body()?.string()
                    //logd(responseStr)

                    if (!responseStr.isNullOrBlank()) {
                        val stops = mutableListOf<Stop>()
                        val nwfbResponse = responseStr.split("<br>")

                        nwfbResponse.forEach {
                            val records = it.split("\\|\\|".toRegex())
                            if (records.size >= Constants.Stop.NWFB_STOP_RECORD_SIZE) {
                                stops.add(toStop(route, records, t))
                            }
                            //logd(it)
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

    private fun toPath(route: Route, latLng: LatLng, seq: Long, t: Long): Path {
        val path = Path(
            routeKey = route.routeKey.copy(),
            seq = seq,
            updateTime = t
        )
        path.location = latLng
        return path
    }

    private fun toStop(route: Route, records: List<String>, t: Long): Stop {
        assert(records.size >= Constants.Stop.NWFB_STOP_RECORD_SIZE)

        return Stop(
                routeKey = route.routeKey,
                seq = records[Constants.Stop.NWFB_STOP_RECORD_SEQ].toLong(),
                name = StringLang.newInstance(records[Constants.Stop.NWFB_STOP_RECORD_DETAILS].substringBefore(",").trim()),
                to = StringLang.newInstance(records[Constants.Stop.NWFB_STOP_RECORD_TO].trim()),
                details = StringLang.newInstance(records[Constants.Stop.NWFB_STOP_RECORD_DETAILS].trim()),
                latitude = records[Constants.Stop.NWFB_STOP_RECORD_LATITUDE].toDouble(),
                longitude =records[Constants.Stop.NWFB_STOP_RECORD_LONGITUDE].toDouble(),
                fare = records[Constants.Stop.NWFB_STOP_RECORD_FARE].toDouble(),
                info = Info(rdv = records[Constants.Stop.NWFB_STOP_RECORD_RDV].trim(),
                        bound = route.info.bound,
                        stopId = records[Constants.Stop.NWFB_STOP_RECORD_STOP_ID].toInt().toString()),
                updateTime = t
        )
    }

    /**
     * Get url of timetable of route
     *
     * @param route Child Route
     */
    override fun getTimetableUrl(route: Route): String? {
        val info = "1|*|${route.routeKey.company}||${route.info.rdv}||${route.info.startSeq}||${route.info.endSeq}"

        return ConnectionHelper.nwfb.getTimetable(
                rdv = info,
                bound = route.info.bound,
                l = "0",
                syscode = getSystemCode(),
                syscode2 = getSystemCode2()).request().url().toString()
    }

    override fun getTimetable(route: Route): String? {
        return null
    }

    /**
     * Get Eta of list of stops and update into DB
     *
     * @param stops list of stops
     */
    @AddTrace(name = "NwfbConnection_updateEta")
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
                            val response =
                                    ConnectionHelper.nwfbEta.getEta(
                                            stopid = stop.info.stopId,
                                            service_no = stop.routeKey.routeNo,
                                            removeRepeatedSuspend = "Y",
                                            interval = "60",
                                            l = "0",
                                            bound = stop.info.bound,
                                            stopseq = stop.seq.toString(),
                                            rdv = stop.info.rdv,
                                            showtime = "Y",
                                            syscode = getSystemCode(),
                                            syscode2 = getSystemCode2()).execute()

                            if (response.isSuccessful) {
                                var responseStr = response.body()?.string()
                                //logd(responseStr)

                                val etaResults = mutableListOf<EtaResult>()
                                val msg = getInvalidMsg(responseStr ?: "")

                                if (responseStr.isNullOrBlank() || msg.isNotEmpty()) {
                                    etaResults.add(toEtaResult(stop, msg))

                                } else {
                                    responseStr = responseStr.replace(".*\\|##\\|".toRegex(), "")
                                    val nwfbResponse = responseStr.split("<br>")

                                    nwfbResponse.forEach {
                                        val records = it.split("(\\|\\|)|(\\|\\^\\|)".toRegex())
                                        if(records.size >= Eta.NWFB_ETA_RECORD_SIZE) {
                                            etaResults.add(toEtaResult(records))
                                        }
                                        //logd(it)
                                    }
                                }

                                if (!etaResults.isEmpty()) {
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
        ConnectionHelper.nwfbEta.getEta(
                stopid = stop.info.stopId,
                service_no = stop.routeKey.routeNo,
                removeRepeatedSuspend = "Y",
                interval = "60",
                l = "0",
                bound = stop.info.bound,
                stopseq = stop.seq.toString(),
                rdv = stop.info.rdv,
                showtime = "Y",
                syscode = getSystemCode())
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        val t = Utils.getCurrentTimestamp()

                        stop.etaStatus = Constants.EtaStatus.FAILED
                        stop.etaUpdateTime = t

                        AppHelper.db.stopDao().update(stop)
                    }
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>){
                        val t = Utils.getCurrentTimestamp()
                        var responseStr = response.body()?.string()
                        //logd(responseStr)

                        val etaResults = mutableListOf<EtaResult>()
                        val msg = getInvalidMsg(responseStr ?: "")

                        if (responseStr.isNullOrBlank() || !msg.isEmpty()) {
                            etaResults.add(toEtaResult(stop, msg))

                        } else {
                            responseStr = responseStr.replace(".*\\|##\\|".toRegex(), "")
                            val nwfbResponse = responseStr.split("<br>")

                            nwfbResponse.forEach {
                                val records = it.split("(\\|\\|)|(\\|\\^\\|)".toRegex())
                                if(records.size >= Eta.NWFB_ETA_RECORD_SIZE) {
                                    etaResults.add(toEtaResult(records))
                                }
                                //logd(it)
                            }
                        }

                        if (!etaResults.isEmpty()) {
                            stop.etaStatus = Constants.EtaStatus.SUCCESS
                            stop.etaResults = etaResults
                            stop.etaUpdateTime = t
                            //logd(AppHelper.gson.toJson(stop.etaResults))
                        } else {
                            stop.etaStatus = Constants.EtaStatus.FAILED
                            stop.etaUpdateTime = t
                        }

                        AppHelper.db.stopDao().update(stop)
                    }
                })
    }

    // ETA Related
    private fun getInvalidMsg(responseStr: String): String {
        return when (responseStr.contains("\\|DISABLED\\|".toRegex())) {
            true -> AppHelper.getString(R.string.eta_msg_no_eta_service)
            false -> {
                when (responseStr.contains("\\|HTML\\|".toRegex())) {
                    true -> when (responseStr.contains("服務時間已過")) {
                        true -> AppHelper.getString(R.string.eta_msg_not_in_service_hours)
                        // TODO("Extract ETA message")
                        false -> AppHelper.getString(R.string.eta_msg_no_eta_service)
                    }
                    false -> ""
                }
            }
        }
    }

    // Normal ETA
    private fun toEtaResult(records: List<String>): EtaResult {
        assert(records.size >= Eta.NWFB_ETA_RECORD_SIZE)

        var msg = (records[Eta.NWFB_ETA_RECORD_ETA_TIME] + " " + timeStrToMsg(records[Eta.NWFB_ETA_RECORD_MSG])).trim()

        var distance = records[Eta.NWFB_ETA_RECORD_DISTANCE].toLong()
        if (distance <= 0L && msg.contains("已到達")) {
            distance = 1
        }

        val gps = !msg.contains("非實時")

        msg = msg.replace("已到達".toRegex(), "")
                .replace("非實時".toRegex(), "")
                .trim()

        return EtaResult(
                company = records[Eta.NWFB_ETA_RECORD_COMPANY].trim(),
                etaTime = Utils.timeStrToTimestamp(records[Eta.NWFB_ETA_RECORD_ETA_TIME]),
                msg = msg,
                scheduleOnly = (distance <= 0),
                gps = (gps && distance > 0),
                distance = distance)
    }
}
