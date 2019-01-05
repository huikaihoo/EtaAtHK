package hoo.etahk.transfer.data

import com.google.gson.GsonBuilder

abstract class FilesWorker {

    companion object {
        const val backupFolder = "/EtaAtHK/"
        val gson = GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create()!!
    }

    var appData: AppData = AppData()
        protected set
}