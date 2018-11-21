package hoo.etahk.remote.request
import com.google.gson.annotations.SerializedName

data class NlbEtaReq(
    @SerializedName("routeId") val routeId: String? = "",
    @SerializedName("stopId") val stopId: String? = "",
    @SerializedName("language") val language: String? = "zh"
)