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

    object Permission {
        const val PERMISSIONS_REQUEST_LOCATION = 1
        const val PERMISSIONS_REQUEST_STORAGE = 2
    }

    object Request {
        const val REQUEST_PLACE_PICKER = 1
        const val REQUEST_LOCATION_ADD = 2
        const val REQUEST_LOCATION_UPDATE = 3
    }

    object Notification {
        const val NOTIFICATION_UPDATE_ROUTES = 101
    }

    object BroadcastIndent {
        const val FINISH_UPDATE_ROUTES = "hoo.etahk.broadcast.FinishUpdateRoutes"
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
        const val MTRB = "MTRB" // MTR Bus
        // Not Support ETA (Bus)
        const val DB = "DB"
        const val PI = "PI"
        const val LRT_FEEDER = "LRTFeeder"
        // Green Minibus
        const val GMB = "GMB"
        // Tram
        const val TRAM = "TRAM"
        // MTR
        const val MTR = "MTR"
        const val AES = "AES" // Airport Express Shuttle
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
        const val GMB_HKI = 21L
        const val GMB_KL = 22L
        const val GMB_NT = 23L
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
        const val NWFB_URL = "http://mobile.nwstbus.com.hk/"
        const val NLB_URL = "https://nlb.kcbh.com.hk:8443/"
        const val GOV_URL = "http://app1.hketransport.td.gov.hk/"
        const val GOV2_URL = "http://cms.hkemobility.gov.hk/"
        const val TRAM_URL = "http://hktramways.com/"
        const val GIST_URL = "https://api.github.com/gists/"
    }

    object NetworkType {
        const val DEFAULT = 0L
        const val LONG = 1L
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

        const val TRAM_STOP_RECORD_SIZE = 6
        const val TRAM_STOP_RECORD_STOP_ID = 0
        const val TRAM_STOP_RECORD_NAME_EN = 1
        const val TRAM_STOP_RECORD_NAME_TC = 2
        const val TRAM_STOP_RECORD_NAME_SC = 3
        const val TRAM_STOP_RECORD_LATITUDE = 4
        const val TRAM_STOP_RECORD_LONGITUDE = 5
    }

    object Eta {
        const val NWFB_ETA_RECORD_SIZE = 31
        const val NWFB_ETA_RECORD_COMPANY = 0
        const val NWFB_ETA_RECORD_DISTANCE = 13
        const val NWFB_ETA_RECORD_ETA_TIME = 16
        const val NWFB_ETA_RECORD_MSG = 26

        const val TRAM_ETA_RECORD_ETA_TIME = "eat"
        const val TRAM_ETA_RECORD_DEST_TC = "tram_dest_tc"
        const val TRAM_ETA_RECORD_DEST_EN = "tram_dest_en"
    }
}

