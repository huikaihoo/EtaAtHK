package hoo.etahk.model.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop

@Dao
abstract class StopDao {

    companion object {
        const val STOP_COND =
            "WHERE company = :company " +
            "AND routeNo = :routeNo " +
            "AND bound = :bound " +
            "AND variant = :variant "
    }

    // Count
    @Query("SELECT COUNT(*) FROM stop")
    abstract fun count(): Int

    @Query("SELECT IFNULL(MIN(updateTime), 0) FROM stop $STOP_COND ORDER BY seq")
    abstract fun lastUpdate(company: String,
                            routeNo: String,
                            bound: Long,
                            variant: Long): Long

    // Select
    @Query("SELECT * FROM stop $STOP_COND ORDER BY seq")
    abstract fun select(company: String,
                        routeNo: String,
                        bound: Long,
                        variant: Long): LiveData<List<Stop>>

    @Query("SELECT * FROM stop $STOP_COND ORDER BY seq")
    abstract fun selectOnce(company: String,
                            routeNo: String,
                            bound: Long,
                            variant: Long): List<Stop>

//    @Query("SELECT * FROM stop " +
//            "WHERE company = :company " +
//            "AND routeNo = :routeNo " +
//            "AND bound = :bound " +
//            "AND variant = :variant " +
//            "ORDER BY seq")
//    abstract fun selectOnce(company: String,
//                            routeNo: String,
//                            bound: Long,
//                            variant: Long): List<Stop>

    // Insert / Update (single)
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    abstract fun insert(stop: Stop)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(stop: Stop)

    // Insert / Update (list)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(stops: List<Stop>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract fun update(stops: List<Stop>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateOnReplace(stops: List<Stop>)

    @Transaction
    open fun insertOrUpdate(routeKey: RouteKey? = null, stops: List<Stop>, updateTime: Long? = null) {
        insert(stops)
        update(stops)
        if (routeKey != null && updateTime != null) {
            delete(routeKey.company,
                    routeKey.routeNo,
                    routeKey.bound,
                    routeKey.variant,
                    updateTime)
        }
    }

    @Transaction
    open fun insertOnDeleteOld(companies: List<String>, stops: List<Stop>) {
        companies.forEach {
            delete(it)
        }
        insert(stops)
    }

    // Delete
//    @Delete
//    abstract fun delete(stop: Stop)

    @Query("DELETE FROM stop WHERE company = :company")
    abstract fun delete(company: String)

    @Query("DELETE FROM stop $STOP_COND AND updateTime < :updateTime")
    abstract fun delete(company: String,
                        routeNo: String,
                        bound: Long,
                        variant: Long,
                        updateTime: Long)
}