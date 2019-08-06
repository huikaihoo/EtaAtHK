package hoo.etahk.view.follow

import android.annotation.SuppressLint
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.content.ContextCompat
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.constants.SharedPrefs
import hoo.etahk.common.extensions.prependImage
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.view.ItemTouchHelperAdapter
import hoo.etahk.model.diff.BaseDiffCallback
import hoo.etahk.model.diff.ItemDiffCallback
import hoo.etahk.model.relation.ItemAndStop
import hoo.etahk.view.App
import hoo.etahk.view.base.BaseViewHolder
import hoo.etahk.view.base.DiffAdapter
import kotlinx.android.synthetic.main.item_stop.view.eta_0
import kotlinx.android.synthetic.main.item_stop.view.eta_1
import kotlinx.android.synthetic.main.item_stop.view.eta_2
import kotlinx.android.synthetic.main.item_stop.view.fare
import kotlinx.android.synthetic.main.item_stop.view.stop_desc
import kotlinx.android.synthetic.main.item_stop.view.stop_title
import java.util.Collections

class FollowItemsAdapter : DiffAdapter<FollowFragment, ItemAndStop>(), ItemTouchHelperAdapter {

    init {
        useDiff = false
    }

    override fun getDiffCallback(oldData: List<ItemAndStop>, newData: List<ItemAndStop>): BaseDiffCallback<ItemAndStop> = ItemDiffCallback(oldData, newData)

    override fun getItemViewId(position: Int, dataSource: List<ItemAndStop>): Int = R.layout.item_stop

    override fun instantiateViewHolder(view: View, viewType: Int): ViewHolder = ViewHolder(view)

    class ViewHolder(itemView: View) : BaseViewHolder<FollowFragment, ItemAndStop>(itemView) {

        @SuppressLint("SetTextI18n")
        override fun onBind(context: FollowFragment?, position: Int, dataSource: List<ItemAndStop>) {
            val item = dataSource[position]

            itemView.stop_title.text = item.item.routeKey.getCompanyName() + " " + item.item.routeKey.routeNo
            itemView.stop_desc.text = "NOT EXIST"

            dataSource[position].stop?.let { stop ->
                val etaStatus = stop.etaStatus
                val etaResults = stop.etaResults

                itemView.stop_title.text = stop.routeKey.getCompanyName() + " " + stop.routeKey.routeNo
                itemView.stop_desc.text = stop.name.value + " " + AppHelper.getString(R.string.to_prefix) + stop.to.value
                if (stop.fare > 0) {
                    itemView.fare.text = AppHelper.getString(R.string.price_2dp).format(stop.fare)
                } else {
                    itemView.fare.text = ""
                }

                // ETA Text Color
                val color = when (etaResults.isNotEmpty() && etaResults[0].valid && etaResults[0].getDiffInMinutes() <= SharedPrefs.DEFAULT_HIGHLIGHT_B4_DEPARTURE) {
                    true -> ContextCompat.getColor(App.instance, R.color.colorDialogAccent)
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

                if (etaResults.size > 1) {
                    itemView.eta_0.maxLines = 1
                    itemView.eta_1.visibility = View.VISIBLE
                    itemView.eta_2.visibility = View.VISIBLE
                } else {
                    itemView.eta_0.maxLines = 2
                    itemView.eta_1.visibility = View.GONE
                    itemView.eta_2.visibility = View.GONE
                }

                itemView.setOnClickListener { context?.updateEta(listOf(item)) }
            }
            itemView.setOnLongClickListener { context?.showItemPopupMenu(itemView, item); true }
        }

    }

    /**
     * Called when an item has been dragged far enough to trigger a move. This is called every time
     * an item is shifted, and **not** at the end of a "drop" event.<br></br>
     * <br></br>
     * Implementations should call [RecyclerView.Adapter.notifyItemMoved] after
     * adjusting the underlying data to reflect this move.
     *
     * @param fromPosition The start position of the moved item.
     * @param toPosition   Then resolved position of the moved item.
     *
     * @see RecyclerView.getAdapterPositionFor
     * @see RecyclerView.ViewHolder.getAdapterPosition
     */
    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(dataSource, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(dataSource, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)

        context?.isItemsDisplaySeqChanged = true
        context?.updateItemsDisplaySeq(dataSource)
    }

    /**
     * Called when an item has been dismissed by a swipe.<br></br>
     * <br></br>
     * Implementations should call [RecyclerView.Adapter.notifyItemRemoved] after
     * adjusting the underlying data to reflect this removal.
     *
     * @param position The position of the item dismissed.
     *
     * @see RecyclerView.getAdapterPositionFor
     * @see RecyclerView.ViewHolder.getAdapterPosition
     */
    override fun onItemDismiss(position: Int) {
        val newDataSource = dataSource.toMutableList()
        newDataSource.removeAt(position)
        dataSource = newDataSource.toList()
        notifyItemRemoved(position)
    }
}
