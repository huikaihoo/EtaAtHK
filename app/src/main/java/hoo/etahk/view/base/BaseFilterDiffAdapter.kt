package hoo.etahk.view.base

import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

abstract class BaseFilterDiffAdapter<C, D> : BaseDiffAdapter<C, D>() {

    var filter: String = ""
        set(value) {
            field = value
            publishResults(value, true)
        }

    override var dataSource: List<D> = emptyList()
        set(value) {
            field = value
            publishResults(filter, false)
        }

    private var dataSourceFiltered: List<D> = emptyList()

    private fun publishResults(constraint: String, scrollToTop: Boolean) {
        launch(UI) {
            val result = performFiltering(constraint)

            if (useDiff) {
                dispatchUpdates(DiffUtil.calculateDiff(getDiffCallback(dataSourceFiltered, result)))
                if (scrollToTop)
                    scrollToPosition(0)
            }

            dataSourceFiltered = result

            if (!useDiff)
                notifyDataSetChanged()
        }
    }

    // Need to override
    abstract fun performFiltering(constraint: String): List<D>

    // Need to override
    abstract fun scrollToPosition(position: Int)

    override fun getItemCount() = dataSourceFiltered.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder<C, D> {
        return instantiateViewHolder(LayoutInflater.from(parent?.context).inflate(viewType, parent, false), viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return getItemViewId(position, dataSourceFiltered)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<C, D>, position: Int) {
        holder.onBind(context, position, dataSourceFiltered)
    }
}