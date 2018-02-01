package hoo.etahk.model.relation

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import hoo.etahk.model.data.FollowGroup

class GroupAndItems {
    @Embedded
    lateinit var group: FollowGroup

    @Relation(parentColumn = "Id",
              entityColumn = "groupId")
    var itemAndStop: List<ItemAndStop> = listOf()
}
