package hoo.etahk.remote.response

import com.google.gson.annotations.SerializedName

data class GistRes(
    @SerializedName("files") val files: Files? = Files(),
    @SerializedName("created_at") val createdAt: String? = "",
    @SerializedName("updated_at") val updatedAt: String? = ""
) {
    data class Files(
        @SerializedName("kmb") val kmb: File? = File()
    )

    data class File(
        @SerializedName("filename") val filename: String? = "",
        @SerializedName("type") val type: String? = "",
        @SerializedName("language") val language: String? = "",
        @SerializedName("raw_url") val rawUrl: String? = "",
        @SerializedName("size") val size: Long? = 0L,
        @SerializedName("truncated") val truncated: Boolean? = true,
        @SerializedName("content") val content: String? = ""
    )
}