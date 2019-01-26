package hoo.etahk.view.route

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.view.App
import hoo.etahk.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_timetable.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class TimetableActivity : BaseActivity() {

    private lateinit var viewModel: TimetableViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(RouteActivity.getTheme(intent.extras.getString(Constants.Argument.ARG_COMPANY), intent.extras.getLong(Constants.Argument.ARG_TYPE_CODE)))
        window.navigationBarColor = Utils.getThemeColorPrimaryDark(this)
        super.setTaskDescription()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(TimetableViewModel::class.java)

        // Setup Actionbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.extras?.getString(Constants.Argument.ARG_COMPANY).orEmpty() + " " + intent.extras?.getString(Constants.Argument.ARG_ROUTE_NO).orEmpty()

        scroll_view.isSmoothScrollingEnabled = false

        if (viewModel.route == null) {
            GlobalScope.launch(Dispatchers.Default) {
                viewModel.init(
                    intent.extras?.getString(Constants.Argument.ARG_COMPANY).orEmpty(),
                    intent.extras?.getString(Constants.Argument.ARG_ROUTE_NO).orEmpty(),
                    intent.extras.getLong(Constants.Argument.ARG_GOTO_BOUND),
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
        val directionArrow = App.instance.getString(
            when (viewModel.route!!.direction) {
                0L -> R.string.arrow_circular
                else -> R.string.arrow_one_way
            })
        supportActionBar?.subtitle = viewModel.route!!.from.value + directionArrow + viewModel.route!!.to.value

        markwon_view.markdown = viewModel.content
    }
}