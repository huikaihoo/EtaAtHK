package hoo.etahk.remote.response

import com.google.gson.annotations.SerializedName
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop

data class GistDatabaseRes(
    var metadata: Metadata = Metadata(),
    var parentRoutes: List<Route> = listOf(),
    var childRoutes: List<Route> = listOf(),
    var stops: List<Stop> = listOf()
) {
    data class Metadata(
        @SerializedName("parent_cnt") val parentCnt: Int? = 0,
        @SerializedName("child_cnt") val childCnt: Int? = 0,
        @SerializedName("stops_cnt") val stopsCnt: Int? = 0,
        @SerializedName("routes") val routes: List<String?>? = listOf(),
        @SerializedName("timestamp") val timestamp: Long? = -1L
    )

    val isValid: Boolean
        get() = !metadata.routes.isNullOrEmpty() &&
                metadata.parentCnt == parentRoutes.size &&
                metadata.childCnt == childRoutes.size &&
                metadata.stopsCnt == stops.size
}