package hoo.etahk.remote.api

import hoo.etahk.common.Constants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GovApi {
    // Base Url = "http://cms.hkemobility.gov.hk/"
    @GET("et/getrouteinfo4.php")
    fun getParentRoutes(@Query("route_name") route_name: String = "",
                        @Query("company_index") company_index: String = Constants.SharePrefs.GOV_API_PARAMETER_COMPANY_ALL_BUS,
                        @Query("lang") lang: String = "TC",
                        @Query("region") region: String = "",
                        @Query("syscode") syscode: String = "",
                        @Query("p") p: String = Constants.SharePrefs.GOV_API_PARAMETER_PLATFORM,
                        @Query("version") appversion: String = Constants.SharePrefs.GOV_API_PARAMETER_APP_VERSION): Call<ResponseBody>
}