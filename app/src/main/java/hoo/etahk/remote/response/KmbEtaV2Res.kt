package hoo.etahk.remote.response


import com.google.gson.annotations.SerializedName

data class KmbEtaV2Res(
    @SerializedName("routeNo") val routeNo: String? = "",
    @SerializedName("bound") val bound: Long? = 0,
    @SerializedName("service_type") val serviceType: Long? = 0,
    @SerializedName("seq") val seq: Long? = 0,
    @SerializedName("generated") val generated: Long? = 0,
    @SerializedName("responsecode") val responsecode: Long? = 0,
    @SerializedName("updated") val updated: Long? = 0,
    @SerializedName("eta") val eta: List<KmbEtaRes.Response>? = listOf()
)