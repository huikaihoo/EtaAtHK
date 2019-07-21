package hoo.etahk.common.constants

object SharePrefs {
    // General
    const val DEFAULT_DATA_VALIDITY_PERIOD = 1
    const val DEFAULT_ETA_AUTO_REFRESH = 30L // 60L
    const val DEFAULT_HIGHLIGHT_B4_DEPARTURE = 5

    // Parameters
    // App
    const val DEFAULT_PAGED_LIST_PAGE_SIZE = "20"

    // OKHttp3
    // Default
    const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36"
    const val DEFAULT_MAX_REQUESTS_VAL = 64
    const val DEFAULT_MAX_REQUESTS_PER_HOST_VAL = 4
    // Long
    const val LONG_CONNECTION_TIMEOUT_VAL = 10000L
    const val LONG_READ_TIMEOUT_VAL = 10000L
    const val LONG_WRITE_TIMEOUT_VAL = 10000L
    // KMB Eta
    const val ETA_KMB_MAX_REQUESTS_PER_HOST_VAL = 4
    const val ETA_KMB_CONNECTION_TIMEOUT_VAL = 3500L
    const val ETA_KMB_READ_TIMEOUT_VAL = 3500L
    const val ETA_KMB_WRITE_TIMEOUT_VAL = 3500L
    // NWFB Eta
    const val ETA_NWFB_MAX_REQUESTS_PER_HOST_VAL = 2
    const val ETA_NWFB_CONNECTION_TIMEOUT_VAL = 7000L
    const val ETA_NWFB_READ_TIMEOUT_VAL = 7000L
    const val ETA_NWFB_WRITE_TIMEOUT_VAL = 7000L

    // API
    // NWFB
    const val NWFB_API_PARAMETER_TYPE_ALL_BUS = "0"
    const val NWFB_API_PARAMETER_TYPE_ETA_BUS = "5"
    const val NWFB_API_PARAMETER_PLATFORM = "android"
    const val NWFB_API_PARAMETER_APP_VERSION = "3.5.5"
    const val NWFB_API_PARAMETER_APP_VERSION_2 = "49"
    // MTRB
    const val MTRB_API_PARAMETER_VERSION = "1"
    // GOV
    const val GOV_API_PARAMETER_PLATFORM = "android"
    const val GOV_API_PARAMETER_APP_VERSION = "3.6"
    const val GOV2_API_PARAMETER_APP_VERSION = "1.0"
    const val GOV_API_PARAMETER_COMPANY_ALL_BUS = "-1"
    const val GOV_API_PARAMETER_COMPANY_ALL_BUS_GMB = "0"
    const val GOV_API_PARAMETER_COMPANY_ALL_GMB = "20"
}