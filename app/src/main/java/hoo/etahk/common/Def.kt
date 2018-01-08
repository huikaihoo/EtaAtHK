package hoo.etahk.common

import android.support.annotation.IntDef

object Def {
    @IntDef(Constants.AppMode.DEV,
            Constants.AppMode.BETA,
            Constants.AppMode.RELEASE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class AppMode

    @IntDef(Constants.RouteType.NONE,
            Constants.RouteType.BUS_KL_NT,
            Constants.RouteType.BUS_HKI,
            Constants.RouteType.BUS_CROSS_HARBOUR,
            Constants.RouteType.BUS_AIRPORT_LANTAU,
            Constants.RouteType.BUS_KL_NT_NIGHT,
            Constants.RouteType.BUS_HKI_NIGHT,
            Constants.RouteType.BUS_CROSS_HARBOUR_NIGHT,
            Constants.RouteType.BUS_AIRPORT_LANTAU_NIGHT,
            Constants.RouteType.TRAM,
            Constants.RouteType.MTR)
    @Retention(AnnotationRetention.SOURCE)
    annotation class RouteType
}