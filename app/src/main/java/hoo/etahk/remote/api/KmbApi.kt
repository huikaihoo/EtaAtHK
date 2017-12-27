package hoo.etahk.remote.api

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface KmbApi {

    // Base Url = "http://search.kmb.hk/KMBWebSite/Function/"
//    @GET("FunctionRequest.ashx?action=getAnnounce")
//    fun getAnnounce(@Query("route") route: String, @Query("bound") bound: String): Call<KmbAnnounceRes>
//
//    @GET("FunctionRequest.ashx?action=getRouteBound")
//    fun getRouteBound(@Query("route") route: String): Call<KmbRouteBoundRes>
//
//    @GET("FunctionRequest.ashx?action=getSpecialRoute")
//    fun getSpecialRoute(@Query("route") route: String, @Query("bound") bound: String): Call<KmbSpecialRouteRes>
//
//    @GET("FunctionRequest.ashx?action=getStops")
//    fun getStops(@Query("route") route: String, @Query("bound") bound: String, @Query("serviceType") serviceType: String): Call<KmbStopsRes>

    // Base Url = "http://search.kmb.hk/KMBWebSite/"
    @GET("AnnouncementPicture.ashx")
    fun getAnnouncementPicture(@Query("url") url: String): Call<ResponseBody>

    // Base Url = "http://etav3.kmb.hk/"
    @GET
    fun getEta(@Url url: String): Call<KmbEtaResponse>

    @GET("?action=geteta")
    fun getEta(@Query("route") route: String = "",
               @Query("bound") bound: String = "",
               @Query("stop") stop: String = "",
               @Query("stop_seq") stop_seq: String = "",
               @Query("serviceType") serviceType: String = "",
               @Query("lang") lang: String = "tc",
               @Query("updated") updated: String = ""): Call<KmbEtaResponse>

    class KmbEtaResponse {
        var updated: Long = 0L
        var generated: Long = 0L
        @SerializedName("responsecode")
        var responseCode: Long = 0L
        var response: List<Response>? = emptyList()

        class Response {
            @SerializedName("bus_service_type")
            var busServiceType: Long = 0L
            var t: String? = null        // time (hh:mm xxxx)
            var ei: String? = null       // can server receive bus gps signal (N=Yes; Y=No)
            var w: String? = null        // wheelchair (Y/N/"")
            var eot: String? = null      // E: time only; T: with text
            var ol: String? = null       // Bus Capacity
            var ex: String? = null       // expire time (YYYY-MM-DD hh:mm:ss)
            var wifi: String? = null     // wifi (null/Y)
        }
    }
}
