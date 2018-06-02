package hoo.etahk.model.data

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import hoo.etahk.common.Constants
import hoo.etahk.model.json.Extra

@Entity
data class Misc(
        @PrimaryKey(autoGenerate = true)
        var Id: Long? = null,
        var miscType: Constants.MiscType = Constants.MiscType.NONE,
        var dataStrA: String,
        var dataStrB: String,
        var dataStrC: String,
        var dataIntA: Int,
        var dataIntB: Int,
        var dataIntC: Int,
        var dataDoubleA: Double,
        var dataDoubleB: Double,
        var dataDoubleC: Double,
        var dataBooleanA: Boolean,
        var dataBooleanB: Boolean,
        var dataBooleanC: Boolean,
        var extra: Extra = Extra(),
        var latitude: Double = 0.0,
        var longitude: Double = 0.0,
        var freq: Long,
        var displaySeq: Long,
        var updateTime: Long = 0L) {

    // For relationship: Do not touch it!
    var relationStr: String = ""
        get() = ""

    var location
        get() = LatLng(latitude, longitude)
        set(value) {
            latitude = value.latitude
            longitude = value.longitude
        }
}
