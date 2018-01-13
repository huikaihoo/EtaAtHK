package hoo.etahk.view.base

import android.support.v7.util.DiffUtil
import hoo.etahk.model.diff.BaseDiffCallback
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

abstract class BaseDiffAdapter<C, D> : BaseAdapter<C, D>() {

    var useDiff = true

    override var dataSource: List<D> = emptyList()
        set(value) {
            launch(UI) {
                if (useDiff)
                    dispatchUpdates(DiffUtil.calculateDiff(getDiffCallback(field, value)))
                field = value
                if (!useDiff)
                    notifyDataSetChanged()
            }
        }

    open protected fun dispatchUpdates(diffResult: DiffUtil.DiffResult) {
        diffResult.dispatchUpdatesTo(this)
    }

    // Need to override
    abstract fun getDiffCallback(oldData: List<D>, newData: List<D>): BaseDiffCallback<D>
}