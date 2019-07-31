package hoo.etahk.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import hoo.etahk.common.Constants
import hoo.etahk.model.json.Extra

@Entity
data class Misc(
        @PrimaryKey(autoGenerate = true)
        var Id: Long? = null,
        var miscType: Constants.MiscType = Constants.MiscType.NONE,
        var relationStr: String = "", // For relationship: Do not touch it!
        var dataStrA: String? = null,
        var dataStrB: String? = null,
        var dataStrC: String? = null,
        var dataIntA: Int? = null,
        var dataIntB: Int? = null,
        var dataIntC: Int? = null,
        var dataDoubleA: Double? = null,
        var dataDoubleB: Double? = null,
        var dataDoubleC: Double? = null,
        var dataBooleanA: Boolean? = null,
        var dataBooleanB: Boolean? = null,
        var dataBooleanC: Boolean? = null,
        var extra: Extra = Extra(),
        var latitude: Double = -1.0,
        var longitude: Double = -1.0,
        var freq: Long = -1L,
        var displaySeq: Long = -1L,
        var updateTime: Long = 0L) {

    var latLng
        get() = LatLng(latitude, longitude)
        set(value) {
            latitude = value.latitude
            longitude = value.longitude
        }
}
