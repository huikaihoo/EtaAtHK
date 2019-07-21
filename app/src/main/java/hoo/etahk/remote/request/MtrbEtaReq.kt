package hoo.etahk.remote.request

import com.google.gson.annotations.SerializedName
import hoo.etahk.common.constants.SharePrefs

data class MtrbEtaReq(
    @SerializedName("key") val key: String? = "",
    @SerializedName("ver") val ver: String? = SharePrefs.MTRB_API_PARAMETER_VERSION,
    @SerializedName("language") val language: String? = "zh",
    @SerializedName("routeName") val routeName: String? = ""
)