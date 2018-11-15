package hoo.etahk.common

object Constants {

    const val DATABASE_NAME = "db"
    const val DATABASE_VERSION = 1

    enum class EtaStatus {
        NONE,
        SUCCESS,
        FAILED,
        NETWORK_ERROR
    }

    enum class MiscType {
        NONE,
        ROUTE_FAVOURITE,
        ROUTE_HISTORY,
        ROUTE_SEARCH,
        FOLLOW_LOCATION_HISTORY
    }

    object AppMode {
        const val DEV = 0L
        const val BETA = 1L
        const val RELEASE = 2L
    }

    object Time {
        const val PROGRESS_BAR_UPDATE_INTERVAL = 10L
        const val ANIMATION_TIME = 300L
        const val ONE_SECOND_IN_MILLIS = 1000L
        const val ONE_MINUTE_IN_SECONDS = 60L
        const val ONE_DAY_IN_SECONDS = 86400L

        const val PATTERN_DISPLAY = "yyyyMMdd HH:mm:ss"
        const val PATTERN_BACKUP_FILE = "yyyyMMdd_HHmmss"
    }

    object SharePrefs {
        const val USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.40 Safari/537.36"

        const val DEFAULT_ETA_AUTO_REFRESH = 30L // 60L
        const val DEFAULT_HIGHLIGHT_B4_DEPARTURE = 5
        const val DEFAULT_OUTDATED_TIME = 1
        const val DEFAULT_PAGE_SIZE = 20

        const val DEFAULT_MAX_REQUESTS_VAL = 64
        const val DEFAULT_MAX_REQUESTS_PER_HOST_VAL = 4
        // Stop
        const val STOP_CONNECTION_TIMEOUT_VAL = 10000L
        const val STOP_READ_TIMEOUT_VAL = 10000L
        const val STOP_WRITE_TIMEOUT_VAL = 10000L
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

        const val NWFB_API_PARAMETER_TYPE_ALL_BUS = "0"
        const val NWFB_API_PARAMETER_TYPE_ETA_BUS = "5"
        const val NWFB_API_PARAMETER_PLATFORM = "android"
        const val NWFB_API_PARAMETER_APP_VERSION = "3.5.5"
        const val NWFB_API_PARAMETER_APP_VERSION_2 = "49"
        const val GOV_API_PARAMETER_PLATFORM = "android"
        const val GOV_API_PARAMETER_APP_VERSION = "3.6"
        const val GOV2_API_PARAMETER_APP_VERSION = "1.0"
        const val GOV_API_PARAMETER_COMPANY_ALL_BUS = "-1"
        const val GOV_API_PARAMETER_COMPANY_ALL_BUS_MINIBUS = "0"
    }

    object Permission {
        const val PERMISSIONS_REQUEST_LOCATION = 1
    }

    object Request {
        const val REQUEST_PLACE_PICKER = 1
    }

    object Argument {
        const val ARG_COMPANY = "company"
        const val ARG_ROUTE_NO = "route_no"
        const val ARG_TYPE_CODE = "type_code"
        const val ARG_GOTO_BOUND = "goto_bound"
        const val ARG_GOTO_SEQ = "goto_seq"
        const val ARG_ACTIONBAR_TITLE = "actionbar_title"
        const val ARG_ACTIONBAR_SUBTITLE = "actionbar_subtitle"
        const val ARG_LATITUDE = "latitude"
        const val ARG_LONGITUDE = "longitude"
        const val ARG_MISC_TYPE = "misc_type"
        const val ARG_LOCATION_ID = "location_id"
        const val ARG_NAME = "name"
        const val ARG_TITLE = "title"
    }

    object Company {
        const val BUS = "BUS"
        const val GOV = "GOV"
        // Support ETA (Bus)
        const val KMB = "KMB"
        const val LWB = "LWB"
        const val NWFB = "NWFB"
        const val CTB = "CTB"
        const val NLB = "NLB"
        // Not Support ETA (Bus)
        const val DB = "DB"
        const val PI = "PI"
        const val LRT_FEEDER = "LRTFeeder"
        // Tram
        const val TRAM = "TRAM"
        // MTR Train
        const val MTR = "MTR"
    }

