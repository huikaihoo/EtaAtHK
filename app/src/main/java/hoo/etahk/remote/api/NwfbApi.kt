package hoo.etahk.remote.api

import hoo.etahk.common.Constants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface NwfbApi {

    // Base Url = "http://mobile.nwstbus.com.hk/"
    @GET("api6/getmmroutelist.php")
    fun getParentRoutes(@Query("rno") rno: String = "",
                        @Query("m") m: String,
                        @Query("l") l: String = "0",
                        @Query("syscode") syscode: String = "",
                        @Query("p") p: String = Constants.SharePrefs.NWFB_API_PARAMETER_PLATFORM,
                        @Query("appversion") appversion: String = Constants.SharePrefs.NWFB_API_PARAMETER_APP_VERSION): Call<ResponseBody>

    @GET("api6/getvariantlist.php")
    fun getBoundVariant(@Query("id") id: String = "",
                        @Query("l") l: String = "0",
                        @Query("syscode") syscode: String = "",
                        @Query("p") p: String = Constants.SharePrefs.NWFB_API_PARAMETER_PLATFORM,
                        @Query("appversion") appversion: String = Constants.SharePrefs.NWFB_API_PARAMETER_APP_VERSION): Call<ResponseBody>

    @GET("api6/ppstoplist.php")
    fun getStops(@Query("info") info: String = "",
                 @Query("l") l: String = "0",
                 @Query("syscode") syscode: String = "",
                 @Query("p") p: String = Constants.SharePrefs.NWFB_API_PARAMETER_PLATFORM,
                 @Query("appversion") appversion: String = Constants.SharePrefs.NWFB_API_PARAMETER_APP_VERSION): Call<ResponseBody>

    @GET("api6/getline_multi2.php")
    fun getPaths(@Query("r") rdv: String = "",
                 @Query("bound") bound: String = "",
                 @Query("l") l: String = "0",
                 @Query("syscode") syscode: String = "",
                 @Query("p") p: String = Constants.SharePrefs.NWFB_API_PARAMETER_PLATFORM,
                 @Query("appversion") appversion: String = Constants.SharePrefs.NWFB_API_PARAMETER_APP_VERSION): Call<ResponseBody>

    @GET("api6/getnextbus2.php")
    fun getEta(@Query("stopid") stopid: String = "",
               @Query("service_no") service_no: String = "",
               @Query("removeRepeatedSuspend") removeRepeatedSuspend: String = "Y",
               @Query("interval") interval: String = "",
               @Query("l") l: String = "0",
               @Query("bound") bound: String = "",
               @Query("stopseq") stopseq: String = "",
               @Query("rdv") rdv: String = "",
               @Query("showtime") showtime: String = "Y",
               @Query("removeRepeatedSuspend") removeRepeatedSuspend2: String = "Y",
               @Query("syscode") syscode: String = "",
               @Query("p") p: String = Constants.SharePrefs.NWFB_API_PARAMETER_PLATFORM,
               @Query("version") appversion: String = Constants.SharePrefs.NWFB_API_PARAMETER_APP_VERSION,
               @Query("version2") appversion2: String = Constants.SharePrefs.NWFB_API_PARAMETER_APP_VERSION_2,
               @Query("syscode2", encoded = true) syscode2: String = "",
               @Query("tk") tk: String = "0"): Call<ResponseBody>
}