package hoo.etahk.view.route

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import hoo.etahk.R
import hoo.etahk.common.Utils
import hoo.etahk.common.constants.Argument
import hoo.etahk.common.extensions.getExtra
import hoo.etahk.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_timetable.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class TimetableActivity : BaseActivity() {

    private lateinit var viewModel: TimetableViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(RouteActivity.getTheme(getExtra(Argument.ARG_COMPANY), getExtra(
            Argument.ARG_TYPE_CODE)))
        window.navigationBarColor = Utils.getThemeColorPrimaryDark(this)
        super.setTaskDescription()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(TimetableViewModel::class.java)

        // Setup Actionbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getExtra<String>(Argument.ARG_COMPANY) + " " + getExtra(Argument.ARG_ROUTE_NO)

        scroll_view.isSmoothScrollingEnabled = false

        if (viewModel.route == null) {
            GlobalScope.launch(Dispatchers.Default) {
                viewModel.init(
                    getExtra(Argument.ARG_COMPANY),
                    getExtra(Argument.ARG_ROUTE_NO),
                    getExtra(Argument.ARG_GOTO_BOUND),
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
        val route = viewModel.route

        if (route != null) {
            supportActionBar?.title = route.routeKey.getCompanyName() + " " + route.routeKey.routeNo
            supportActionBar?.subtitle = route.from.value + route.getDirectionArrow() + route.to.value

            markwon_view.markdown = viewModel.content
        } else {
            markwon_view.markdown = getString(R.string.failed_to_load)
        }
    }
}