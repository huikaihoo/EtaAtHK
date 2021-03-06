package hoo.etahk.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import hoo.etahk.common.extensions.toLocation

@Entity
data class FollowLocation(
        @PrimaryKey(autoGenerate = true)
        var Id: Long? = null,
        var name: String = "",
        var icon: String = "",
        var latitude: Double = -1.0,
        var longitude: Double = -1.0,
        var distance: Double = 0.0,
        var pin: Boolean = false,
        var defaultGroupId: Long = -1L,     // reserve
        var displaySeq: Long = -1L,
        var updateTime: Long = 0L) {

    var latLng
        get() = LatLng(latitude, longitude)
        set(value) {
            latitude = value.latitude
            longitude = value.longitude
        }

    var location
        get() = latLng.toLocation()
        set(value) {
            latitude = value.latitude
            longitude = value.longitude
        }
}
