package hoo.etahk.common.constants

import hoo.etahk.R
import hoo.etahk.common.extensions.toIntOrDefault
import hoo.etahk.common.helper.SharedPrefsHelper

@Suppress("MemberVisibilityCanBePrivate")
object SharedPrefs {
    ////////////////
    // Preference //
    ////////////////

    // General
    val language: String
        get() = SharedPrefsHelper.get(R.string.pref_language)

    // Bus
    var busJointly: String
        get() = SharedPrefsHelper.get(R.string.pref_bus_jointly, BUS_JOINTLY_ALWAYS_ASK)
        set(value) = SharedPrefsHelper.put(R.string.param_user_agent, value)
    const val BUS_JOINTLY_DEFAULT_KMB_LWB = "0"
    const val BUS_JOINTLY_DEFAULT_NWFB_CTB = "1"
    const val BUS_JOINTLY_ALWAYS_ASK = "2"

    // General (To-Do)
    const val DEFAULT_DATA_VALIDITY_PERIOD = 1
    const val DEFAULT_ETA_AUTO_REFRESH = 30L // 60L
    const val DEFAULT_HIGHLIGHT_B4_DEPARTURE = 5
    const val DEFAULT_SHOW_FOLLOW_LOCATION_DISTANCE = 1000.0
    const val DEFAULT_SAME_STOP_DISTANCE = 8.0
    const val DEFAULT_NEARBY_STOPS_DISTANCE = 500.0
    const val DEFAULT_NEARBY_STOPS_MAX_NUMBER = 75

    ////////////////
    // Parameters //
    ////////////////

    // App
    var acceptedTerms: Boolean
        get() = SharedPrefsHelper.get(R.string.param_accepted_terms, DEFAULT_ACCEPTED_TERMS)
        set(value) = SharedPrefsHelper.put(R.string.param_accepted_terms, value)
    const val DEFAULT_ACCEPTED_TERMS = false

    var pagedListPageSize: Int
        get() = SharedPrefsHelper.get<String>(R.string.param_paged_list_page_size).toIntOrDefault(DEFAULT_PAGED_LIST_PAGE_SIZE)
        set(value) = SharedPrefsHelper.put(R.string.param_paged_list_page_size, value.toString())
    const val DEFAULT_PAGED_LIST_PAGE_SIZE = 20

    // Feature
    var enableBusList: Boolean
        get() = SharedPrefsHelper.get(R.string.param_enable_bus_list, DEFAULT_ENABLE_BUS_LIST)
        set(value) = SharedPrefsHelper.put(R.string.param_enable_bus_list, value)
    const val DEFAULT_ENABLE_BUS_LIST = true

    var enableGmbList: Boolean
        get() = SharedPrefsHelper.get(R.string.param_enable_gmb_list, DEFAULT_ENABLE_GMB_LIST)
        set(value) = SharedPrefsHelper.put(R.string.param_enable_gmb_list, value)
    const val DEFAULT_ENABLE_GMB_LIST = false

    var enableTramList: Boolean
        get() = SharedPrefsHelper.get(R.string.param_enable_tram_list, DEFAULT_ENABLE_TRAM_LIST)
        set(value) = SharedPrefsHelper.put(R.string.param_enable_tram_list, value)
    const val DEFAULT_ENABLE_TRAM_LIST = false

    var enableMtrList: Boolean
        get() = SharedPrefsHelper.get(R.string.param_enable_mtr_list, DEFAULT_ENABLE_MTR_LIST)
        set(value) = SharedPrefsHelper.put(R.string.param_enable_mtr_list, value)
    const val DEFAULT_ENABLE_MTR_LIST = false

    // Gist
    var gistIdKmb: String
        get() = SharedPrefsHelper.get(R.string.param_gist_id_kmb)
        set(value) = SharedPrefsHelper.put(R.string.param_gist_id_kmb, value)

    var gistIdNwfb: String
        get() = SharedPrefsHelper.get(R.string.param_gist_id_nwfb)
        set(value) = SharedPrefsHelper.put(R.string.param_gist_id_nwfb, value)

    var gistIdMtrb: String
        get() = SharedPrefsHelper.get(R.string.param_gist_id_mtrb)
        set(value) = SharedPrefsHelper.put(R.string.param_gist_id_mtrb, value)

    // OkHttp
    var userAgent: String
        get() = SharedPrefsHelper.get(R.string.param_user_agent, DEFAULT_USER_AGENT)
        set(value) = SharedPrefsHelper.put(R.string.param_user_agent, value)
    const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36"

    // OkHttp (To-Do)
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

    fun resetParams() {
        // Firebase
        SharedPrefsHelper.put(R.string.param_enable_remote_config, true)

        // App
        this.acceptedTerms = DEFAULT_ACCEPTED_TERMS
        this.pagedListPageSize = DEFAULT_PAGED_LIST_PAGE_SIZE

        // Feature
        this.enableBusList = DEFAULT_ENABLE_BUS_LIST
        this.enableGmbList = DEFAULT_ENABLE_GMB_LIST
        this.enableTramList = DEFAULT_ENABLE_TRAM_LIST
        this.enableMtrList = DEFAULT_ENABLE_MTR_LIST

        // OkHttp
        this.userAgent = DEFAULT_USER_AGENT
    }
}