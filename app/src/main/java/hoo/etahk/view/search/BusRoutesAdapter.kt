package hoo.etahk.view.search

import android.annotation.SuppressLint
import android.view.View
import com.mcxiaoke.koi.ext.Bundle
import com.mcxiaoke.koi.ext.startActivity
import hoo.etahk.R
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.view.App
import hoo.etahk.view.base.BaseAdapter
import hoo.etahk.view.base.BaseViewHolder
import hoo.etahk.view.route.RouteActivity
import kotlinx.android.synthetic.main.item_route.view.*

class BusRoutesAdapter : BaseAdapter<BusSearchFragment, Route, BusRoutesAdapter.ViewHolder>() {

    override fun getItemViewId(): Int = R.layout.item_route

    override fun instantiateViewHolder(view: View?): ViewHolder = ViewHolder(view)

    class ViewHolder(itemView: View?) : BaseViewHolder<BusSearchFragment, Route>(itemView) {

        @SuppressLint("SetTextI18n")
        override fun onBind(context: BusSearchFragment?, position: Int, dataSource: List<Route>) {
            val item = dataSource[position]
            val directionArrow = App.instance.getString(
                    when (item.direction) {
                        0L -> R.string.arrow_circular
                        1L -> R.string.arrow_one_way
                        else -> R.string.arrow_two_ways
                    })

            itemView.route_no.text = item.routeKey.routeNo
            itemView.from_to.text = item.from.value + directionArrow + item.to.value
            itemView.route_desc.text = item.getParentDesc()

            itemView.setOnClickListener { startRouteActivity(context, item.routeKey) }
        }

        private fun startRouteActivity(context: BusSearchFragment?, routeKey: RouteKey){
            context?.activity?.startActivity<RouteActivity>(Bundle {
                putString(RouteActivity.ARG_COMPANY, routeKey.company)
                putString(RouteActivity.ARG_ROUTE_NO, routeKey.routeNo)
            })
        }
    }
}
