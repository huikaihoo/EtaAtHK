package hoo.etahk.view.follow

import android.annotation.SuppressLint
import android.text.SpannableStringBuilder
import android.view.View
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.model.diff.BaseDiffCallback
import hoo.etahk.model.diff.ItemDiffCallback
import hoo.etahk.model.relation.ItemAndStop
import hoo.etahk.view.App
import hoo.etahk.view.base.BaseDiffAdapter
import hoo.etahk.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_stop.view.*

class FollowItemsAdapter : BaseDiffAdapter<FollowFragment, ItemAndStop>() {

    init {
        useDiff = false
    }

    override fun getDiffCallback(oldData: List<ItemAndStop>, newData: List<ItemAndStop>): BaseDiffCallback<ItemAndStop> = ItemDiffCallback(oldData, newData)

    override fun getItemViewId(position: Int, dataSource: List<ItemAndStop>): Int = R.layout.item_stop

    override fun instantiateViewHolder(view: View?, viewType: Int): ViewHolder = ViewHolder(view)

    class ViewHolder(itemView: View?) : BaseViewHolder<FollowFragment, ItemAndStop>(itemView) {

        @SuppressLint("SetTextI18n")
        override fun onBind(context: FollowFragment?, position: Int, dataSource: List<ItemAndStop>) {
            val item = dataSource[position]

            itemView.stop_name.text = "..."

            dataSource[position].stop?.let { stop ->
                val etaStatus = stop.etaStatus
                val etaResults = stop.etaResults

                itemView.stop_name.text = stop.name.value
                itemView.stop_desc.text = stop.routeKey.getCompanyName() + " " + stop.routeKey.routeNo + " " + App.instance.getString(R.string.to) + stop.to.value
                if (stop.fare > 0) {
                    itemView.fare.text = App.instance.getString(R.string.price_2dp).format(stop.fare)
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
                            text = Utils.appendImageToTextView(tv, R.drawable.ic_text_loading, text)
                        } else if (etaStatus != Constants.EtaStatus.SUCCESS) {
                            text = Utils.appendImageToTextView(tv, R.drawable.ic_text_failed, text)
                        }
                        if (etaResults[i].wifi) {
                            text = Utils.appendImageToTextView(tv, R.drawable.ic_text_wifi, text)
                        }
                        text.append(etaResults[i].getDisplayMsg())
                        tv.text = text
                    }
                }

                itemView.setOnClickListener { context?.updateEta(listOf(item)) }
            }
        }

    }
}
