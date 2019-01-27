package hoo.etahk.transfer.data

import hoo.etahk.common.constants.SharePrefs

data class SharedPrefData(
    // Parameters
    // Firebase
    var userUUID: String = "",
    // App
    var appMode: String = "",
    var pagedListPageSize: String = SharePrefs.DEFAULT_PAGED_LIST_PAGE_SIZE,
    // Gist
    var gistIdKmb: String = "",
    var gistIdNwfb: String = "",
    // OkHttp
    var userAgent: String = SharePrefs.DEFAULT_USER_AGENT
)