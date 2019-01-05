package hoo.etahk.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import hoo.etahk.model.data.FollowGroup

class GroupAndItems {
    @Embedded
    lateinit var group: FollowGroup

    @Relation(
        parentColumn = "Id",
        entityColumn = "groupId"
    )
    var itemAndStop: List<ItemAndStop> = listOf()
}
