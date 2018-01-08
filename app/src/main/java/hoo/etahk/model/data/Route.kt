package hoo.etahk.model.data

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import hoo.etahk.common.Utils
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
        var specialCode: Long = -1L,                    // from gov (parent routes only)
        var companyDetails: List<String>,               // store as json string
        var from: StringLang = StringLang(),            // store as json string
        var to: StringLang = StringLang(),              // store as json string
        var details: StringLang = StringLang(),         // store as json string
        var path: String = "",                          // reserve
        var info: Info = Info(),
        var eta: Boolean = false,
        var seq: Long = -1L,
        var typeSeq: Long = -1L,
        var infoBatchUpdate: Boolean = false,
        var infoUpdateTime: Long = 0L,
        var updateTime: Long = 0L): Comparable<Route> {

    val boundCount: Long
        get() = if (direction < 2L) 1L else direction

    val childDirection: Long
        get() = if (direction > 1L) 1L else direction

    fun getParentDesc(): String {
        return Utils.getStringResourceByName(routeKey.company.toLowerCase())
    }

    fun getToByBound(): StringLang {
        return if (routeKey.variant > 0) getToByBound(routeKey.bound) else StringLang()
    }

    private fun getToByBound(bound: Long): StringLang {
        return when(bound) {
            1L -> to
            2L -> from
            else -> StringLang()
        }
    }

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: Route) = routeKey.compareTo(other.routeKey)
}