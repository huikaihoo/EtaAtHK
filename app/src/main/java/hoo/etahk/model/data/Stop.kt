package hoo.etahk.model.data

import android.arch.persistence.room.*
import com.google.android.gms.maps.model.LatLng
import hoo.etahk.model.json.EtaResult
import hoo.etahk.model.json.StringLang

@Entity(indices = [Index(value = ["company", "routeNo", "direction", "variant", "seq"],
                        name = "idx_stop_key",
                        unique = true )]
)
data class Stop(
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,
        var routeId: Long,
        @Embedded
        var routeKey: RouteKey,
        var seq: Long = -1L,
        var name: StringLang = StringLang(),            // store as json string
        var to: StringLang = StringLang(),              // store as json string
        var details: StringLang = StringLang(),         // store as json string
        var latitude: Double = 0.0,
        var longitude: Double = 0.0,
        var fare: Double = -1.0,
        var info: String = "",
        var etaUrl: String = "",                        // reserved for later
        var etaResults: List<EtaResult> = emptyList(),  // store as json string
        var etaUpdateTime: Long = 0L,
        var updateTime: Long = 0L) {
    @Ignore
    var location = LatLng(0.0, 0.0)
        get() = LatLng(latitude, longitude)
        private set

    @Ignore
    var etaStatus: Int = 0
}