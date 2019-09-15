package hoo.etahk.remote.response

import com.google.gson.annotations.SerializedName

data class NlbEtaV2Res(
    @SerializedName("estimatedArrivals") val estimatedArrivals: List<EstimatedArrival>? = listOf(),
    @SerializedName("message") val message: String? = ""
) {
    data class EstimatedArrival(
        @SerializedName("estimatedArrivalTime") val estimatedArrivalTime: String? = "",
        @SerializedName("routeVariantName") val routeVariantName: String? = "",
        @SerializedName("departed") val departed: Long? = 0,
        @SerializedName("noGPS") val noGPS: Long? = 0,
        @SerializedName("wheelChair") val wheelChair: Long? = 0,
        @SerializedName("generateTime") val generateTime: String? = ""
    )
}