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

    object Eta {
        val NWFB_ETA_RECORD_SIZE = 31
        val NWFB_EAT_RECORD_COMPANY = 0
        val NWFB_EAT_RECORD_DISTANCE = 13
        val NWFB_EAT_RECORD_ETA_TIME = 16
        val NWFB_EAT_RECORD_MSG = 26
    }

    // Share Preferences Key
    object Prefs {
        val APP_MODE = "app_mode"
        val LANG = "lang"
    }
}

