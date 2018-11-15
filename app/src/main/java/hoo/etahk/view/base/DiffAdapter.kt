package hoo.etahk.view.base

import androidx.recyclerview.widget.DiffUtil
import hoo.etahk.model.diff.BaseDiffCallback
import kotlinx.coroutines.*

abstract class DiffAdapter<C, D> : BaseAdapter<C, D>() {

    var useDiff = true

    override var dataSource: List<D> = emptyList()
        set(value) {
            GlobalScope.launch(Dispatchers.Main) {
                if (useDiff)
                    dispatchUpdates(DiffUtil.calculateDiff(getDiffCallback(field, value)))
                field = value
                if (!useDiff)
                    notifyDataSetChanged()
            }
        }

    protected open fun dispatchUpdates(diffResult: DiffUtil.DiffResult) {
        diffResult.dispatchUpdatesTo(this)
    }

    // Need to override
    abstract fun getDiffCallback(oldData: List<D>, newData: List<D>): BaseDiffCallback<D>
}