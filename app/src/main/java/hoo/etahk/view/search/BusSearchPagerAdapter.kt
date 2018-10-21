package hoo.etahk.view.search

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

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
