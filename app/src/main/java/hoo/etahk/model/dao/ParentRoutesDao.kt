package hoo.etahk.model.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import hoo.etahk.common.Constants.OrderBy
import hoo.etahk.model.data.Route

@Dao
abstract class ParentRoutesDao {

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

    // Select
    @Query(PARENT_ROUTE_SELECT +
            "ORDER BY " +
            "CASE WHEN typeCode < 10 THEN typeCode ELSE 10 END, " +
            "CASE WHEN typeCode < 10 THEN Seq ELSE Seq END, " +
            "routeNo")
    protected abstract fun selectOrderByBus(typeCodes: List<Long>): LiveData<List<Route>>

    // Select
    @Query(PARENT_ROUTE_SELECT +
            "ORDER BY typeCode, seq, routeNo")
    protected abstract fun selectOrderByTypCodeTypeSeq(typeCodes: List<Long>): LiveData<List<Route>>

    // Select
    @Query(PARENT_ROUTE_SELECT +
            "ORDER BY seq, routeNo")
    protected abstract fun selectOrderByTypeSeq(typeCodes: List<Long>): LiveData<List<Route>>

    // Select
    @Query(PARENT_ROUTE_SELECT +
            "ORDER BY seq, routeNo")
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
        insert(routes)
        update(routes)
        if (updateTime != null) {
            delete(updateTime)
        }
    }

    // Delete
    @Delete
    abstract fun delete(route: Route)

    @Query("DELETE FROM route " +
            "WHERE bound = 0 " +
            "AND updateTime < :updateTime")
    abstract fun delete(updateTime: Long)
}
