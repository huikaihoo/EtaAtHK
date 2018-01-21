package hoo.etahk.view.search

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class BusSearchPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return BusSearchFragment.newInstance(BusSearchActivity.availableIndices[position])
    }

    override fun getCount(): Int {
        return BusSearchActivity.availableIndices.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return BusSearchActivity.searchList[position].title
    }
}
