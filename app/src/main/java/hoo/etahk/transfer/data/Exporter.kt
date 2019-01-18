package hoo.etahk.transfer.data

import android.os.Environment
import android.os.Environment.MEDIA_MOUNTED
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Constants.SharePrefs
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.SharedPrefsHelper
import java.io.File
import java.io.FileOutputStream

class Exporter: FilesWorker() {

    var backupFullPath = ""
        private set

    fun export(database: Boolean = true, sharedPreferences: Boolean = true): String {

        appData.createTime = Utils.getDateTimeString(Utils.getCurrentTimestamp(), Constants.Time.PATTERN_BACKUP_FILE)

        if (database && !prepareDatabase()) {
            return "Error 1"
        }

        if (sharedPreferences && !prepareSharedPref()) {
            return "Error 2"
        }

        if (!backup()) {
            return "Error 3"
        }

        return backupFullPath
    }


    private fun prepareDatabase(): Boolean {
        logd("Start Prepare Database")

        return try {
            val databaseData = DatabaseData(Constants.DATABASE_VERSION)

            databaseData.misc = AppHelper.db.miscDao().exportData()
            databaseData.followLocation = AppHelper.db.locationDao().exportData()
            databaseData.followGroup = AppHelper.db.groupDao().exportData()
            databaseData.followItem = AppHelper.db.itemDao().exportData()

            appData.databaseData = databaseData

            true
        } catch (e: Exception) {
            loge("Prepare Database failed!", e)
            false
        }
    }

    private fun prepareSharedPref(): Boolean {
        logd("Start Prepare Shared Preferences")

        return try {
            val sharedPrefData = SharedPrefData()

            sharedPrefData.userUUID = SharedPrefsHelper.get(R.string.param_user_uuid)
            sharedPrefData.pagedListPageSize = SharedPrefsHelper.get(R.string.param_paged_list_page_size, SharePrefs.DEFAULT_PAGED_LIST_PAGE_SIZE)
            if (SharedPrefsHelper.getAppMode() == Constants.AppMode.DEV) {
                sharedPrefData.gistIdKmb = SharedPrefsHelper.get(R.string.param_gist_id_kmb)
                sharedPrefData.gistIdNwfb = SharedPrefsHelper.get(R.string.param_gist_id_nwfb)
            }
            sharedPrefData.userAgent = SharedPrefsHelper.get(R.string.param_user_agent, SharePrefs.DEFAULT_USER_AGENT)

            appData.sharedPrefData = sharedPrefData

            true
        } catch (e: Exception) {
            loge("Prepare Shared Preferences failed!", e)
            false
        }

    }

    private fun backup(): Boolean {
        if (appData.createTime.isBlank() || Environment.getExternalStorageState() != MEDIA_MOUNTED) {
            return false
        }

        val backupName = backupFolder + "EtaAtHK_" + appData.createTime + ".json"
        logd("Start Export to $backupName")

        return try {
            val str = gson.toJson(appData)

            val file = File(Environment.getExternalStorageDirectory().absolutePath, backupName)
            file.parentFile.mkdirs()

            val fos = FileOutputStream(file)
            fos.write(str.toByteArray())
            fos.close()

            backupFullPath = file.absolutePath

            true
        } catch (e: Exception) {
            loge("Backup failed!", e)
            false
        }
    }
}