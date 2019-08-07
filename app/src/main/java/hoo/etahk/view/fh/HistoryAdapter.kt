package hoo.etahk.view.fh

import android.annotation.SuppressLint
import android.view.View
import hoo.etahk.R
import hoo.etahk.common.Utils
import hoo.etahk.common.constants.Argument
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.diff.RouteHistoryDiffCallback
import hoo.etahk.model.relation.RouteHistoryEx
import hoo.etahk.view.base.BasePagedAdapter
import hoo.etahk.view.base.BaseViewHolder
import hoo.etahk.view.route.RouteActivity
import kotlinx.android.synthetic.main.item_route.view.from_to
import kotlinx.android.synthetic.main.item_route.view.route_company
import kotlinx.android.synthetic.main.item_route.view.route_no
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
            itemView.route_company.text = Utils.getStringResourceByName(history.company.toLowerCase())

            if (route == null) {
                itemView.from_to.text = "NOT EXIST"
            } else {
                itemView.from_to.text = route.from.value + route.getDirectionArrow() + route.to.value

                val routeKey = RouteKey(
                    company = history.company,
                    routeNo = route.routeKey.routeNo,
                    bound = route.routeKey.bound,
                    variant = route.routeKey.variant)

                itemView.setOnClickListener { startRouteActivity(context, routeKey, route.anotherCompany) }
                itemView.setOnLongClickListener { context?.showRoutePopupMenu(itemView, route, history); true }
            }
        }

        private fun startRouteActivity(context: FHActivity?, routeKey: RouteKey, anotherCompany: String = ""){
            context?.startActivity<RouteActivity>(
                Argument.ARG_COMPANY to routeKey.company,
                Argument.ARG_ROUTE_NO to routeKey.routeNo,
                Argument.ARG_TYPE_CODE to routeKey.typeCode,
                Argument.ARG_ANOTHER_COMPANY to anotherCompany,
                Argument.ARG_GOTO_BOUND to -1L,
                Argument.ARG_GOTO_SEQ to -1L
            )
        }
    }
}