package hoo.etahk.remote.response

import com.google.gson.annotations.SerializedName

data class KmbEtaRes(
    @SerializedName("generated") val generated: Long? = 0,
    @SerializedName("response") val response: List<Response>? = listOf(),
    @SerializedName("responsecode") val responsecode: Long? = 0,
    @SerializedName("updated") val updated: Long? = 0
) {
    data class Response(
        @SerializedName("bus_service_type") val busServiceType: Long = 1L,
        @SerializedName("ei") val ei: String? = "",     // can server receive bus gps signal (N=Yes; Y=No)
        @SerializedName("eot") val eot: String? = "",   // E: time only; T: with text
        @SerializedName("ex") val ex: String? = "",     // expire time (YYYY-MM-DD hh:mm:ss)
        @SerializedName("ol") val ol: String? = "",     // Bus Capacity
        @SerializedName("t") val t: String? = "",       // time (hh:mm xxxx)
        @SerializedName("w") val w: String? = "",       // wheelchair (Y/N/"")
        @SerializedName("wifi") val wifi: String? = ""  // wifi -> two wheelchair (null/Y)
    )
}