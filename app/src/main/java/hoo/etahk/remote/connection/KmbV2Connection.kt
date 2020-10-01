package hoo.etahk.remote.connection

import com.google.firebase.perf.metrics.AddTrace
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.EtaResult
import hoo.etahk.remote.api.GistApi
import hoo.etahk.remote.api.KmbApi
import hoo.etahk.remote.response.KmbEtaRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

open class KmbV2Connection(
    private val kmb: KmbApi,
    private val kmbEta: KmbApi,
    private val gist: GistApi): KmbConnection(kmb, kmbEta, gist) {

    @AddTrace(name = "KmbV2Connection_updateEta")
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
                            val kmbEtaReq = getEtaV2Req(
                                routeNo = stop.routeKey.routeNo,
                                bound = stop.routeKey.bound.toString(),
                                variant = stop.routeKey.variant.toString(),
                                seq = stop.seq.toString()
                            )

                            var response = kmbEta.getEtaV2(kmbEtaReq).execute()

                            if (response.isSuccessful) {
                                val kmbEtaRes = response.body()
                                //logd(kmbEtaRes.toString())

                                val etaResults = mutableListOf<EtaResult>()

                                if (!kmbEtaRes.isNullOrEmpty() && kmbEtaRes[0].eta != null && kmbEtaRes[0].eta!!.isNotEmpty()) {
                                    (kmbEtaRes[0].eta!!).forEach {
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