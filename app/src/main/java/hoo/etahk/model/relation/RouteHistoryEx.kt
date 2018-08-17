package hoo.etahk.model.relation

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import hoo.etahk.model.data.Route
import hoo.etahk.model.misc.RouteHistory

class RouteHistoryEx {
    @Embedded
    lateinit var history: RouteHistory

    @Relation(parentColumn = "relationStr",
              entityColumn = "routeStr")
    var routes: List<Route> = listOf()

    val route: Route?
        get() = if (routes.isEmpty()) null else routes[0]
}