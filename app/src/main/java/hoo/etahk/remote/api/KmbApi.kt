package hoo.etahk.remote.api

import hoo.etahk.remote.response.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// baseUrl = "http://search.kmb.hk/KMBWebSite/"
interface KmbApi {
    @GET("Function/FunctionRequest.ashx?action=getRouteBound")
    fun getRouteBound(@Query("route") route: String): Call<KmbRouteBoundRes>

    @GET("Function/FunctionRequest.ashx?action=getSpecialRoute")
    fun getBoundVariant(@Query("route") route: String, @Query("bound") bound: String): Call<KmbBoundVariantRes>

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

    @GET("http://etav3.kmb.hk/?action=geteta")
    fun getEta(@Query("route") route: String = "",
               @Query("bound") bound: String = "",
               @Query("stop") stop: String = "",
               @Query("stop_seq") stop_seq: String = "",
               @Query("serviceType") serviceType: String = "",
               @Query("lang") lang: String = "tc",
               @Query("updated") updated: String = ""): Call<KmbEtaRes>

    @GET("http://etadatafeed.kmb.hk:1933/GetData.ashx?type=ETA_R")
    fun getEtaRoutes(): Call<List<KmbEtaRoutesRes>>
}
