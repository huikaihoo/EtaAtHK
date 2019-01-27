package hoo.etahk.view.route

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import getValue
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.extras
import hoo.etahk.common.extensions.getExtra
import hoo.etahk.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_timetable.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class TimetableActivity : BaseActivity() {

    private lateinit var viewModel: TimetableViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(RouteActivity.getTheme(getExtra(Constants.Argument.ARG_COMPANY), getExtra(Constants.Argument.ARG_TYPE_CODE)))
        window.navigationBarColor = Utils.getThemeColorPrimaryDark(this)
        super.setTaskDescription()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(TimetableViewModel::class.java)

        // Setup Actionbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getExtra<String>(Constants.Argument.ARG_COMPANY) + " " + getExtra(Constants.Argument.ARG_ROUTE_NO)

        scroll_view.isSmoothScrollingEnabled = false

        if (viewModel.route == null) {
            GlobalScope.launch(Dispatchers.Default) {
                viewModel.init(
                    getExtra(Constants.Argument.ARG_COMPANY),
                    getExtra(Constants.Argument.ARG_ROUTE_NO),
                    getExtra(Constants.Argument.ARG_GOTO_BOUND),
                    1L)

                GlobalScope.launch(Dispatchers.Main) {
                    showTimetable()
                }
            }
        } else {
            showTimetable()
        }
    }

    private fun showTimetable() {
        val directionArrow = getString(
            when (viewModel.route!!.direction) {
                0L -> R.string.arrow_circular
                else -> R.string.arrow_one_way
            })
        supportActionBar?.subtitle = viewModel.route!!.from.value + directionArrow + viewModel.route!!.to.value

        markwon_view.markdown = viewModel.content
    }
}