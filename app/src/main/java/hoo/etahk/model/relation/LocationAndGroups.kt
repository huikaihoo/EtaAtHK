package hoo.etahk.model.relation

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Relation
import hoo.etahk.model.data.FollowGroup
import hoo.etahk.model.data.FollowLocation
class LocationAndGroups {
    @Embedded
    lateinit var location: FollowLocation

    @Relation(parentColumn = "Id",
              entityColumn = "locationId")
    var groups: List<FollowGroup> = listOf()
        get() = field.sorted()

    @Ignore
    var selectedGroupPosition: Int = 0
}
