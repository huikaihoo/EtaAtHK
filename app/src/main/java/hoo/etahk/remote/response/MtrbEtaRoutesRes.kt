package hoo.etahk.remote.response

import com.google.gson.annotations.SerializedName

data class MtrbEtaRoutesRes(
    @SerializedName("routeStatus") val routeStatus: List<RouteStatus>? = listOf()
) {
    data class RouteStatus(
        @SerializedName("routeNumber") val routeNumber: String? = "",
        @SerializedName("status") val status: String? = ""
    )
}
