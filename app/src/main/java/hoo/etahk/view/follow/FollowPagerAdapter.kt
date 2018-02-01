package hoo.etahk.view.follow

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import hoo.etahk.model.relation.LocationAndGroups

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
        return dataSource!!.groups[position].name
    }
}