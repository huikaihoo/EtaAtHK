package hoo.etahk.model.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import hoo.etahk.common.Constants
import hoo.etahk.model.misc.RouteFavourite
import hoo.etahk.model.relation.RouteFavouriteEx

@Dao
abstract class MiscFavouriteDao {
    // Count
    @Query("SELECT COUNT(*) FROM Misc WHERE miscType = :miscType")
    abstract fun count(miscType: Constants.MiscType = Constants.MiscType.ROUTE_FAVOURITE): Int

    // Select
    @Query("SELECT * FROM Misc WHERE dataStrA = :company AND dataStrB = :routeNo AND miscType = :miscType LIMIT 1")
    abstract fun selectOnce(company: String, routeNo: String, miscType: Constants.MiscType = Constants.MiscType.ROUTE_FAVOURITE): RouteFavourite?

    @Transaction
    @Query("SELECT Id, miscType, relationStr, dataStrA, dataStrB, displaySeq, updateTime FROM Misc WHERE miscType = :miscType ORDER BY displaySeq")
    abstract fun selectDS(miscType: Constants.MiscType = Constants.MiscType.ROUTE_FAVOURITE): DataSource.Factory<Integer, RouteFavouriteEx>
}

