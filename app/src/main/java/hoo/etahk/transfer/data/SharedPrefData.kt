package hoo.etahk.transfer.data

import hoo.etahk.common.Constants

data class SharedPrefData(
    // Parameters
    // App
    var pagedListPageSize: String = Constants.SharePrefs.DEFAULT_PAGED_LIST_PAGE_SIZE,
    // Gist
    var gistIdKmb: String = "",
    var gistIdNwfb: String = "",
    // OkHttp
    var userAgent: String = Constants.SharePrefs.DEFAULT_USER_AGENT
)