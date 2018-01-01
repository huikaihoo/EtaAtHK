package hoo.etahk.remote.response
import com.google.gson.annotations.SerializedName

data class KmbStopsRes(
		@SerializedName("data") val data: Data? = Data(),
		@SerializedName("result") val result: Boolean? = false
) {
    data class Data(
            @SerializedName("additionalInfo") val additionalInfo: AdditionalInfo? = AdditionalInfo(),
            @SerializedName("basicInfo") val basicInfo: BasicInfo? = BasicInfo(),
            @SerializedName("route") val route: Route? = Route(),
            @SerializedName("routeStops") val routeStops: List<RouteStop?>? = listOf()
    )

    data class AdditionalInfo(
            @SerializedName("ENG") val eNG: String? = "",
            @SerializedName("SC") val sC: String? = "",
            @SerializedName("TC") val tC: String? = ""
    )

    data class Route(
            @SerializedName("bound") val bound: Long? = 0L,
            @SerializedName("lineGeometry") val lineGeometry: String? = "",
            @SerializedName("route") val route: String? = "",
            @SerializedName("serviceType") val serviceType: Long? = 0L
    )

    data class BasicInfo(
            @SerializedName("Airport") val airport: String? = "",
            @SerializedName("BusType") val busType: String? = "",
            @SerializedName("DestCName") val destCName: String? = "",
            @SerializedName("DestEName") val destEName: String? = "",
            @SerializedName("DestSCName") val destSCName: String? = "",
            @SerializedName("OriCName") val oriCName: String? = "",
            @SerializedName("OriEName") val oriEName: String? = "",
            @SerializedName("OriSCName") val oriSCName: String? = "",
            @SerializedName("Overnight") val overnight: String? = "",
            @SerializedName("Racecourse") val racecourse: String? = "",
            @SerializedName("ServiceTypeENG") val serviceTypeENG: String? = "",
            @SerializedName("ServiceTypeSC") val serviceTypeSC: String? = "",
            @SerializedName("ServiceTypeTC") val serviceTypeTC: String? = "",
            @SerializedName("Special") val special: String? = ""
    )

    data class RouteStop(
            @SerializedName("AirFare") val airFare: Double? = 0.0,
            @SerializedName("BSICode") val bsiCode: String? = "",
            @SerializedName("Bound") val bound: Long? = 0L,
            @SerializedName("CLocation") val cLocation: String? = "",
            @SerializedName("CName") val cName: String? = "",
            @SerializedName("Direction") val direction: String? = "",
            @SerializedName("ELocation") val eLocation: String? = "",
            @SerializedName("EName") val eName: String? = "",
            @SerializedName("Route") val route: String? = "",
            @SerializedName("SCLocation") val sCLocation: String? = "",
            @SerializedName("SCName") val sCName: String? = "",
            @SerializedName("Seq") val seq: Long? = 0L,
            @SerializedName("ServiceType") val serviceType: Long? = 0L,
            @SerializedName("X") val x: Double? = 0.0,   // HK1980 Grid Easting
            @SerializedName("Y") val y: Double? = 0.0    // HK1980 Grid Northing
    )
}

