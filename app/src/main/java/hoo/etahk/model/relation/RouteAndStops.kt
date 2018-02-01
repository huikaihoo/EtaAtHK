package hoo.etahk.model.relation

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop

class RouteAndStops {
    @Embedded
    lateinit var route: Route

    @Relation(parentColumn = "routeStr",
              entityColumn = "routeStr")
    var stops: List<Stop> = listOf()
}
