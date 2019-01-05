package hoo.etahk.remote.response

import com.google.gson.annotations.SerializedName

data class KmbRouteBoundRes(
    @SerializedName("data") val data: List<Data?>? = listOf(),
    @SerializedName("result") val result: Boolean? = false
) {
    data class Data(
        @SerializedName("BOUND") val bound: Long? = 0L,
        @SerializedName("ROUTE") val route: String? = "",
        @SerializedName("SERVICE_TYPE") val serviceType: Long? = 0L
    )
}