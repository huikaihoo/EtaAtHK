package hoo.etahk.transfer.data

import hoo.etahk.common.constants.SharedPrefs

data class SharedPrefData(
    // Preference
    // Bus
    var busJointly: String = SharedPrefs.BUS_JOINTLY_ALWAYS_ASK,
    // Parameters
    // Firebase
    var userUUID: String = "",
    // App
    var appMode: String = "",
    var pagedListPageSize: String = SharedPrefs.DEFAULT_PAGED_LIST_PAGE_SIZE.toString(),
    // Gist
    var gistIdKmb: String = "",
    var gistIdNwfb: String = "",
    var gistIdMtrb: String = "",
    // OkHttp
    var userAgent: String = SharedPrefs.DEFAULT_USER_AGENT
)