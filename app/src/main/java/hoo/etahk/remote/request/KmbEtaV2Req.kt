package hoo.etahk.remote.request

import com.google.gson.annotations.SerializedName

data class KmbEtaV2Req(
    @SerializedName("d") val d: String = "",
    @SerializedName("ctr") val ctr: String = ""
)