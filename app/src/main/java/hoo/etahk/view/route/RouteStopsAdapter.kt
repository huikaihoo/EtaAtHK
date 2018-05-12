package hoo.etahk.view.route

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import android.view.View
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.model.data.Stop
import hoo.etahk.model.diff.BaseDiffCallback
import hoo.etahk.model.diff.StopDiffCallback
import hoo.etahk.view.App
import hoo.etahk.view.base.BaseDiffAdapter
import hoo.etahk.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_stop.view.*

class RouteStopsAdapter : BaseDiffAdapter<RouteFragment, Stop>() {

    init {
        useDiff = false
    }

    override fun getDiffCallback(oldData: List<Stop>, newData: List<Stop>): BaseDiffCallback<Stop> = StopDiffCallback(oldData, newData)

    override fun getItemViewId(position: Int, dataSource: List<Stop>): Int = R.layout.item_stop

    override fun instantiateViewHolder(view: View?, viewType: Int): ViewHolder = ViewHolder(view)

    class ViewHolder(itemView: View?) : BaseViewHolder<RouteFragment, Stop>(itemView) {

        @SuppressLint("SetTextI18n")
        override fun onBind(context: RouteFragment?, position: Int, dataSource: List<Stop>) {
            val stop = dataSource[position]
            val etaStatus = stop.etaStatus
            val etaResults = stop.etaResults

            itemView.stop_title.text = "${position + 1}. ${stop.name.value}"
            itemView.stop_desc.text = stop.details.value
            if (stop.fare > 0 && dataSource.size > (position + 1)) {
                itemView.fare.text = App.instance.getString(R.string.price_2dp).format(stop.fare)
            } else {
                itemView.fare.text = ""
            }

            // ETA Text Color
            var highlight = false
            if (position > 0) {
                val prevIsLoading = dataSource[position - 1].isLoading
                val prevEtaResults = dataSource[position - 1].etaResults
                val prevEtaTime = if (prevEtaResults.isNotEmpty()) prevEtaResults[0].etaTime else -1
                val currEtaTime = if (etaResults.isNotEmpty()) etaResults[0].etaTime else -1

                if (!prevIsLoading && (currEtaTime in 1..(prevEtaTime - 1) || (prevEtaTime < 0L && currEtaTime > 0L)))
                    highlight = true
            } else {
                if (etaResults.isNotEmpty() && etaResults[0].valid && etaResults[0].getDiffInMinutes() <= Constants.SharePrefs.DEFAULT_HIGHLIGHT_B4_DEPARTURE)
                    highlight = true
            }

            val color = when (highlight) {
                true -> Utils.getThemeColorAccent(context!!.activity as Context)
                false -> ContextCompat.getColor(App.instance, R.color.colorWhite)
            }

            itemView.stop_title.setTextColor(color)
            itemView.eta_0.setTextColor(color)

            // ETA Result
            for (i in 0..2) {
                val tv = when (i) {
                    0 -> itemView.eta_0
                    1 -> itemView.eta_1
                    2 -> itemView.eta_2
                    else -> null
                }

                tv?.text = ""

                if (tv != null && i < etaResults.size) {
                    var text = SpannableStringBuilder()
                    if (stop.isLoading) {
                        text = Utils.appendImageToTextView(tv, R.drawable.ic_text_loading, text)
                    } else if (etaStatus != Constants.EtaStatus.SUCCESS) {
                        text = Utils.appendImageToTextView(tv, R.drawable.ic_text_failed, text)
                    }
                    if (etaResults[i].valid && !etaResults[i].gps) {
                        text = Utils.appendImageToTextView(tv, R.drawable.ic_text_gps_off, text)
                    }
                    if (etaResults[i].wifi) {
                        text = Utils.appendImageToTextView(tv, R.drawable.ic_text_wifi, text)
                    }
                    text.append(etaResults[i].getDisplayMsg())
                    tv.text = text
                }
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
