package hoo.etahk.view.follow

import android.app.PendingIntent
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.model.relation.LocationAndGroups
import hoo.etahk.view.base.NavActivity
import hoo.etahk.view.dialog.InputDialog
import kotlinx.android.synthetic.main.activity_follow.*
import kotlinx.android.synthetic.main.activity_follow_nav.*
import kotlinx.android.synthetic.main.dialog_input.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch


class FollowActivity : NavActivity() {

    companion object {
        private const val TAG = "FollowActivity"
    }
    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private lateinit var spinnerAdapter: FollowSpinnerAdapter
    private lateinit var pagerAdapter: FollowPagerAdapter
    private lateinit var viewModel: FollowViewModel
    private var onTabSelectedListener: TabLayout.ViewPagerOnTabSelectedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_nav)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        viewModel = ViewModelProviders.of(this).get(FollowViewModel::class.java)
        viewModel.durationInMillis = Constants.SharePrefs.DEFAULT_ETA_AUTO_REFRESH * Constants.Time.ONE_SECOND_IN_MILLIS
        viewModel.enableSorting.value = false

        // Setup Spinner
        spinnerAdapter = FollowSpinnerAdapter(this)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val beforePosition = viewModel.selectedLocationPosition
                if (beforePosition != position) {
                    if (viewModel.keepSpinnerSelection) {
                        spinner.setSelection(viewModel.selectedLocationPosition)
                    } else {
                        viewModel.selectedLocationPosition = position
                        updateFragments(spinnerAdapter.dataSource[position])
                    }
                }
                viewModel.keepSpinnerSelection = false
            }

            override fun onNothingSelected(parent: AdapterView<out Adapter>?) {

            }
        }

        // Setup Fragment
        pagerAdapter = FollowPagerAdapter(supportFragmentManager)
        container.adapter = pagerAdapter
        tabs.setupWithViewPager(container)

        super.initNavigationDrawer()

        // Setup Progressbar
        progress_bar.max = viewModel.durationInMillis.toInt()
        progress_bar.progress = 0

        viewModel.initLocationsAndGroups()

        subscribeUiChanges()
    }

    override fun onResume() {
        nav.menu.findItem(R.id.nav_follow).isChecked = true
        super.onResume()
    }

    private fun subscribeUiChanges() {
        viewModel.getFollowLocations().observe(this, Observer<List<LocationAndGroups>> {
            it?.let {
                viewModel.keepSpinnerSelection = true
                spinnerAdapter.dataSource = it

                if (viewModel.selectedLocationPosition >= it.size) {
                    viewModel.selectedLocationPosition = 0
                } else {
                    viewModel.selectedLocationPosition = viewModel.selectedLocationPosition
                    container.offscreenPageLimit = it.size
                }

                if (it.isNotEmpty())
                    updateFragments(it[viewModel.selectedLocationPosition])
            }
        })

        viewModel.getMillisLeft().observe(this, Observer<Long> {
            it?.let {
                launch(UI){
                    progress_bar.progress = it.toInt()
                }
            }
        })
    }

    private fun updateFragments(locationAndGroups: LocationAndGroups) {
        onTabSelectedListener?.let { tabs.removeOnTabSelectedListener(it) }

        onTabSelectedListener = object : TabLayout.ViewPagerOnTabSelectedListener(container) {
            override fun onTabSelected(tab: TabLayout.Tab) {
                super.onTabSelected(tab)
                if (tabs.tabCount > 0)
                    locationAndGroups.selectedGroupPosition = tab.position
                Log.d(TAG, "S ${locationAndGroups.location.Id} ${locationAndGroups.selectedGroupPosition}")
            }
        }
        pagerAdapter.dataSource = locationAndGroups
        tabs.getTabAt(locationAndGroups.selectedGroupPosition)?.select()
        tabs.addOnTabSelectedListener(onTabSelectedListener!!)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_follow, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.menu_add_loaction -> {
                val inputDialog = InputDialog(this)
                inputDialog.setTitle(R.string.title_add_location)
                    .setHint(R.string.hint_location_name)
                    .setPositiveButton(listener = DialogInterface.OnClickListener {dialog, which ->
                        viewModel.insertLocation(inputDialog.view.input.text.toString())
                        Snackbar.make(container, R.string.msg_add_location_success, Snackbar.LENGTH_SHORT).show()
                    })
                    .show()
                true
            }
            R.id.menu_edit_location -> {
                val location = viewModel.getSelectedLocation().value!!

                val inputDialog = InputDialog(this)
                inputDialog.setTitle(R.string.title_rename_location)
                    .setHint(R.string.hint_new_location_name)
                    .setText(location.location.name)
                    .setPositiveButton(listener = DialogInterface.OnClickListener {dialog, which ->
                        location.location.name = inputDialog.view.input.text.toString()
                        viewModel.updateLocation(location.location)
                        Snackbar.make(container, R.string.msg_one_location_renamed, Snackbar.LENGTH_SHORT).show()

                    })
                    .show()
                true
            }
            R.id.menu_remove_loaction -> {
                val location = viewModel.getSelectedLocation().value!!

                AlertDialog.Builder(this)
                    .setTitle(R.string.title_conform_delete_location)
                    .setMessage(R.string.content_conform_delete)
                    .setPositiveButton(android.R.string.ok, { dialog, which ->
                        viewModel.deleteLocation(location.location)
                        Snackbar.make(container, R.string.msg_one_location_removed, Snackbar.LENGTH_SHORT).show()
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                true
            }
            R.id.menu_add_group -> {
                val inputDialog = InputDialog(this)
                inputDialog.setTitle(R.string.title_add_group)
                    .setHint(R.string.hint_group_name)
                    .setPositiveButton(listener = DialogInterface.OnClickListener {dialog, which ->
                        viewModel.insertGroup(inputDialog.view.input.text.toString())
                        Snackbar.make(container, R.string.msg_add_group_success, Snackbar.LENGTH_SHORT).show()
                    })
                    .show()
                true
            }
            R.id.menu_rename_group -> {
                val location = viewModel.getSelectedLocation().value!!

                if (!location.groups.isEmpty()) {
                    val group = location.groups[container.currentItem]

                    val inputDialog = InputDialog(this)
                    inputDialog.setTitle(R.string.title_rename_group)
                        .setHint(R.string.hint_new_group_name)
                        .setText(group.name)
                        .setPositiveButton(listener = DialogInterface.OnClickListener {dialog, which ->
                            group.name = inputDialog.view.input.text.toString()
                            viewModel.updateGroup(group)
                            Snackbar.make(container, R.string.msg_one_group_renamed, Snackbar.LENGTH_SHORT).show()

                        })
                        .show()
                }
                true
            }
            R.id.menu_remove_group -> {
                val location = viewModel.getSelectedLocation().value!!

                if (!location.groups.isEmpty()) {
                    val group = location.groups[container.currentItem]

                    AlertDialog.Builder(this)
                        .setTitle(R.string.title_conform_delete_group)
                        .setMessage(R.string.content_conform_delete)
                        .setPositiveButton(android.R.string.ok, { dialog, which ->
                            viewModel.deleteGroup(group)
                            Snackbar.make(
                                container,
                                R.string.msg_one_group_removed,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                }
                true
            }
            R.id.menu_sort_items -> {
                item.isChecked = !item.isChecked
                viewModel.enableSorting.value = item.isChecked
                true
            }
            R.id.menu_add_shortcut -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val shortcutManager = getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager

                    //check if device supports Pin Shortcut or not
                    if (shortcutManager.isRequestPinShortcutSupported) {
                        // Assumes there's already a shortcut with the ID "open_website".
                        // The shortcut must be enabled.
                        val pinShortcutInfo = ShortcutInfo.Builder(this, "follow").build()

                        // Create the PendingIntent object only if your app needs to be notified
                        // that the user allowed the shortcut to be pinned. Note that, if the
                        // pinning operation fails, your app isn't notified. We assume here that the
                        // app has implemented a method called createShortcutResultIntent() that
                        // returns a broadcast intent.
                        val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo)

                        // Configure the intent so that your app's broadcast receiver gets
                        // the callback successfully.
                        val successCallback = PendingIntent.getBroadcast(this, 0, pinnedShortcutCallbackIntent, 0)

                        //finally ask user to add the shortcut to home screen
                        shortcutManager.requestPinShortcut(
                            pinShortcutInfo,
                            successCallback.intentSender
                        )
                    }
                }
                true
            }
            R.id.menu_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
