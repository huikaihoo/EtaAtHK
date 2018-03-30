package hoo.etahk.view.base

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

abstract class BaseAdapter<C, D> : RecyclerView.Adapter<BaseViewHolder<C, D>>() {

    var context: C? = null

    open var dataSource: List<D> = emptyList()
        set(value) {
            field = value
            launch(UI) {
                notifyDataSetChanged()
            }
        }

    override fun getItemCount() = dataSource.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<C, D> {
        return instantiateViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false), viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return getItemViewId(position, dataSource)
    }

    // Need to override
    protected abstract fun getItemViewId(position: Int, dataSource: List<D>): Int

    // Need to override
    abstract fun instantiateViewHolder(view: View?, viewType: Int): BaseViewHolder<C, D>

    override fun onBindViewHolder(holder: BaseViewHolder<C, D>, position: Int) {
        holder.onBind(context, position, dataSource)
    }
}