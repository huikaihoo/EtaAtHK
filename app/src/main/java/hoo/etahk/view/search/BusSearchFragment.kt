package hoo.etahk.view.search

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener
import hoo.etahk.R
import hoo.etahk.model.data.Route
import kotlinx.android.synthetic.main.activity_search_tab.*
import kotlinx.android.synthetic.main.fragment_recycler_fast_scroll.view.*

class BusSearchFragment : Fragment() {

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

    private lateinit var mRootView: View
    private lateinit var mBusSearchViewModel: BusSearchViewModel
    private lateinit var mBusSearchFragmentViewModel: BusSearchFragmentViewModel
    private var mBusRoutesAdapter: BusRoutesAdapter = BusRoutesAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mBusRoutesAdapter.context = this

        mBusSearchViewModel = ViewModelProviders.of(activity!!).get(BusSearchViewModel::class.java)
        mBusSearchFragmentViewModel = ViewModelProviders.of(this).get(BusSearchFragmentViewModel::class.java)

        mBusSearchFragmentViewModel.index = arguments!!.getInt(ARG_INDEX)

        mRootView = inflater.inflate(R.layout.fragment_recycler_fast_scroll, container, false)

        mRootView.recycler_view.layoutManager = LinearLayoutManager(activity)
        mRootView.recycler_view.itemAnimator = DefaultItemAnimator()
        mRootView.recycler_view.addItemDecoration(
                DividerItemDecoration(activity,
                        (mRootView.recycler_view.layoutManager as LinearLayoutManager).orientation))

        mRootView.recycler_view.setPopupBgColor(BusSearchActivity.searchList[mBusSearchFragmentViewModel.index].color.colorPrimaryAccent)
        mRootView.recycler_view.setThumbColor(BusSearchActivity.searchList[mBusSearchFragmentViewModel.index].color.colorPrimaryAccent)
        mRootView.recycler_view.setStateChangeListener(object: OnFastScrollStateChangeListener{
            override fun onFastScrollStop() {
                (activity as BusSearchActivity).fab.show()
            }
            override fun onFastScrollStart() {
                (activity as BusSearchActivity).fab.hide()
            }

        })

        mRootView.recycler_view.adapter = mBusRoutesAdapter

        mRootView.refresh_layout.isEnabled = false

        subscribeUiChanges()

        return mRootView
    }

    private fun subscribeUiChanges() {
        mBusSearchViewModel.searchText.observe(this, Observer<String> {
            mBusRoutesAdapter.filter = it?: ""
        })

        mBusSearchViewModel.isRefreshing.observe(this, Observer<Boolean> {
            if (it != null) {
                if (it && !mRootView.refresh_layout.isRefreshing) {
                    mRootView.refresh_layout.isEnabled = true
                    mRootView.refresh_layout.isRefreshing = true
                } else if (!it && mRootView.refresh_layout.isRefreshing) {
                    mRootView.refresh_layout.isRefreshing = false
                    mRootView.refresh_layout.isEnabled = false
                }
            }
        })

        mBusSearchFragmentViewModel.getParentRoutes().observe(this, Observer<List<Route>> {
            it?.let { mBusRoutesAdapter.dataSource = it }
        })
    }

    fun scrollToPosition(position: Int) {
        (mRootView.recycler_view.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
    }
}
