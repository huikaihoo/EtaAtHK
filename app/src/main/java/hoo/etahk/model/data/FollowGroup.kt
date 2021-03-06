package hoo.etahk.model.data

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

@Entity(indices = [Index(value = ["locationId"],
                          name = "idx_location_id")],
        foreignKeys = [(ForeignKey(entity = FollowLocation::class,
                                   parentColumns = ["Id"],
                                   childColumns = ["locationId"],
                                   onDelete = CASCADE))])
data class FollowGroup(
        @PrimaryKey(autoGenerate = true)
        var Id: Long? = null,
        var locationId: Long,
        var name: String,
        var displaySeq: Long,
        var updateTime: Long = 0L): Comparable<FollowGroup> {
    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: FollowGroup) = displaySeq.compareTo(other.displaySeq)

    @Ignore
    var locationName: String = ""
}
