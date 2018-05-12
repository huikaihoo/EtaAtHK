package hoo.etahk.common.view

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper


/**
 * Source: http://stackoverflow.com/questions/41142711/25-1-0-android-support-lib-is-breaking-fab-behavior
 */
class ItemTouchHelperCallback(private val _adapter: ItemTouchHelperAdapter,
                              var enableDrag: Boolean = true,
                              var enableSwipe: Boolean = true) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        return enableDrag
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return enableSwipe
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        _adapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        _adapter.onItemDismiss(viewHolder.adapterPosition)
    }

}