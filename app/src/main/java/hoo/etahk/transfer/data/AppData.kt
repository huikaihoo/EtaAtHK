package hoo.etahk.transfer.data

import hoo.etahk.BuildConfig
import hoo.etahk.view.App

data class AppData(var createTime: String = "") {
    val packageName: String = BuildConfig.APPLICATION_ID
    val versionCode: Int = BuildConfig.VERSION_CODE
    val versionName: String = App.instance.getVersionName()
    val buildType: String = BuildConfig.BUILD_TYPE

    var databaseData: DatabaseData? = null
    var sharedPrefData: SharedPrefData? = null
}