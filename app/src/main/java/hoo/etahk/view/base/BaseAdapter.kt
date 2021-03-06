package hoo.etahk.view.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*

abstract class BaseAdapter<C, D> : RecyclerView.Adapter<BaseViewHolder<C, D>>() {

    var context: C? = null

    open var dataSource: List<D> = emptyList()
        set(value) {
            field = value
            GlobalScope.launch(Dispatchers.Main) {
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
    abstract fun instantiateViewHolder(view: View, viewType: Int): BaseViewHolder<C, D>

    override fun onBindViewHolder(holder: BaseViewHolder<C, D>, position: Int) {
        holder.onBind(context, position, dataSource)
    }
}