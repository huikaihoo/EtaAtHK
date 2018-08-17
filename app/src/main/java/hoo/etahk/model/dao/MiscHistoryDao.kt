package hoo.etahk.model.dao

import android.arch.paging.DataSource
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import hoo.etahk.common.Constants
import hoo.etahk.model.misc.RouteHistory
import hoo.etahk.model.relation.RouteHistoryEx

@Dao
abstract class MiscHistoryDao {
    // Count
    @Query("SELECT COUNT(*) FROM Misc WHERE miscType = :miscType")
    abstract fun count(miscType: Constants.MiscType = Constants.MiscType.ROUTE_HISTORY): Int

    // Select
    @Query("SELECT * FROM Misc WHERE dataStrA = :company AND dataStrB = :routeNo AND miscType = :miscType LIMIT 1")
    abstract fun selectOnce(company: String, routeNo: String, miscType: Constants.MiscType = Constants.MiscType.ROUTE_HISTORY): RouteHistory?

    @Transaction
    @Query("SELECT Id, miscType, relationStr, dataStrA, dataStrB, freq, displaySeq, updateTime FROM Misc WHERE miscType = :miscType ORDER BY updateTime DESC")
    abstract fun selectDS(miscType: Constants.MiscType = Constants.MiscType.ROUTE_HISTORY): DataSource.Factory<Integer, RouteHistoryEx>
}

