package hoo.etahk.model.repo

import android.arch.lifecycle.LiveData
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop

object StopsRepo {

    private val TAG = "StopsRepo"

    // ETA
    fun updateEta(stops: List<Stop>?) {
        stops?.forEach { stop ->
            ConnectionHelper.updateEta(stop)
        }
    }

    // Followed Stops
    fun getFollowedStops(): LiveData<List<Stop>> {
        return AppHelper.db.stopsDao().selectAll()
    }


    // Testing
    fun insertStop() {
        val stop1 = Stop(id = 1,
                routeId = 14,
                routeKey = RouteKey("KMB", "1", 1, 1),
                seq = 1,
                etaUrl = "http://etav3.kmb.hk/?action=getEta&lang=tc&route=45&bound=1&stop=KO03-T-1300-0&stop_seq=0&serviceType=1")

        AppHelper.db.stopsDao().insert(stop1)

        val stop2 = Stop(id = 2,
                routeId = 14,
                routeKey = RouteKey("CTB", "2", 1, 1),
                seq = 1,
                etaUrl = "http://mobile.nwstbus.com.hk/api6/getnextbus2.php?stopid=1531&service_no=E23&removeRepeatedSuspend=Y&l=0&bound=O&stopseq=11&rdv=E23-GTC-2&showtime=Y&removeRepeatedSuspend=Y")

        AppHelper.db.stopsDao().insert(stop2)

        val stop3 = Stop(id = 3,
                routeId = 1,
                routeKey = RouteKey("NWFB", "3", 1, 1),
                seq = 1,
                etaUrl = "http://mobile.nwstbus.com.hk/api6/getnextbus2.php?stopid=1531&service_no=796X&removeRepeatedSuspend=Y&l=0&bound=O&stopseq=11&rdv=E23-GTC-2&showtime=Y&removeRepeatedSuspend=Y")

        AppHelper.db.stopsDao().insert(stop3)

        val stop4 = Stop(id = 4,
                routeId = 14,
                routeKey = RouteKey("CTB", "4", 1, 1),
                seq = 1,
                etaUrl = "http://mobile.nwstbus.com.hk/api6/getnextbus2.php?stopid=1531&service_no=N23&removeRepeatedSuspend=Y&l=0&bound=O&stopseq=11&rdv=E23-GTC-2&showtime=Y&removeRepeatedSuspend=Y")

        AppHelper.db.stopsDao().insert(stop4)
    }
}