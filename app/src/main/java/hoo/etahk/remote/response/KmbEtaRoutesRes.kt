package hoo.etahk.remote.response

import com.google.gson.annotations.SerializedName

data class KmbEtaRoutesRes(
    @SerializedName("r_no") val rNo: String? = ""   // "1,1A,..."
)