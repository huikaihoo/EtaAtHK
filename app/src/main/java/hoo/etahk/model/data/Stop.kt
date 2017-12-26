package hoo.etahk.model.data

import android.arch.persistence.room.*
import com.google.android.gms.maps.model.LatLng

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
        var name: StringLang = StringLang(),
        var to: StringLang = StringLang(),
        var details: StringLang = StringLang(),
        var latitude: Double = 0.0,
        var longitude: Double = 0.0,
        var fare: Double = -1.0,
        var info: String = "",
        var etaUrl: String = "",
        var etaResults: List<EtaResult> = emptyList(),    // json string
        var etaUpdateTime: Long = 0L,
        var updateTime: Long = 0L) {
    @Ignore
    var location = LatLng(0.0, 0.0)
        get() = LatLng(latitude, longitude)
        private set

    @Ignore
    var etaStatus: Int = 0
}