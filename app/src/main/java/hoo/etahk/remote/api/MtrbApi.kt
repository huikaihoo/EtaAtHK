package hoo.etahk.remote.api

import hoo.etahk.remote.request.MtrbEtaReq
import hoo.etahk.remote.request.MtrbEtaRoutesReq
import hoo.etahk.remote.response.MtrbEtaRes
import hoo.etahk.remote.response.MtrbEtaRoutesRes
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// baseUrl = "https://mavmwfs1004.azurewebsites.net/MTRBus/BusService.svc/"
interface MtrbApi {
    @POST("getRouteStatusDetail")
    fun getEtaRoutes(@Body request : MtrbEtaRoutesReq): Call<MtrbEtaRoutesRes>

    @POST("getBusStopsDetail")
    fun getEta(@Body request : MtrbEtaReq): Call<MtrbEtaRes>

    @POST("getBusStopsDetail")
    fun getEtaRaw(@Body request : MtrbEtaReq): Call<ResponseBody>
}