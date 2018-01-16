package hoo.etahk.view.search

import hoo.etahk.common.tools.ThemeColor
import hoo.etahk.view.App

data class BusRoutesConfig(
        private val titleResId: Int,
        var routeTypes: List<Long>,
        var orderBy: Long,
        val color: ThemeColor) {
    val title: String
        get() = App.instance.getString(titleResId)
}