package hoo.etahk.common.view

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper

/**
 * Source: https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf
 *
 * Interface to listen for a move or dismissal event from a [ItemTouchHelper.Callback].
 *
 * @author Paul Burke (ipaulpro)
 */
interface ItemTouchHelperAdapter {

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
    fun onItemMove(fromPosition: Int, toPosition: Int)


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
    fun onItemDismiss(position: Int)
}