package hoo.etahk.remote.connection

import com.google.firebase.perf.metrics.AddTrace
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.EtaResult
import hoo.etahk.remote.api.NlbApi
import hoo.etahk.remote.request.NlbEtaReq
import hoo.etahk.remote.response.NlbEtaV2Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

open class NlbV2Connection(
    private val nlb: NlbApi,
    private val nlbEta: NlbApi): NlbConnection(nlb, nlbEta) {

    @AddTrace(name = "NlbV2Connection_updateEta")
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
                                nlbEta.getEtaV2(
                                    NlbEtaReq(routeId = stop.info.rdv,
                                        stopId = stop.info.stopId)
                                ).execute()

                            logd("${response.body()}")
                            if (response.isSuccessful) {
                                val nlbEtaRes = response.body()
                                val etaResults = mutableListOf<EtaResult>()

                                val msg = nlbEtaRes?.message ?: ""

                                if (nlbEtaRes?.estimatedArrivals != null && nlbEtaRes.estimatedArrivals.isNotEmpty()) {
                                    nlbEtaRes.estimatedArrivals.forEach {
                                        etaResults.add(toEtaResult(stop, it))
                                    }

                                    // logd(AppHelper.gson.toJson(etaResults))

                                    if (etaResults.isEmpty()) {
                                        etaResults.add(
                                            toEtaResult(stop, AppHelper.getString(R.string.eta_msg_no_eta_info))
                                        )
                                    }
                                // TODO("Need to Support English")
                                } else if (msg.contains("沒有班次途經本站".toRegex())) {
                                    etaResults.add(toEtaResult(stop, AppHelper.getString(R.string.eta_msg_not_in_service_hours)))
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

    private fun toEtaResult(stop: Stop, estimatedArrival: NlbEtaV2Res.EstimatedArrival): EtaResult {
        val etaTime = Utils.dateStrToTimestamp(estimatedArrival.estimatedArrivalTime ?: "", "yyyy-MM-dd HH:mm:ss")
        val scheduledOnly = (estimatedArrival.departed ?: 0L == 0L)
        val gps = !scheduledOnly && (estimatedArrival.noGPS ?: 1L == 0L)

        return EtaResult(
            company = stop.routeKey.company,
            etaTime = etaTime,
            msg = Utils.timeStrToMsg(Utils.timestampToTimeStr(etaTime) + " " + (estimatedArrival.routeVariantName ?: "")),
            scheduleOnly = scheduledOnly,
            gps = gps,
            wifi = (estimatedArrival.wheelChair ?: 0L == 1L)    // changed to store wheelchair
        )
    }
}