package hoo.etahk.model.data

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import com.google.android.gms.maps.model.LatLng
import hoo.etahk.common.Constants
import hoo.etahk.model.json.EtaResult
import hoo.etahk.model.json.Info
import hoo.etahk.model.json.StringLang

//@Entity(indices = [Index(value = ["company", "routeNo", "bound", "variant", "seq"],
//                        name = "idx_stop_key",
//                        unique = true )]
//)
@Entity(primaryKeys = ["company", "routeNo", "bound", "variant", "seq"],
        foreignKeys = [ForeignKey(entity = Route::class,
                                  parentColumns = ["company", "routeNo", "bound", "variant"],
                                  childColumns = ["company", "routeNo", "bound", "variant"],
                                  onDelete = CASCADE)])
data class Stop(
        @Embedded
        var routeKey: RouteKey,
        var seq: Long = -1L,
        var name: StringLang = StringLang(),            // store as json string
        @ColumnInfo(name = "locTo")
        var to: StringLang = StringLang(),              // store as json string
        var details: StringLang = StringLang(),         // store as json string
        var latitude: Double = 0.0,
        var longitude: Double = 0.0,
        var fare: Double = -1.0,
        var info: Info = Info(),
        var etaStatus: Constants.EtaStatus = Constants.EtaStatus.NONE,
        var etaResults: List<EtaResult> = emptyList(),  // store as json string
        var etaUpdateTime: Long = 0L,
        var updateTime: Long = 0L) {

    // For relationship: Do not touch it!
    var stopStr: String = routeKey.routeStr + "_" + seq
        get() = routeKey.routeStr + "_" + seq

    var location
        get() = LatLng(latitude, longitude)
        set(value) {
            latitude = value.latitude
            longitude = value.longitude
        }

    @Ignore
    var isLoading: Boolean = false
}