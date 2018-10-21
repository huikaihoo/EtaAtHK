package hoo.etahk.view.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil

abstract class BasePagedAdapter<C, D>(diffCallback: DiffUtil.ItemCallback<D>) : PagedListAdapter<D, BaseViewHolder<C, D>>(diffCallback) {

    var context: C? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<C, D> {
        return instantiateViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false), viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return getItemViewId(position, getItem(position))
    }

    // Need to override
    protected abstract fun getItemViewId(position: Int, item: D?): Int

    // Need to override
    abstract fun instantiateViewHolder(view: View, viewType: Int): BaseViewHolder<C, D>

    override fun onBindViewHolder(holder: BaseViewHolder<C, D>, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.onBind(context, position, listOf(item))
        }
    }
}
