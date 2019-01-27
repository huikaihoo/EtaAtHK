package hoo.etahk.view.search

import hoo.etahk.common.Utils
import hoo.etahk.common.tools.ThemeColor

data class BusRoutesConfig(
        private val titleResId: Int,
        var routeTypes: List<Long>,
        var orderBy: Long,
        val color: ThemeColor) {
    val title: String
        get() = Utils.getString(titleResId)
}