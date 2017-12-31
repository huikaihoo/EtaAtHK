package hoo.etahk.remote.connection

import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop

interface BaseConnection {
    fun getStops(route: Route)
    fun updateEta(stop: Stop)
}