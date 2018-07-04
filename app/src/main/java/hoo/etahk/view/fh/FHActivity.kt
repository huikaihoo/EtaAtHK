package hoo.etahk.view.fh

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import hoo.etahk.R
import hoo.etahk.view.base.NavActivity
import kotlinx.android.synthetic.main.activity_fh.*
import kotlinx.android.synthetic.main.activity_fh_nav.*

class FHActivity : NavActivity() {

    companion object {
        private const val TAG = "FHActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fh_nav)

        setSupportActionBar(toolbar)

        super.initNavigationDrawer()

        bnv.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onResume() {
        nav.menu.findItem(R.id.nav_fh).isChecked = true
        super.onResume()
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_favourite -> {
                    toolbar.title = getString(R.string.title_favourite)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.nav_history -> {
                    toolbar.title = getString(R.string.title_history)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }
}
