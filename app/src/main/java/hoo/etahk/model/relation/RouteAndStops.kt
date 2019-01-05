package hoo.etahk.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import hoo.etahk.model.data.Path
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop

class RouteAndStops {
    @Embedded
    lateinit var route: Route

    @Relation(
        parentColumn = "routeStr",
        entityColumn = "routeStr"
    )
    var paths: List<Path> = listOf()

    @Relation(
        parentColumn = "routeStr",
        entityColumn = "routeStr"
    )
    var stops: List<Stop> = listOf()
}
