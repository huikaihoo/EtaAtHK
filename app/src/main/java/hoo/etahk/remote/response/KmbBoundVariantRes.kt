package hoo.etahk.remote.response
import com.google.gson.annotations.SerializedName

data class KmbBoundVariantRes(
		@SerializedName("data") val data: Data? = Data(),
		@SerializedName("result") val result: Boolean? = false
) {
	data class Data(
			@SerializedName("CountSpecal") val countSpecial: Long? = 0L,
			@SerializedName("routes") val routes: List<Route?>? = listOf()
	)

	data class Route(
			@SerializedName("Bound") val bound: Long? = 0L,
			@SerializedName("Desc_CHI") val descChi: String? = "",
			@SerializedName("Desc_ENG") val descEng: String? = "",
			@SerializedName("Destination_CHI") val destinationChi: String? = "",
			@SerializedName("Destination_ENG") val destinationEng: String? = "",
			@SerializedName("From_holiday") val fromHoliday: String? = "",
			@SerializedName("From_saturday") val fromSaturday: String? = "",
			@SerializedName("From_weekday") val fromWeekday: String? = "",
			@SerializedName("Origin_CHI") val originChi: String? = "",
			@SerializedName("Origin_ENG") val originEng: String? = "",
			@SerializedName("Route") val route: String? = "",
			@SerializedName("ServiceType") val serviceType: String? = "",
			@SerializedName("To_holiday") val toHoliday: String? = "",
			@SerializedName("To_saturday") val toSaturday: String? = "",
			@SerializedName("To_weekday") val toWeekday: String? = ""
	)
}

