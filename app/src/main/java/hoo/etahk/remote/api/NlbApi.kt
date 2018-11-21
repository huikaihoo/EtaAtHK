package hoo.etahk.remote.api

import hoo.etahk.remote.request.NlbEtaReq
import hoo.etahk.remote.response.NlbDatabaseRes
import hoo.etahk.remote.response.NlbEtaRes
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface NlbApi {
    // Base Url = "https://nlb.kcbh.com.hk:8443/"
    @GET("api/passenger/app.php?action=getDatabase")
    fun getDatabase(): Call<NlbDatabaseRes>

    @POST("api/passenger/stop.php?action=estimatedArrivalTime")
    fun getEta(@Body request : NlbEtaReq): Call<NlbEtaRes>
}