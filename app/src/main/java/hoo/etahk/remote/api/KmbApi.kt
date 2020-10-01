package hoo.etahk.remote.api

import hoo.etahk.remote.request.KmbEtaV2Req
import hoo.etahk.remote.response.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
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

    @FormUrlEncoded
    @POST("Function/FunctionRequest.ashx/?action=get_ETA")
    fun getEta(@Field("token") token: String,
               @Field("t") t: String,
               @Query("lang") lang: String = "1"): Call<KmbEtaRes>

    @Headers("Content-Type: application/json")
    @POST("https://etav3.kmb.hk/?action=geteta")
    fun getEtaV2(@Body body: KmbEtaV2Req): Call<List<KmbEtaV2Res>>

    @GET("http://etadatafeed.kmb.hk:1933/GetData.ashx?type=ETA_R")
    fun getEtaRoutes(): Call<List<KmbEtaRoutesRes>>

    @GET("Function/FunctionRequest.ashx?action=getschedule")
    fun getTimetable(@Query("route") route: String,
                     @Query("bound") bound: String): Call<KmbTimetableRes>

}
