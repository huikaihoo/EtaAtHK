package hoo.etahk.view.follow

import android.annotation.SuppressLint
import android.text.SpannableStringBuilder
import android.view.View
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.prependImage
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.model.custom.NearbyStop
import hoo.etahk.model.diff.BaseDiffCallback
import hoo.etahk.model.diff.NearbyStopDiffCallback
import hoo.etahk.view.base.BaseViewHolder
import hoo.etahk.view.base.DiffAdapter
import kotlinx.android.synthetic.main.item_header.view.header_title
import kotlinx.android.synthetic.main.item_stop.view.eta_0
import kotlinx.android.synthetic.main.item_stop.view.eta_1
import kotlinx.android.synthetic.main.item_stop.view.eta_2
import kotlinx.android.synthetic.main.item_stop.view.fare
import kotlinx.android.synthetic.main.item_stop.view.stop_desc
import kotlinx.android.synthetic.main.item_stop.view.stop_title
import kotlinx.android.synthetic.main.item_stop_with_header.view.stop
import kotlin.math.roundToInt

class NearbyStopsAdapter : DiffAdapter<FollowFragment, NearbyStop>() {

    init {
        useDiff = false
    }

    override fun getDiffCallback(oldData: List<NearbyStop>, newData: List<NearbyStop>): BaseDiffCallback<NearbyStop> = NearbyStopDiffCallback(oldData, newData)

    override fun getItemViewId(position: Int, dataSource: List<NearbyStop>): Int = if (dataSource[position].showHeader) R.layout.item_stop_with_header else R.layout.item_stop

    override fun instantiateViewHolder(view: View, viewType: Int): ViewHolder = ViewHolder(view)

    class ViewHolder(itemView: View) : BaseViewHolder<FollowFragment, NearbyStop>(itemView) {

        @SuppressLint("SetTextI18n")
        override fun onBind(context: FollowFragment?, position: Int, dataSource: List<NearbyStop>) {
            val stop = dataSource[position].stop

            if (dataSource[position].showHeader) {
                var title = stop.name.value

                val lastLocation= context?.lastLocation
                if (lastLocation != null) {
                    val distance = lastLocation.distanceTo(stop.location).roundToInt()
                    title += " ($distance ${AppHelper.getQuantityString(R.plurals.meters, distance)})"
                }

                itemView.header_title.text = title
            }

            itemView.stop_title.text = stop.routeKey.getCompanyName() + " " + stop.routeKey.routeNo

            val etaStatus = stop.etaStatus
            val etaResults = stop.etaResults

            itemView.stop_title.text = stop.routeKey.getCompanyName() + " " + stop.routeKey.routeNo
            itemView.stop_desc.text = stop.name.value + " " + AppHelper.getString(R.string.to_prefix) + stop.to.value
            if (stop.fare > 0) {
                itemView.fare.text = AppHelper.getString(R.string.price_2dp).format(stop.fare)
            }

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
                        text = tv.prependImage(R.drawable.ic_text_loading, text)
                    } else if (etaStatus != Constants.EtaStatus.SUCCESS) {
                        text = tv.prependImage(R.drawable.ic_text_failed, text)
                    }
                    if (etaResults[i].valid && !etaResults[i].gps) {
                        text = tv.prependImage(R.drawable.ic_text_gps_off, text)
                    }
                    if (etaResults[i].wifi) {
                        text = tv.prependImage(R.drawable.ic_text_wheelchair, text)
                    }
                    if (etaResults[i].valid && etaResults[i].capacity >= 0L) {
                        text = tv.prependImage(Utils.getCapacityResId(etaResults[i].capacity), text)
                    }
                    text.append(etaResults[i].getDisplayMsg())
                    tv.text = text
                }
            }

            val stopView = if (dataSource[position].showHeader) itemView.stop else itemView
            stopView.setOnClickListener { context?.updateEtaByStops(listOf(stop)) }
            stopView.setOnLongClickListener { context?.showItemPopupMenu(stopView, stop); true }
        }
    }
}
