package hoo.etahk.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import hoo.etahk.model.data.FollowItem
import hoo.etahk.model.data.Stop

class ItemAndStop {
    @Embedded
    lateinit var item: FollowItem

    @Relation(
        parentColumn = "stopStr",
        entityColumn = "stopStr"
    )
    var stops: List<Stop> = listOf()

    val stop: Stop?
        get() = if (stops.isEmpty()) null else stops[0]
}
