package hoo.etahk.remote.connection

import hoo.etahk.model.data.Stop

interface BaseConnection {

    fun updateEta(stop: Stop)

}