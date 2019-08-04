package hoo.etahk.view.search

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener
import hoo.etahk.R
import hoo.etahk.common.Utils
import hoo.etahk.common.constants.Argument
import hoo.etahk.common.constants.SharePrefs
import hoo.etahk.common.helper.SharedPrefsHelper
import hoo.etahk.model.data.Route
import hoo.etahk.view.base.BaseFragment
import hoo.etahk.view.route.RouteActivity
import kotlinx.android.synthetic.main.activity_search.fab
import kotlinx.android.synthetic.main.fragment_recycler_fast_scroll.view.recycler_view
import kotlinx.android.synthetic.main.fragment_recycler_fast_scroll.view.refresh_layout
import org.jetbrains.anko.startActivity

class BusSearchFragment : BaseFragment() {

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_INDEX = "index"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(index: Int): BusSearchFragment {
            val fragment = BusSearchFragment()
            val args = Bundle()
            args.putInt(ARG_INDEX, index)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var rootView: View
    private lateinit var viewModel: BusSearchViewModel
    private lateinit var fragmentViewModel: BusSearchFragmentViewModel
    private var busRoutesAdapter: BusRoutesAdapter = BusRoutesAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        busRoutesAdapter.context = this

        viewModel = ViewModelProviders.of(activity!!).get(BusSearchViewModel::class.java)
        fragmentViewModel = ViewModelProviders.of(this).get(BusSearchFragmentViewModel::class.java)

        fragmentViewModel.index = arguments!!.getInt(ARG_INDEX)

        rootView = inflater.inflate(R.layout.fragment_recycler_fast_scroll, container, false)

        rootView.recycler_view.layoutManager = LinearLayoutManager(activity)
        rootView.recycler_view.itemAnimator = DefaultItemAnimator()
        rootView.recycler_view.addItemDecoration(
            DividerItemDecoration(
                activity,
                (rootView.recycler_view.layoutManager as LinearLayoutManager).orientation
            )
        )

        rootView.recycler_view.setPopupBgColor(BusSearchActivity.searchList[fragmentViewModel.index].color.colorPrimaryAccent)
        rootView.recycler_view.setThumbColor(BusSearchActivity.searchList[fragmentViewModel.index].color.colorPrimaryAccent)
        rootView.recycler_view.setOnFastScrollStateChangeListener(object: OnFastScrollStateChangeListener{
            override fun onFastScrollStop() {
                (activity as BusSearchActivity).fab.show()
            }
            override fun onFastScrollStart() {
                (activity as BusSearchActivity).fab.hide()
            }
        })

        rootView.recycler_view.adapter = busRoutesAdapter

        rootView.refresh_layout.setColorSchemeColors(BusSearchActivity.searchList[fragmentViewModel.index].color.colorPrimary)
        rootView.refresh_layout.isRefreshing = busRoutesAdapter.dataSource.isEmpty()

        subscribeUiChanges()

        return rootView
    }

    fun showCompaniesPopupMenu(view: View, route: Route) {
        val popup = PopupMenu(activity!!, view, Gravity.END)

        popup.inflate(R.menu.popup_bus_jointly)

        popup.menu.findItem(R.id.popup_company_kmb_lwb).title = getString(
            R.string.view_company_route,
            Utils.getStringResourceByName(route.routeKey.company.toLowerCase()),
            route.routeKey.routeNo
        )
        popup.menu.findItem(R.id.popup_company_nwfb_ctb).title = getString(
            R.string.view_company_route,
            Utils.getStringResourceByName(route.anotherCompany.toLowerCase()),
            route.routeKey.routeNo
        )

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.popup_company_kmb_lwb -> {
                    activity?.startActivity<RouteActivity>(
                        Argument.ARG_COMPANY to route.routeKey.company,
                        Argument.ARG_ROUTE_NO to route.routeKey.routeNo,
                        Argument.ARG_TYPE_CODE to route.routeKey.typeCode,
                        Argument.ARG_ANOTHER_COMPANY to route.anotherCompany,
                        Argument.ARG_GOTO_BOUND to -1L,
                        Argument.ARG_GOTO_SEQ to -1L
                    )
                }
                R.id.popup_company_nwfb_ctb -> {
                    activity?.startActivity<RouteActivity>(
                        Argument.ARG_COMPANY to route.anotherCompany,
                        Argument.ARG_ROUTE_NO to route.routeKey.routeNo,
                        Argument.ARG_TYPE_CODE to route.routeKey.typeCode,
                        Argument.ARG_ANOTHER_COMPANY to route.routeKey.company,
                        Argument.ARG_GOTO_BOUND to -1L,
                        Argument.ARG_GOTO_SEQ to -1L
                    )
                }
            }
            true
        }
        popup.show()
    }

    fun showRoutePopupMenu(view: View, route: Route) {
        val pref = SharedPrefsHelper.get(R.string.pref_bus_jointly, SharePrefs.BUS_JOINTLY_ALWAYS_ASK)
        val companyDetailsByPref = route.companyDetailsByPref

        val popup = PopupMenu(activity!!, view, Gravity.END)

        popup.inflate(R.menu.popup_search)

        val viewItem = popup.menu.findItem(R.id.popup_view)
        if (route.companyDetails.size <= 1 || pref == SharePrefs.BUS_JOINTLY_ALWAYS_ASK) {
            viewItem.isVisible = false
        } else {
            viewItem.title = getString(
                R.string.view_company_route,
                Utils.getStringResourceByName(companyDetailsByPref[1].toLowerCase()),
                route.routeKey.routeNo
            )
        }

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.popup_view -> {
                    activity?.startActivity<RouteActivity>(
                        Argument.ARG_COMPANY to companyDetailsByPref[1],
                        Argument.ARG_ROUTE_NO to route.routeKey.routeNo,
                        Argument.ARG_TYPE_CODE to route.routeKey.typeCode,
                        Argument.ARG_ANOTHER_COMPANY to companyDetailsByPref[0],
                        Argument.ARG_GOTO_BOUND to -1L,
                        Argument.ARG_GOTO_SEQ to -1L
                    )
                }
                R.id.popup_add_fav -> {
                    fragmentViewModel.insertRouteFavourite(route.routeKey, route.anotherCompany)
                    Snackbar.make(view, R.string.msg_add_to_favourite_success, Snackbar.LENGTH_SHORT).show()
                }
            }
            true
        }
        popup.show()
    }

    private fun subscribeUiChanges() {
        viewModel.searchText.observe(viewLifecycleOwner, Observer<String> {
            busRoutesAdapter.filter = it?: ""
        })

        fragmentViewModel.getParentRoutes().observe(viewLifecycleOwner, Observer<List<Route>> {
            it?.let {
                if (it.isNotEmpty()) {
                    rootView.refresh_layout.isRefreshing = false
                    rootView.refresh_layout.isEnabled = false
                }
                busRoutesAdapter.dataSource = it
            }
        })
    }

    fun scrollToPosition(position: Int) {
        (rootView.recycler_view.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
    }
}
