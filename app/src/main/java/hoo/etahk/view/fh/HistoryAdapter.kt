package hoo.etahk.view.fh

import android.annotation.SuppressLint
import android.view.View
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.diff.RouteHistoryDiffCallback
import hoo.etahk.model.relation.RouteHistoryEx
import hoo.etahk.view.base.BasePagedAdapter
import hoo.etahk.view.base.BaseViewHolder
import hoo.etahk.view.route.RouteActivity
import kotlinx.android.synthetic.main.item_route.view.*
import org.jetbrains.anko.startActivity

class HistoryAdapter : BasePagedAdapter<FHActivity, RouteHistoryEx>(RouteHistoryDiffCallback()) {

    override fun getItemViewId(position: Int, dataSource: RouteHistoryEx?): Int = R.layout.item_route

    override fun instantiateViewHolder(view: View, viewType: Int): ViewHolder = ViewHolder(view)

    class ViewHolder(itemView: View) : BaseViewHolder<FHActivity, RouteHistoryEx>(itemView) {

        @SuppressLint("SetTextI18n")
        override fun onBind(context: FHActivity?, position: Int, dataSource: List<RouteHistoryEx>) {
            val route = if (dataSource.isEmpty()) null else dataSource[0].route
            val history = dataSource[0].history

            itemView.route_no.text = history.routeNo
            itemView.route_desc.text = Utils.getStringResourceByName(history.company.toLowerCase())

            if (route == null) {
                itemView.from_to.text = "NOT EXIST"
            } else {
                val directionArrow = Utils.getString(
                    when (route.direction) {
                        0L -> R.string.arrow_circular
                        1L -> R.string.arrow_one_way
                        else -> R.string.arrow_two_ways
                    })

                itemView.from_to.text = route.from.value + directionArrow + route.to.value

                val routeKey = RouteKey(
                    company = history.company,
                    routeNo = route.routeKey.routeNo,
                    bound = route.routeKey.bound,
                    variant = route.routeKey.variant)

                itemView.setOnClickListener { startRouteActivity(context, routeKey, route.anotherCompany) }
                //itemView.setOnLongClickListener { context?.showRoutePopupMenu(itemView, route); true }
            }
        }

        private fun startRouteActivity(context: FHActivity?, routeKey: RouteKey, anotherCompany: String = ""){
            context?.startActivity<RouteActivity>(
                Constants.Argument.ARG_COMPANY to routeKey.company,
                Constants.Argument.ARG_ROUTE_NO to routeKey.routeNo,
                Constants.Argument.ARG_TYPE_CODE to routeKey.typeCode,
                Constants.Argument.ARG_ANOTHER_COMPANY to anotherCompany,
                Constants.Argument.ARG_GOTO_BOUND to -1L,
                Constants.Argument.ARG_GOTO_SEQ to -1L
            )
        }
    }
}