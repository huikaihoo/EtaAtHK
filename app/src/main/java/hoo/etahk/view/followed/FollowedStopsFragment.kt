package hoo.etahk.view.followed

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hoo.etahk.R
import hoo.etahk.model.data.Stop
import kotlinx.android.synthetic.main.fragment_followed_stops.view.*

class FollowedStopsFragment : Fragment() {

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(sectionNumber: Int): FollowedStopsFragment {
            val fragment = FollowedStopsFragment()
            val args = Bundle()
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            fragment.arguments = args
            return fragment
        }
    }

    private var mFollowedStopsViewModel: FollowedStopsViewModel? = null
    private var mFollowedStopsAdapter: FollowedStopsAdapter = FollowedStopsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mFollowedStopsAdapter.context = this

        val rootView = inflater.inflate(R.layout.fragment_followed_stops, container, false)

        rootView.recycler_view.layoutManager = LinearLayoutManager(activity)
        rootView.recycler_view.adapter = mFollowedStopsAdapter

        mFollowedStopsViewModel = ViewModelProviders.of(activity!!).get(FollowedStopsViewModel::class.java)
        subscribeUiChanges()

        return rootView
    }


    fun updateEta(stops: List<Stop>) {
        mFollowedStopsViewModel?.updateEta(stops)
    }

    private fun subscribeUiChanges() {
        mFollowedStopsViewModel?.getFollowStops()?.observe(this, Observer<List<Stop>> {
            it?.let { mFollowedStopsAdapter.dataSource = it }
        })
    }
}
