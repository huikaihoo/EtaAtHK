package hoo.etahk.model.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import hoo.etahk.model.data.Path
import hoo.etahk.model.data.Route

@Dao
abstract class PathDao {

    companion object {
        const val PATH_COND =
            "WHERE company = :company " +
            "AND routeNo = :routeNo " +
            "AND bound = :bound " +
            "AND variant = :variant "
    }

    // Count
    @Query("SELECT COUNT(*) FROM path")
    abstract fun count(): Int

    // Select
    @Query("SELECT * FROM path $PATH_COND ORDER BY seq")
    abstract fun select(company: String,
                        routeNo: String,
                        bound: Long,
                        variant: Long): LiveData<List<Path>>

    // Insert / Update (list)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(stops: List<Path>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract fun update(stops: List<Path>)

    @Transaction
    open fun insertOrUpdate(route: Route? = null, paths: List<Path>, updateTime: Long? = null) {
        insert(paths)
        update(paths)
        if (route != null && updateTime != null) {
            delete(route.routeKey.company,
                    route.routeKey.routeNo,
                    route.routeKey.bound,
                    route.routeKey.variant,
                    updateTime)
        }
    }

    // Delete
    @Query("DELETE FROM path $PATH_COND AND updateTime < :updateTime")
    abstract fun delete(company: String,
                        routeNo: String,
                        bound: Long,
                        variant: Long,
                        updateTime: Long)
}