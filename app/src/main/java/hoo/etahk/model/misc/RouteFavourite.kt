package hoo.etahk.model.misc

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import hoo.etahk.common.Constants
import hoo.etahk.model.data.Misc

@Entity
data class RouteFavourite(
    @PrimaryKey(autoGenerate = true)
    var Id: Long? = null,
    @ColumnInfo(name = "dataStrA")
    var company: String,
    @ColumnInfo(name = "dataStrB")
    var routeNo: String,
    @ColumnInfo(name = "dataStrC")
    var anotherCompany: String?
) : BaseMisc(Constants.MiscType.ROUTE_FAVOURITE) {

    // For relationship: Do not touch it!
    var relationStr: String = company + "_" + routeNo + "_0_0"
        get() = company + "_" + routeNo + "_0_0"

    override fun toMisc(): Misc {
        return Misc(
            Id = Id,
            miscType = miscType,
            relationStr = relationStr,
            dataStrA = company,
            dataStrB = routeNo,
            dataStrC = anotherCompany,
            displaySeq = displaySeq,
            updateTime = updateTime
        )
    }
}
