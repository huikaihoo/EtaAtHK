package hoo.etahk.common

object Constants {
    enum class AppMode {
        DEV,
        BETA,
        RELEASE
    }

    enum class EtaStatus {
        NONE,
        LOADING,
        SUCCESS,
        FAILED,
        NETWORK_ERROR
    }

    object Time {
        val ANIMATION_TIME = 300L
        val ONE_SECOND_IN_MILLIS = 1000L
        val ONE_MINUTE_IN_SECONDS = 60L
        val ONE_DAY_IN_SECONDS = 86400L
    }

    object SharePrefs {
        val DEFAULT_ETA_AUTO_REFRESH = 30L // 60L
        val DEFAULT_HIGHLIGHT_B4_DEPARTURE = 5

        val DEFAULT_MAX_REQUESTS = 100
        val DEFAULT_MAX_REQUESTS_PER_HOST = 3
        val DEFAULT_MAX_RETRY_ON_FAILED = 1L
        val DEFAULT_CONNECTION_TIMEOUT = 5000L
        val DEFAULT_READ_TIMEOUT = 5000L
        val DEFAULT_WRITE_TIMEOUT = 5000L
        val NWFB_API_PARAMETER_PLATFORM = "android"
        val NWFB_API_PARAMETER_APP_VERSION = "3.3.1"
    }

    object Company {
        val KMB = "KMB"
        val LWB = "LWB"
        val NWFB = "NWFB"
        val CTB = "CTB"
        val MTR = "MTR"
    }

    object RouteType {
        val BUS_ALL = -1L
        val BUS_ALL_DAY = 0L
        val BUS_KL_NT = 1L
        val BUS_HKI = 2L
        val BUS_CROSS_HARBOUR = 3L
        val BUS_AIRPORT_LANTAU = 4L
        val BUS_ALL_NIGHT = 10L
        val BUS_KL_NT_NIGHT = 11L
        val BUS_HKI_NIGHT = 12L
        val BUS_CROSS_HARBOUR_NIGHT = 13L
        val BUS_AIRPORT_LANTAU_NIGHT = 14L
        val TRAM = 30L
        val MTR = 40L
    }

    object Url {
        val KMB_URL = "http://search.kmb.hk/KMBWebSite/"
        val KMB_ETA_URL = "http://etav3.kmb.hk/"
        val KMB_ETA_FEED_URL = "http://etadatafeed.kmb.hk:1933/"
        val NWFB_URL = "http://mobile.nwstbus.com.hk/"
    }

    object Route {
        val NWFB_VARIANT_RECORD_SIZE = 10
        val NWFB_VARIANT_RECORD_VARIANT = 0
        val NWFB_VARIANT_RECORD_RDV = 2
        val NWFB_VARIANT_RECORD_DETAILS = 3
        val NWFB_VARIANT_RECORD_START_SEQ = 6
        val NWFB_VARIANT_RECORD_END_SEQ = 7
        val NWFB_VARIANT_RECORD_INFO_BOUND = 9
    }
    object Stop {
        val NWFB_STOP_RECORD_SIZE = 14
        val NWFB_STOP_RECORD_RDV = 1
        val NWFB_STOP_RECORD_SEQ = 2
        val NWFB_STOP_RECORD_STOP_ID = 3
        val NWFB_STOP_RECORD_LATITUDE = 5
        val NWFB_STOP_RECORD_LONGITUDE = 6
        val NWFB_STOP_RECORD_DETAILS = 7
        val NWFB_STOP_RECORD_TO = 8
        val NWFB_STOP_RECORD_FARE = 10
    }

    object Eta {
        val NWFB_ETA_RECORD_SIZE = 31
        val NWFB_ETA_RECORD_COMPANY = 0
        val NWFB_ETA_RECORD_DISTANCE = 13
        val NWFB_ETA_RECORD_ETA_TIME = 16
        val NWFB_ETA_RECORD_MSG = 26
    }

    // Share Preferences Key
    object Prefs {
        val APP_MODE = "app_mode"
        val LANG = "lang"
    }
}

