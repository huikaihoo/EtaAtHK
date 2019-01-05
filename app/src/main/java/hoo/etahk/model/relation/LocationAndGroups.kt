package hoo.etahk.model.relation

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import hoo.etahk.model.data.FollowGroup
import hoo.etahk.model.data.FollowLocation

class LocationAndGroups {
    @Embedded
    lateinit var location: FollowLocation

    @Relation(
        parentColumn = "Id",
        entityColumn = "locationId"
    )
    var groups: List<FollowGroup> = listOf()
        get() {
            field.forEach {
                it.locationName = location.name
            }
            return field.sorted()
        }

    @Ignore
    var selectedGroupPosition: Int = 0
}
