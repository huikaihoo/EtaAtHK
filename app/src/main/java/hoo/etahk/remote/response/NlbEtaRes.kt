package hoo.etahk.remote.response

import com.google.gson.annotations.SerializedName

data class NlbEtaRes(
    @SerializedName("estimatedArrivalTime") val estimatedArrivalTime: EstimatedArrivalTime? = EstimatedArrivalTime()
) {
    data class EstimatedArrivalTime(
        @SerializedName("html") val html: String? = ""
    )
}