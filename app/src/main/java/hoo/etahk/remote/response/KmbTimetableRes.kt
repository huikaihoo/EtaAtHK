package hoo.etahk.remote.response

import com.google.gson.annotations.SerializedName

data class KmbTimetableRes(
    @SerializedName("data") val data: Map<Long, List<Timetable?>?>? = mapOf(),
    @SerializedName("result") val result: Boolean? = false
) {
    data class Timetable(
        @SerializedName("BoundText1") val boundText1: String? = "",
        @SerializedName("BoundText2") val boundText2: String? = "",
        @SerializedName("BoundTime1") val boundTime1: String? = "",
        @SerializedName("BoundTime2") val boundTime2: String? = "",
        @SerializedName("DayType") val dayType: String? = "",
        @SerializedName("Destination_Chi") val destinationChi: String? = "",
        @SerializedName("Destination_Eng") val destinationEng: String? = "",
        @SerializedName("OrderSeq") val orderSeq: Long? = 0L,
        @SerializedName("Origin_Chi") val originChi: String? = "",
        @SerializedName("Origin_Eng") val originEng: String? = "",
        @SerializedName("Route") val route: String? = "",
        @SerializedName("ServiceType") val serviceType: String? = "",
        @SerializedName("ServiceType_Chi") val serviceTypeChi: String? = "",
        @SerializedName("ServiceType_Eng") val serviceTypeEng: String? = ""
    )
}