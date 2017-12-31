package hoo.etahk.common

object Constants {
    enum class AppMode {
        DEV,
        BETA,
        RELEASE
    }

    object Time {
        val ONE_SECOND_IN_MILLIS = 1000L
        val ONE_MINUTE_IN_SECONDS = 60L
        val ONE_DAY_IN_SECONDS = 86400L
    }

    object Company {
        val KMB = "KMB"
        val LWB = "LWB"
        val NWFB = "NWFB"
        val CTB = "CTB"
        val MTR = "MTR"
    }

    object Url {
        val KMB_URL = "http://search.kmb.hk/KMBWebSite/"
        val KMB_ETA_URL = "http://etav3.kmb.hk/"
        val KMB_ETA_FEED_URL = "http://etadatafeed.kmb.hk:1933/"
        val NWFB_URL = "http://mobile.nwstbus.com.hk/"
    }
    object Eta {
        // >>> Change to share pref
        val ETA_AUTO_REFRESH = 30L
        val HIGHLIGHT_B4_DEPARTURE = 5
        val NWFB_API_PARAMETER_APP_VERSION = "3.3.1"
        // <<< Change to share pref

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

