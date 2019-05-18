package hoo.etahk.model.data

import androidx.room.Ignore
import hoo.etahk.common.Constants
import hoo.etahk.common.Constants.Company.CTB
import hoo.etahk.common.Constants.Company.DB
import hoo.etahk.common.Constants.Company.KMB
import hoo.etahk.common.Constants.Company.LRT_FEEDER
import hoo.etahk.common.Constants.Company.LWB
import hoo.etahk.common.Constants.Company.NLB
import hoo.etahk.common.Constants.Company.NWFB
import hoo.etahk.common.Constants.Company.PI
import hoo.etahk.common.Constants.RouteType.BUS_AIRPORT_LANTAU
import hoo.etahk.common.Constants.RouteType.BUS_AIRPORT_LANTAU_NIGHT
import hoo.etahk.common.Constants.RouteType.BUS_CROSS_HARBOUR
import hoo.etahk.common.Constants.RouteType.BUS_CROSS_HARBOUR_NIGHT
import hoo.etahk.common.Constants.RouteType.BUS_HKI
import hoo.etahk.common.Constants.RouteType.BUS_HKI_NIGHT
import hoo.etahk.common.Constants.RouteType.BUS_KL_NT
import hoo.etahk.common.Constants.RouteType.BUS_KL_NT_NIGHT
import hoo.etahk.common.Constants.RouteType.NONE
import hoo.etahk.common.Constants.RouteType.TRAM
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.loge

data class RouteKey (
        val company: String,
        val routeNo: String,
        var bound: Long,
        var variant: Long): Comparable<RouteKey> {

    fun getCompanyName(): String {
        return Utils.getStringResourceByName(company.toLowerCase())
    }

    var typeCode: Long = NONE

    // For relationship: Do not touch it!
    var routeStr: String = company +  "_" + routeNo +  "_" + bound.toString() + "_" + variant.toString()
        get() = company +  "_" + routeNo +  "_" + bound.toString() + "_" + variant.toString()

    @Ignore
    var prefix: String = ""
        private set
    @Ignore
    var num: Long = 0L
        private set
    @Ignore
    var suffix: String = ""
        private set

    init {
        splitRouteNo()
        setTypeCode()
    }

    private fun Long.hundreds(): Long {
        return this/100L
    }
    private fun Long.tens(): Long {
        return this/10L
    }

    private fun splitRouteNo() {
        if (routeNo.isNotBlank()) {
            try {
                val routeParts = routeNo.split("[^A-Z0-9]+|(?<=[A-Z])(?=[0-9])|(?<=[0-9])(?=[A-Z])".toRegex())
                val noPrefix = routeParts[0].isEmpty() || routeParts[0][0] in '0'..'9'

                for(i in routeParts.indices) {
                    when (i) {
                        0 ->
                            if (noPrefix)
                                num = routeParts[i].toLong()
                            else
                                prefix = routeParts[i].trim()
                        1 ->
                            if (noPrefix)
                                suffix = routeParts[i].trim()
                            else
                                num = routeParts[i].toLong()
                        2 ->
                            suffix = routeParts[i].trim()
                    }
                }
            } catch (e: Exception) {
                loge("splitRouteNo failed!", e)
            }
        }
    }

    private fun setTypeCode() {
        val h = num.hundreds()
        val t = num.tens()

        typeCode =
            if (company == Constants.Company.TRAM) {
                TRAM
            }else if (prefix == "NA") {
                BUS_AIRPORT_LANTAU_NIGHT
            } else if (prefix == "N") {
                if (company == LWB || company == NLB || (company == CTB && (t == 1L || t == 2L || t == 4L))) {
                    BUS_AIRPORT_LANTAU_NIGHT
                } else if (h == 1L || h == 3L || h == 6L || h == 9L) {
                    BUS_CROSS_HARBOUR_NIGHT
                } else if (company == KMB || t == 70L || t == 79L) {
                    BUS_KL_NT_NIGHT
                } else {
                    BUS_HKI_NIGHT
                }
            } else if (prefix == "A" || prefix == "E" || prefix == "R" || prefix == "S") {
                BUS_AIRPORT_LANTAU
            } else if (prefix == "K") {
                BUS_KL_NT
            } else if (prefix == "B") {
                if (num in 4L..6L) {
                    BUS_AIRPORT_LANTAU
                } else {
                    BUS_KL_NT
                }
            } else if (prefix == "H" || routeNo == "88R") {
                BUS_CROSS_HARBOUR
            } else if (company == DB || company == PI) {
                BUS_AIRPORT_LANTAU
            } else if (routeNo == "629" || t == 38L) {   // Start of prefix: # M P T W X
                BUS_HKI
            } else if (company == CTB && (routeNo == "20" || routeNo == "22" || suffix == "R")) {
                BUS_KL_NT
            } else if (h == 1L || h == 3L || h == 6L || h == 9L || routeNo == "W1") {
                BUS_CROSS_HARBOUR
            } else if (company == KMB || company == LRT_FEEDER || t == 70L || t == 79L) {
                BUS_KL_NT
            } else if (company == NWFB || company == CTB) {
                BUS_HKI
            } else if (company == LWB || company == NLB) {
                BUS_AIRPORT_LANTAU
            } else {
                NONE
            }
    }

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: RouteKey): Int = when {
        prefix != other.prefix -> prefix.compareTo(other.prefix)
        num != other.num -> num.compareTo(other.num)
        suffix != other.suffix -> suffix.compareTo(other.suffix)
        bound != other.bound -> bound.compareTo(other.bound)
        else -> variant.compareTo(other.variant)
    }

    object TypeComparator: Comparator<RouteKey> {
        override fun compare(a: RouteKey, b: RouteKey): Int = 0
    }
}