    object RouteType {
        const val NONE = -1L
        const val BUS_KL_NT = 1L
        const val BUS_HKI = 2L
        const val BUS_CROSS_HARBOUR = 3L
        const val BUS_AIRPORT_LANTAU = 4L
        const val BUS_NIGHT = 10L
        const val BUS_KL_NT_NIGHT = 11L
        const val BUS_HKI_NIGHT = 12L
        const val BUS_CROSS_HARBOUR_NIGHT = 13L
        const val BUS_AIRPORT_LANTAU_NIGHT = 14L
        const val TRAM = 30L
        const val MTR = 100L
    }

    object OrderBy {
        const val SEQ = 0L
        const val TYPE_SEQ = 1L
        const val TYPE_CODE_TYPE_SEQ = 2L
        const val BUS = 3L
    }

    object Url {
        const val KMB_URL = "http://search.kmb.hk/KMBWebSite/"
        const val KMB_ETA_URL = "http://etav3.kmb.hk/"
        const val KMB_ETA_FEED_URL = "http://etadatafeed.kmb.hk:1933/"
        const val NWFB_URL = "http://mobile.nwstbus.com.hk/"
        const val GOV_URL = "http://app1.hketransport.td.gov.hk/"
        const val GOV2_URL = "http://cms.hkemobility.gov.hk/"
    }

    object NetworkType {
        const val DEFAULT = 0L
        const val STOP = 1L
        const val ETA = 2L
    }

    object Route {
        const val GOV_ROUTE_RECORD_SIZE = 16
        const val GOV_ROUTE_RECORD_ROUTE_NO = 1
        const val GOV_ROUTE_RECORD_FROM = 2
        const val GOV_ROUTE_RECORD_TO = 3
        const val GOV_ROUTE_RECORD_BOUND_COUNT = 6
        const val GOV_ROUTE_RECORD_CIRCULAR = 7
        const val GOV_ROUTE_RECORD_SPECIAL = 9
        const val GOV_ROUTE_RECORD_FARE = 11
        const val GOV_ROUTE_RECORD_DETAILS = 13
        const val GOV_ROUTE_RECORD_COMPANIES = 15

        const val NWFB_ROUTE_RECORD_SIZE = 11
        const val NWFB_ROUTE_RECORD_COMPANY = 0
        const val NWFB_ROUTE_RECORD_ROUTE_NO = 1
        const val NWFB_ROUTE_RECORD_DIRECTION = 3
        const val NWFB_ROUTE_RECORD_FROM = 4
        const val NWFB_ROUTE_RECORD_TO = 5
        const val NWFB_ROUTE_RECORD_INFO_BOUND_ID = 7
        const val NWFB_ROUTE_RECORD_INFO_BOUND = 9
        const val NWFB_ROUTE_RECORD_DETAILS = 10

        const val NWFB_VARIANT_RECORD_SIZE = 10
        const val NWFB_VARIANT_RECORD_VARIANT = 0
        const val NWFB_VARIANT_RECORD_RDV = 2
        const val NWFB_VARIANT_RECORD_DETAILS = 3
        const val NWFB_VARIANT_RECORD_START_SEQ = 6
        const val NWFB_VARIANT_RECORD_END_SEQ = 7
        const val NWFB_VARIANT_RECORD_INFO_BOUND = 9
    }

    object Stop {
        const val NWFB_STOP_RECORD_SIZE = 14
        const val NWFB_STOP_RECORD_RDV = 1
        const val NWFB_STOP_RECORD_SEQ = 2
        const val NWFB_STOP_RECORD_STOP_ID = 3
        const val NWFB_STOP_RECORD_LATITUDE = 5
        const val NWFB_STOP_RECORD_LONGITUDE = 6
        const val NWFB_STOP_RECORD_DETAILS = 7
        const val NWFB_STOP_RECORD_TO = 8
        const val NWFB_STOP_RECORD_FARE = 10
    }

    object Eta {
        const val NWFB_ETA_RECORD_SIZE = 31
        const val NWFB_ETA_RECORD_COMPANY = 0
        const val NWFB_ETA_RECORD_DISTANCE = 13
        const val NWFB_ETA_RECORD_ETA_TIME = 16
        const val NWFB_ETA_RECORD_MSG = 26
    }

    // Share Preferences Key
    object Prefs {
        const val APP_MODE = "app_mode"
        const val LANG = "lang"
    }
}

