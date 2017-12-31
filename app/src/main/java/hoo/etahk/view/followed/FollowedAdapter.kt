package hoo.etahk.view.followed

import android.support.design.widget.Snackbar
import android.view.View
import hoo.etahk.R
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.model.data.Stop
import hoo.etahk.view.base.BaseAdapter
import hoo.etahk.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_stop.view.*

class FollowedAdapter : BaseAdapter<FollowedFragment, Stop, FollowedAdapter.ViewHolder>() {

    override fun getItemViewId(): Int = R.layout.item_stop

    override fun instantiateViewHolder(view: View?): ViewHolder = ViewHolder(view)

    class ViewHolder(itemView: View?) : BaseViewHolder<FollowedFragment, Stop>(itemView) {

        //val tvName by lazy { itemView?.findViewById<TextView?>(R.id.tvName) }
        //val tvDescription by lazy { itemView?.findViewById<TextView?>(R.id.tvDescription) }

        override fun onBind(context: FollowedFragment?, position: Int, dataSource: List<Stop>) {
            val item = dataSource[position]
            val etaResults = item.etaResults
            itemView.stop_name.text = item.seq.toString()

            for (i in 0..2) {
                val tv = when (i) {
                    0 -> itemView.eta_0
                    1 -> itemView.eta_1
                    2 -> itemView.eta_2
                    else -> null
                }
                val msg = when (i < etaResults.size) {
                    true -> etaResults[i].getDisplayMsg()
                    false -> ""
                }
                tv?.text = msg
            }
            itemView.stop_desc.text = AppHelper.gson.toJson(item.etaResults)

            itemView.setOnClickListener { view ->
                context?.updateEta(listOf(item))
                Snackbar.make(view, "Item ${item.seq} Clicked", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
            }
        }

    }
}

//class FollowedAdapter(val items : List<Stop>, val itemClickListener: (Stop)->Unit) : RecyclerView.Adapter<FollowedAdapter.ViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stop, parent, false)
//        return ViewHolder(view, itemClickListener)
//    }
//
//    override fun getItemCount(): Int = items.size
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bind(items[position])
//    }
//
//    class ViewHolder(val view: View, val itemClickListener: (Stop) -> Unit) : RecyclerView.ViewHolder(view) {
//        fun bind(news: Stop) {
//            view.stop_name.text =
//            view.x = news.desc
//            view.desc.text = news.type
//            view.setOnClickListener {
//                itemClickListener(news)
//            }
//        }
//    }
//}
