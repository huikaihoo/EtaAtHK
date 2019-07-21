package hoo.etahk.remote.response

import com.google.gson.annotations.SerializedName

data class MtrbEtaRes(
    @SerializedName("appRefreshTimeInSecond") val appRefreshTimeInSecond: String? = "",
    @SerializedName("busStop") val busStop: List<BusStop>? = listOf(),
    @SerializedName("caseNumber") val caseNumber: Long? = 0,
    @SerializedName("caseNumberDetail") val caseNumberDetail: String? = "",
    @SerializedName("footerRemarks") val footerRemarks: String? = "",
    @SerializedName("routeColour") val routeColour: String? = "",
    @SerializedName("routeName") val routeName: String? = "",
    @SerializedName("routeStatus") val routeStatus: String? = "",
    @SerializedName("routeStatusColour") val routeStatusColour: String? = "",
    @SerializedName("routeStatusRemarkContent") val routeStatusRemarkContent: String? = "",
    @SerializedName("routeStatusRemarkFooterRemark") val routeStatusRemarkFooterRemark: String? = "",
    @SerializedName("routeStatusRemarkTitle") val routeStatusRemarkTitle: String? = "",
    @SerializedName("routeStatusTime") val routeStatusTime: String? = "",
    @SerializedName("status") val status: String? = ""
) {
    data class BusStop(
        @SerializedName("bus") val bus: List<Bus>? = listOf(),
        @SerializedName("busStopId") val busStopId: String? = "",
        @SerializedName("isSuspended") val isSuspended: String? = "",
        @SerializedName("busIcon") val busIcon: String? = "",
        @SerializedName("busStopRemark") val busStopRemark: String? = "",
        @SerializedName("busStopStatus") val busStopStatus: String? = "",
        @SerializedName("busStopStatusRemarkContent") val busStopStatusRemarkContent: String? = "",
        @SerializedName("busStopStatusRemarkTitle") val busStopStatusRemarkTitle: String? = "",
        @SerializedName("busStopStatusTime") val busStopStatusTime: String? = ""
    ) {
        data class Bus(
            @SerializedName("arrivalTimeInSecond") val arrivalTimeInSecond: String? = "",
            @SerializedName("arrivalTimeText") val arrivalTimeText: String? = "",
            @SerializedName("busId") val busId: String? = "",
            @SerializedName("busLocation") val busLocation: BusLocation? = BusLocation(),
            @SerializedName("departureTimeInSecond") val departureTimeInSecond: String? = "",
            @SerializedName("departureTimeText") val departureTimeText: String? = "",
            @SerializedName("isDelayed") val isDelayed: String? = "",
            @SerializedName("isScheduled") val isScheduled: String? = "",
            @SerializedName("lineRef") val lineRef: String? = "",
            @SerializedName("busRemark") val busRemark: String? = ""
        ) {
            data class BusLocation(
                @SerializedName("latitude") val latitude: Double? = -1.0,
                @SerializedName("longitude") val longitude: Double? = -1.0
            )
        }
    }
}
