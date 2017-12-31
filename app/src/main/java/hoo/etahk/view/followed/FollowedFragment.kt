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
import kotlinx.android.synthetic.main.fragment_followed.view.*

class FollowedFragment : Fragment() {

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
        fun newInstance(sectionNumber: Int): FollowedFragment {
            val fragment = FollowedFragment()
            val args = Bundle()
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            fragment.arguments = args
            return fragment
        }
    }

    private var mFollowedViewModel: FollowedViewModel? = null
    private var mFollowedAdapter: FollowedAdapter = FollowedAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mFollowedAdapter.context = this

        val rootView = inflater.inflate(R.layout.fragment_followed, container, false)

        rootView.recycler_view.layoutManager = LinearLayoutManager(activity)
        rootView.recycler_view.adapter = mFollowedAdapter

        mFollowedViewModel = ViewModelProviders.of(activity!!).get(FollowedViewModel::class.java)
        subscribeUiChanges()

        return rootView
    }

    fun updateEta(stops: List<Stop>) {
        mFollowedViewModel?.updateEta(stops)
    }

    private fun subscribeUiChanges() {
        mFollowedViewModel?.getFollowStops()?.observe(this, Observer<List<Stop>> {
            it?.let { mFollowedAdapter.dataSource = it }
        })
    }
}
