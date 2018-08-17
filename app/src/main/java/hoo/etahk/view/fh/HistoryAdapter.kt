package hoo.etahk.view.fh

import android.annotation.SuppressLint
import android.view.View
import com.mcxiaoke.koi.ext.Bundle
import com.mcxiaoke.koi.ext.startActivity
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.diff.RouteHistoryDiffCallback
import hoo.etahk.model.relation.RouteHistoryEx
import hoo.etahk.view.App
import hoo.etahk.view.base.BasePagedAdapter
import hoo.etahk.view.base.BaseViewHolder
import hoo.etahk.view.route.RouteActivity
import kotlinx.android.synthetic.main.item_route.view.*

class HistoryAdapter : BasePagedAdapter<FHActivity, RouteHistoryEx>(RouteHistoryDiffCallback()) {

    override fun getItemViewId(position: Int, dataSource: RouteHistoryEx?): Int = R.layout.item_route

    override fun instantiateViewHolder(view: View?, viewType: Int): ViewHolder = ViewHolder(view)

    class ViewHolder(itemView: View?) : BaseViewHolder<FHActivity, RouteHistoryEx>(itemView) {

        @SuppressLint("SetTextI18n")
        override fun onBind(context: FHActivity?, position: Int, dataSource: List<RouteHistoryEx>) {
            val route = if (dataSource.isEmpty()) null else dataSource[0].route

            if (route == null) {
                val history = dataSource[position].history

                itemView.route_no.text = history.routeNo
                itemView.from_to.text = "NOT EXIST"
                itemView.route_desc.text = Utils.getStringResourceByName(history.company)
            } else {
                val directionArrow = App.instance.getString(
                    when (route.direction) {
                        0L -> R.string.arrow_circular
                        1L -> R.string.arrow_one_way
                        else -> R.string.arrow_two_ways
                    })

                itemView.route_no.text = route.routeKey.routeNo
                itemView.from_to.text = route.from.value + directionArrow + route.to.value
                itemView.route_desc.text = route.getParentDesc()

                itemView.setOnClickListener { startRouteActivity(context, route.routeKey) }
                //itemView.setOnLongClickListener { context?.showRoutePopupMenu(itemView, route); true }
            }
        }

        private fun startRouteActivity(context: FHActivity?, routeKey: RouteKey){
            context?.startActivity<RouteActivity>(Bundle {
                putString(Constants.Argument.ARG_COMPANY, routeKey.company)
                putString(Constants.Argument.ARG_ROUTE_NO, routeKey.routeNo)
                putLong(Constants.Argument.ARG_TYPE_CODE, routeKey.typeCode)
                putLong(Constants.Argument.ARG_GOTO_BOUND, -1L)
                putLong(Constants.Argument.ARG_GOTO_SEQ, -1L)
            })
        }
    }
}