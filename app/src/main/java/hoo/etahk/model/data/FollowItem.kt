package hoo.etahk.model.data

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE

@Entity(indices = [Index(value = ["groupId"],
                         name = "idx_group_id")],
        foreignKeys = [(ForeignKey(entity = FollowGroup::class,
                                   parentColumns = ["Id"],
                                   childColumns = ["groupId"],
                                   onDelete = CASCADE))])
data class FollowItem(
        @PrimaryKey(autoGenerate = true)
        var Id: Long? = null,
        var groupId: Long,
        @Embedded
        var routeKey: RouteKey,
        var seq: Long = -1L,
        var header: String = "",
        var displaySeq: Long,
        var updateTime: Long = 0L) {

    // For relationship: Do not touch it!
    var stopStr: String = routeKey.routeStr + "_" + seq
        get() = routeKey.routeStr + "_" + seq

}
