package hoo.etahk.model.data

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import com.google.android.gms.maps.model.LatLng
import hoo.etahk.common.Constants
import hoo.etahk.model.json.EtaResult
import hoo.etahk.model.json.Info
import hoo.etahk.model.json.StringLang

//@Entity(indices = [Index(value = ["company", "routeNo", "bound", "variant", "seq"],
//                        name = "idx_stop_key",
//                        unique = true )]
//)
@Entity(primaryKeys = ["company", "routeNo", "bound", "variant", "seq"])
data class Stop(
        @Embedded
        var routeKey: RouteKey,
        var seq: Long = -1L,
        var name: StringLang = StringLang(),            // store as json string
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
    var location
        get() = LatLng(latitude, longitude)
        set(value) {
            latitude = value.latitude
            longitude = value.longitude
        }
}