package hoo.etahk.model.misc

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import hoo.etahk.common.Constants

@Entity
data class RouteHist(
        @PrimaryKey(autoGenerate = true)
        var Id: Long? = null,
        var miscType: Constants.MiscType = Constants.MiscType.NONE,
        var company: String,
        var routeNo: String,
        var displaySeq: Long,
        var updateTime: Long = 0L)
