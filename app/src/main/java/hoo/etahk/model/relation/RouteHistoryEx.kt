package hoo.etahk.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import hoo.etahk.model.data.Route
import hoo.etahk.model.misc.RouteHistory

class RouteHistoryEx {
    @Embedded
    lateinit var history: RouteHistory

    @Relation(
        parentColumn = "dataStrB",
        entityColumn = "routeNo"
    )
    var routes: List<Route> = listOf()

    val route: Route?
        get() = when {
            routes.isEmpty() -> null
            routes.size == 1 -> routes[0]
            else -> {
                val filtered = routes.filter {
                    it.routeKey.routeStr.endsWith("_0_0") && it.companyDetails.contains(history.company)
                }
                if (filtered.isEmpty()) null else filtered[0]
            }
        }
}
