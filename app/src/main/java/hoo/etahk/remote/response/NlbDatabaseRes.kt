package hoo.etahk.remote.response

import com.google.gson.annotations.SerializedName

data class NlbDatabaseRes(
    @SerializedName("route_stops") val routeStops: List<RouteStop?>? = listOf(),
    @SerializedName("routes") val routes: List<Route?>? = listOf(),
    @SerializedName("special_routes") val specialRoutes: List<SpecialRoute?>? = listOf(),
    @SerializedName("stop_districts") val stopDistricts: List<StopDistrict?>? = listOf(),
    @SerializedName("stops") val stops: List<Stop?>? = listOf(),
    @SerializedName("version") val version: String? = ""
) {
    data class RouteStop(
        @SerializedName("fare") val fare: Double? = 0.0,
        @SerializedName("fare_holiday") val fareHoliday: Double? = 0.0,
        @SerializedName("route_id") val routeId: String? = "",
        @SerializedName("some_departure_observe_only") val someDepartureObserveOnly: Long? = 0,
        @SerializedName("stop_id") val stopId: String? = "",
        @SerializedName("stop_sequence") val stopSequence: Long? = 0L
    )

    data class Route(
        @SerializedName("additional_description_c") val additionalDescriptionC: String? = "",
        @SerializedName("additional_description_e") val additionalDescriptionE: String? = "",
        @SerializedName("additional_description_s") val additionalDescriptionS: String? = "",
        @SerializedName("overnight_route") val overnightRoute: Long? = 0,
        @SerializedName("route_id") val routeId: String? = "",
        @SerializedName("route_name_c") val routeNameC: String? = "",
        @SerializedName("route_name_e") val routeNameE: String? = "",
        @SerializedName("route_name_s") val routeNameS: String? = "",
        @SerializedName("route_no") val routeNo: String? = "",
        @SerializedName("special_route") val specialRoute: Long? = 0,
        @SerializedName("time_table_c") val timeTableC: String? = "",
        @SerializedName("time_table_e") val timeTableE: String? = "",
        @SerializedName("time_table_s") val timeTableS: String? = "",
        @SerializedName("trip_distance") val tripDistance: Double? = 0.0,
        @SerializedName("trip_time") val tripTime: Long? = 0
    )

    data class SpecialRoute(
        @SerializedName("additional_description_c") val additionalDescriptionC: String? = "",
        @SerializedName("additional_description_e") val additionalDescriptionE: String? = "",
        @SerializedName("additional_description_s") val additionalDescriptionS: String? = "",
        @SerializedName("route_name_c") val routeNameC: String? = "",
        @SerializedName("route_name_e") val routeNameE: String? = "",
        @SerializedName("route_name_s") val routeNameS: String? = "",
        @SerializedName("route_no") val routeNo: String? = "",
        @SerializedName("special_route_id") val specialRouteId: String? = ""
    )

    data class StopDistrict(
        @SerializedName("district_name_c") val districtNameC: String? = "",
        @SerializedName("district_name_e") val districtNameE: String? = "",
        @SerializedName("district_name_s") val districtNameS: String? = "",
        @SerializedName("stop_district_id") val stopDistrictId: String? = ""
    )

    data class Stop(
        @SerializedName("latitude") val latitude: Double? = 0.0,
        @SerializedName("longitude") val longitude: Double? = 0.0,
        @SerializedName("stop_district_id") val stopDistrictId: String? = "",
        @SerializedName("stop_id") val stopId: String? = "",
        @SerializedName("stop_location_c") val stopLocationC: String? = "",
        @SerializedName("stop_location_e") val stopLocationE: String? = "",
        @SerializedName("stop_location_s") val stopLocationS: String? = "",
        @SerializedName("stop_name_c") val stopNameC: String? = "",
        @SerializedName("stop_name_e") val stopNameE: String? = "",
        @SerializedName("stop_name_s") val stopNameS: String? = ""
    )
}