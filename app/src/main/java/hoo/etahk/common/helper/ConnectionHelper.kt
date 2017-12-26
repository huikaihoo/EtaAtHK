package hoo.etahk.common.helper

import hoo.etahk.common.Constants.Company
import hoo.etahk.common.connection.BaseConnection
import hoo.etahk.common.connection.KmbConnection
import hoo.etahk.common.connection.NwfbConnection
import hoo.etahk.model.data.Stop

object ConnectionHelper: BaseConnection {

    private fun getConnection(company: String): BaseConnection? {
        return when (company) {
            Company.KMB -> KmbConnection
            Company.LWB -> NwfbConnection
            Company.NWFB -> NwfbConnection
            Company.CTB -> NwfbConnection
            else -> null
        }
    }

    override fun updateEta(stop: Stop) {
        getConnection(stop.routeKey.company)?.updateEta(stop)
    }
}