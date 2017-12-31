package hoo.etahk.remote.api

import hoo.etahk.remote.response.KmbEtaRes
import hoo.etahk.remote.response.KmbEtaRoutesRes
import hoo.etahk.remote.response.KmbStopsRes
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface KmbApi {

    // Base Url = "http://search.kmb.hk/KMBWebSite/"
//    @GET("Function/FunctionRequest.ashx?action=getRouteBound")
//    fun getRouteBound(@Query("route") route: String): Call<KmbRouteBoundRes>
//
//    @GET("Function/FunctionRequest.ashx?action=getSpecialRoute")
//    fun getSpecialRoute(@Query("route") route: String, @Query("bound") bound: String): Call<KmbSpecialRouteRes>
//
    @GET("Function/FunctionRequest.ashx?action=getStops")
    fun getStops(@Query("route") route: String = "",
                 @Query("bound") bound: String = "",
                 @Query("serviceType") serviceType: String = ""): Call<KmbStopsRes>
//
//    @GET("Function/FunctionRequest.ashx?action=getAnnounce")
//    fun getAnnounce(@Query("route") route: String, @Query("bound") bound: String): Call<KmbAnnounceRes>
//
//    @GET("AnnouncementPicture.ashx")
//    fun getAnnouncementPicture(@Query("url") url: String): Call<ResponseBody>

    // Base Url = "http://etav3.kmb.hk/"
    @GET
    fun getEta(@Url url: String): Call<KmbEtaRes>

    @GET("?action=geteta")
    fun getEta(@Query("route") route: String = "",
               @Query("bound") bound: String = "",
               @Query("stop") stop: String = "",
               @Query("stop_seq") stop_seq: String = "",
               @Query("serviceType") serviceType: String = "",
               @Query("lang") lang: String = "tc",
               @Query("updated") updated: String = ""): Call<KmbEtaRes>

    // Base Url = "http://etadatafeed.kmb.hk:1933/"
    @GET("GetData.ashx?type=ETA_R")
    fun getEtaRoutes(): Call<List<KmbEtaRoutesRes>>
}
