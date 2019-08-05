package hoo.etahk.remote.request

import com.google.gson.annotations.SerializedName
import hoo.etahk.common.constants.SharedPrefs

data class MtrbEtaRoutesReq(
    @SerializedName("key") val key: String? = "",
    @SerializedName("ver") val ver: String? = SharedPrefs.MTRB_API_PARAMETER_VERSION,
    @SerializedName("language") val language: String? = "zh"
)