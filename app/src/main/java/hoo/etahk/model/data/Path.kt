package hoo.etahk.model.data

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.ForeignKey.CASCADE
import com.google.android.gms.maps.model.LatLng

//@Entity(indices = [Index(value = ["company", "routeNo", "bound", "variant", "seq"],
//                        name = "idx_stop_key",
//                        unique = true )]
//)
@Entity(primaryKeys = ["company", "routeNo", "bound", "variant", "seq"],
        foreignKeys = [ForeignKey(entity = Route::class,
                                  parentColumns = ["company", "routeNo", "bound", "variant"],
                                  childColumns = ["company", "routeNo", "bound", "variant"],
                                  onDelete = CASCADE)])
data class Path(
        @Embedded
        var routeKey: RouteKey,
        var seq: Long = -1L,
        var section: Long = -1L,
        var latitude: Double = 0.0,
        var longitude: Double = 0.0,
        var updateTime: Long = 0L) {

    var location
        get() = LatLng(latitude, longitude)
        set(value) {
            latitude = value.latitude
            longitude = value.longitude
        }
}