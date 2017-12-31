package hoo.etahk.view.base

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class BaseAdapter<C, D, VH : BaseViewHolder<C, D>> : RecyclerView.Adapter<VH>() {

    var context: C? = null

    var dataSource: List<D> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = dataSource.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent?.context)
        val view = inflater.inflate(getItemViewId(), parent, false)
        return instantiateViewHolder(view)
    }

    abstract fun getItemViewId() : Int

    abstract fun instantiateViewHolder(view: View?): VH

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(context, position, dataSource)
    }

    //fun getDataSource(position: Int) = dataSource[position]
}