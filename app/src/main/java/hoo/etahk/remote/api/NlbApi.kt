package hoo.etahk.remote.api

import hoo.etahk.remote.request.NlbEtaReq
import hoo.etahk.remote.response.NlbDatabaseRes
import hoo.etahk.remote.response.NlbEtaRes
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// baseUrl = "https://nlb.kcbh.com.hk:8443/"
interface NlbApi {
    @GET("api/passenger/app.php?action=getDatabase")
    fun getDatabase(): Call<NlbDatabaseRes>

    @POST("api/passenger/stop.php?action=estimatedArrivalTime")
    fun getEta(@Body request : NlbEtaReq): Call<NlbEtaRes>

    @GET("api/passenger/route.php?action=getDetail")
    fun getTimetable(@Query("routeId") str: String): Call<ResponseBody>
}