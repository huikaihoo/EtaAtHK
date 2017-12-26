package hoo.etahk.common.connection

import hoo.etahk.model.data.Stop

interface BaseConnection {

    fun updateEta(stop: Stop)

}