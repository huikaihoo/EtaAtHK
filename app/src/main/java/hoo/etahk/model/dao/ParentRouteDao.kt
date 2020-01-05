package hoo.etahk.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import hoo.etahk.common.Constants.OrderBy
import hoo.etahk.common.Constants.RouteType
import hoo.etahk.model.data.Route

@Dao
abstract class ParentRouteDao {

    companion object {
        const val PARENT_ROUTE_SELECT =
            "SELECT * FROM route " +
            "WHERE typeCode IN (:typeCodes) " +
            "AND bound = 0 " +
            "AND variant = 0 "
    }

    // Count
    @Query("SELECT COUNT(*) FROM route WHERE bound = 0")
    abstract fun count(): Int

    @Query("SELECT IFNULL(MIN(updateTime), 0) FROM route WHERE bound = 0")
    abstract fun lastUpdate(): Long

    @Query("SELECT IFNULL(MIN(updateTime), 0) FROM route WHERE bound = 0")
    abstract fun lastUpdateForDisplay(): LiveData<Long>

    // Select
    @Query(PARENT_ROUTE_SELECT +
            "ORDER BY " +
            "CASE WHEN typeCode < 10 THEN typeCode ELSE 10 END, " +
            "CASE WHEN typeCode < 10 THEN displaySeq ELSE displaySeq END, " +
            "routeNo")
    protected abstract fun selectOrderByBus(typeCodes: List<Long>): LiveData<List<Route>>

    @Query("$PARENT_ROUTE_SELECT ORDER BY typeCode, displaySeq, routeNo")
    protected abstract fun selectOrderByTypCodeTypeSeq(typeCodes: List<Long>): LiveData<List<Route>>

    @Query("$PARENT_ROUTE_SELECT ORDER BY displaySeq, routeNo")
    protected abstract fun selectOrderByTypeSeq(typeCodes: List<Long>): LiveData<List<Route>>

    @Query("$PARENT_ROUTE_SELECT ORDER BY displaySeq, routeNo")
    protected abstract fun selectOrderBySeq(typeCodes: List<Long>): LiveData<List<Route>>

    fun select(typeCodes: List<Long>, orderBy: Long): LiveData<List<Route>> {
        return when (orderBy) {
            OrderBy.BUS -> selectOrderByBus(typeCodes)
            OrderBy.TYPE_CODE_TYPE_SEQ -> selectOrderByTypCodeTypeSeq(typeCodes)
            OrderBy.TYPE_SEQ -> selectOrderByTypeSeq(typeCodes)
            else -> selectOrderBySeq(typeCodes)
        }
    }

    @Query("SELECT * FROM route WHERE company = :company AND routeNo = :routeNo AND bound = 0 LIMIT 1")
    abstract fun select(company: String, routeNo: String): LiveData<Route>

    @Query("SELECT * FROM route WHERE company = :company AND routeNo = :routeNo AND bound = 0 LIMIT 1")
    abstract fun selectOnce(company: String, routeNo: String): Route

    // Insert / Update (single)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(route: Route)
//
//    @Update(onConflict = OnConflictStrategy.REPLACE)
//    abstract fun update(route: Route)

    // Insert / Update (list)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(routes: List<Route>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract fun update(routes: List<Route>)

    @Transaction
    open fun insertOrUpdate(routes: List<Route>, updateTime: Long? = null) {
        val typeCodeFrom = routes.minBy { it.routeKey.typeCode }?.routeKey?.typeCode ?: RouteType.NONE
        val typeCodeTo = routes.maxBy { it.routeKey.typeCode }?.routeKey?.typeCode ?: RouteType.NONE

        insert(routes)
        update(routes)
        if (updateTime != null) {
            delete(typeCodeFrom, typeCodeTo, updateTime)
        }
    }

    // Delete
    @Delete
    abstract fun delete(route: Route)

    @Query("DELETE FROM route " +
            "WHERE bound = 0 " +
            "AND typeCode >= :typeCodeFrom " +
            "AND typeCode <= :typeCodeTo " +
            "AND updateTime < :updateTime")
    abstract fun delete(typeCodeFrom: Long, typeCodeTo: Long, updateTime: Long)
}
