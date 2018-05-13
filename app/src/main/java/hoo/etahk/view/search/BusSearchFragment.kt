package hoo.etahk.view.search

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener
import hoo.etahk.R
import hoo.etahk.model.data.Route
import hoo.etahk.view.base.BaseFragment
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.fragment_recycler_fast_scroll.view.*

class BusSearchFragment : BaseFragment() {

    companion object {
        private const val TAG = "BusSearchFragment"
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
                DividerItemDecoration(activity,
                        (rootView.recycler_view.layoutManager as LinearLayoutManager).orientation))

        rootView.recycler_view.setPopupBgColor(BusSearchActivity.searchList[fragmentViewModel.index].color.colorPrimaryAccent)
        rootView.recycler_view.setThumbColor(BusSearchActivity.searchList[fragmentViewModel.index].color.colorPrimaryAccent)
        rootView.recycler_view.setStateChangeListener(object: OnFastScrollStateChangeListener{
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

    private fun subscribeUiChanges() {
        viewModel.searchText.observe(this, Observer<String> {
            busRoutesAdapter.filter = it?: ""
        })

        fragmentViewModel.getParentRoutes().observe(this, Observer<List<Route>> {
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
