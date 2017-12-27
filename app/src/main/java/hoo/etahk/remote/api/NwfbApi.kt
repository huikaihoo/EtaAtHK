package hoo.etahk.remote.api

import hoo.etahk.common.Constants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface NwfbApi {

    // Base Url = "http://mobile.nwstbus.com.hk/"
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
               @Query("syscode") syscode: String = "",
               @Query("p") p: String = "android",
               @Query("appversion") appversion: String = Constants.Eta.NWFB_API_PARAMETER_APP_VERSION): Call<ResponseBody>
}