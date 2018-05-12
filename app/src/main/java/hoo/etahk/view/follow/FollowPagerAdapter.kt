package hoo.etahk.view.follow

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import hoo.etahk.R
import hoo.etahk.model.relation.LocationAndGroups
import hoo.etahk.view.App


/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class FollowPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {
    var dataSource: LocationAndGroups? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return FollowFragment.newInstance(position)
    }

    override fun getCount(): Int {
        return dataSource?.groups?.size?: 0
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return App.instance.getString(R.string.to_prefix) + dataSource!!.groups[position].name
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }
}