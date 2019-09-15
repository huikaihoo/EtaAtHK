package hoo.etahk.view.route

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import hoo.etahk.R
import hoo.etahk.common.Utils
import hoo.etahk.common.constants.SharedPrefs
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.model.data.Stop
import hoo.etahk.model.diff.BaseDiffCallback
import hoo.etahk.model.diff.StopDiffCallback
import hoo.etahk.view.App
import hoo.etahk.view.base.BaseViewHolder
import hoo.etahk.view.base.DiffAdapter
import kotlinx.android.synthetic.main.item_stop.view.eta_0
import kotlinx.android.synthetic.main.item_stop.view.eta_1
import kotlinx.android.synthetic.main.item_stop.view.eta_2
import kotlinx.android.synthetic.main.item_stop.view.fare
import kotlinx.android.synthetic.main.item_stop.view.stop_desc
import kotlinx.android.synthetic.main.item_stop.view.stop_title

class RouteStopsAdapter : DiffAdapter<RouteFragment, Stop>() {

    init {
        useDiff = false
    }

    override fun getDiffCallback(oldData: List<Stop>, newData: List<Stop>): BaseDiffCallback<Stop> = StopDiffCallback(oldData, newData)

    override fun getItemViewId(position: Int, dataSource: List<Stop>): Int = R.layout.item_stop

    override fun instantiateViewHolder(view: View, viewType: Int): ViewHolder = ViewHolder(view)

    class ViewHolder(itemView: View) : BaseViewHolder<RouteFragment, Stop>(itemView) {

        @SuppressLint("SetTextI18n")
        override fun onBind(context: RouteFragment?, position: Int, dataSource: List<Stop>) {
            val stop = dataSource[position]
            val etaStatus = stop.etaStatus
            val etaResults = stop.etaResults

            itemView.stop_title.text = "${position + 1}. ${stop.name.value}"

            var additionInfo = ""
            if (stop.info.partial == 1L) {
                additionInfo = " (" + AppHelper.getString(R.string.stop_only_for_particular_time) + ")"
            }

            if (stop.fare > 0 && dataSource.size > (position + 1)) {
                if (stop.info.fareHoliday > 0.0 && stop.info.fareHoliday != stop.fare) {
                    additionInfo += " [" + AppHelper.getString(R.string.stop_fare_holiday) +
                            AppHelper.getString(R.string.price_2dp).format(stop.info.fareHoliday) + "]"
                }
                itemView.fare.text = AppHelper.getString(R.string.price_2dp).format(stop.fare)
            } else {
                itemView.fare.text = ""
            }

            itemView.stop_desc.text = stop.details.value + additionInfo

            // ETA Text Color
            var highlight = false
            if (position > 0) {
                val prevIsLoading = dataSource[position - 1].isLoading
                val prevEtaResults = dataSource[position - 1].etaResults
                val prevEtaTime = if (prevEtaResults.isNotEmpty()) prevEtaResults[0].etaTime else -1
                val currEtaTime = if (etaResults.isNotEmpty()) etaResults[0].etaTime else -1

                // TODO("Handle info.partial")

                if (!prevIsLoading && ( (currEtaTime/60L) in 1 until prevEtaTime/60L || (prevEtaTime < 0L && currEtaTime > 0L)))
                    highlight = true
            } else {
                if (etaResults.isNotEmpty() && etaResults[0].valid && etaResults[0].getDiffInMinutes() <= SharedPrefs.DEFAULT_HIGHLIGHT_B4_DEPARTURE)
                    highlight = true
            }

            val color = when (highlight) {
                true -> Utils.getThemeColorAccent(context!!.activity as Context)
                false -> ContextCompat.getColor(App.instance, R.color.colorWhite)
            }

            itemView.stop_title.setTextColor(color)
            itemView.eta_0.setTextColor(color)

            // ETA Result
            if (!stop.displayEta || etaResults.isEmpty()) {
                itemView.eta_0.text = AppHelper.getString(R.string.eta_msg_loading)
                itemView.eta_1.text = ""
                itemView.eta_2.text = ""
            } else {
                for (i in 0..2) {
                    val tv = when (i) {
                        0 -> itemView.eta_0
                        1 -> itemView.eta_1
                        2 -> itemView.eta_2
                        else -> null
                    }

                    tv?.text = ""

                    if (tv != null && i < etaResults.size) {
                        tv.text = etaResults[i].getFullTextMsg(tv, stop.isLoading, etaStatus)
                    }
                }
            }

            if (etaResults.size > 1) {
                itemView.eta_0.maxLines = 1
                itemView.eta_1.visibility = View.VISIBLE
                itemView.eta_2.visibility = View.VISIBLE
            } else {
                itemView.eta_0.maxLines = 2
                itemView.eta_1.visibility = View.GONE
                itemView.eta_2.visibility = View.GONE
            }

            if (context == null || !context.isGotoSeqUsed) {
                itemView.setOnTouchListener { v, event ->
                    context?.isGotoSeqUsed = true
                    false
                }
            }

            itemView.setOnClickListener { context?.updateEta(listOf(stop)) }
            itemView.setOnLongClickListener { context?.showStopPopupMenu(itemView, stop); true }
        }
    }
}
