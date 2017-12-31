package hoo.etahk.model.data

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import hoo.etahk.model.json.Info
import hoo.etahk.model.json.StringLang

//@Entity(indices = [(Index(value = ["company", "routeNo", "bound", "variant"],
//        name = "idx_route_key",
//        unique = true))]
//)
@Entity(primaryKeys = ["company", "routeNo", "bound", "variant"])
data class Route(
        @Embedded
        var routeKey: RouteKey,
        var direction: Long,
        var companyDetails: List<String>,               // store as json string
        var specialCode: Long = -1L,
        var from: StringLang = StringLang(),            // store as json string
        var to: StringLang = StringLang(),              // store as json string
        var details: StringLang = StringLang(),         // store as json string
        var path: StringLang = StringLang(),            // store as json string
        var info: Info = Info(),
        var eta: Boolean = false,
        var seq: Long = -1L,
        var infoBatchUpdate: Boolean = false,
        var infoUpdateTime: Long = 0L,
        var updateTime: Long = 0L) {

    fun getToByBound(): StringLang {
        return if (routeKey.variant > 0) getToByBound(routeKey.bound) else StringLang()
    }

    fun getToByBound(bound: Long): StringLang {
        return when(bound) {
            1L -> to
            2L -> from
            else -> StringLang()
        }
    }
}