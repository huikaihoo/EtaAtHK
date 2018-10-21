package hoo.etahk.view.test

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import hoo.etahk.R
import hoo.etahk.common.helper.AppHelper
import kotlinx.android.synthetic.main.activity_t_bv.*

class BvActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_t_bv)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        message.setOnClickListener { view ->
            Snackbar.make(view, "Test Snackbar location" + AppHelper.db.stopDao().count(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }
}
