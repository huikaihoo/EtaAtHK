package hoo.etahk.remote.request

import com.google.gson.annotations.SerializedName

data class KmbEtaReq(
    @SerializedName("token") val token: String = "",
    @SerializedName("t") val t: String = ""
)