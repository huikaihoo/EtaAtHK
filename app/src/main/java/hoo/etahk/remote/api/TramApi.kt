package hoo.etahk.remote.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// baseUrl = "http://hktramways.com/"
interface TramApi {
    @GET("js/googleMap.js")
    fun getDatabase(): Call<ResponseBody>

    @GET("nextTram/geteat.php")
    fun getEta(@Query("stop_code") stop_code: String): Call<ResponseBody>

    @GET("tc/schedules-fares/")
    fun getTimetable(): Call<ResponseBody>
}