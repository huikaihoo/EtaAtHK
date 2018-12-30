package hoo.etahk.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import hoo.etahk.model.relation.RouteAndStops

@Dao
abstract class RouteStopsDao {

    companion object {
        const val ROUTE_STOP_SELECT =
            "SELECT * FROM route " +
            "WHERE company = :company " +
            "AND routeNo = :routeNo " +
            "AND variant > 0 "
    }

    // Select
    @Transaction
    @Query("$ROUTE_STOP_SELECT AND bound > 0 ORDER BY bound, variant")
    abstract fun select(company: String, routeNo: String): LiveData<List<RouteAndStops>>

    @Transaction
    @Query("$ROUTE_STOP_SELECT AND bound = :bound ORDER BY variant")
    abstract fun select(company: String, routeNo: String, bound: Long): LiveData<List<RouteAndStops>>
}