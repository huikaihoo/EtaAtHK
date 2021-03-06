package hoo.etahk.view.follow

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.mcxiaoke.koi.ext.newIntent
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.constants.Argument
import hoo.etahk.common.constants.SharedPrefs
import hoo.etahk.common.extensions.createShortcut
import hoo.etahk.common.extensions.getExtra
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.extensions.restart
import hoo.etahk.common.extensions.startServiceCompat
import hoo.etahk.common.extensions.tag
import hoo.etahk.common.view.AlertDialogBuilder
import hoo.etahk.model.relation.LocationAndGroups
import hoo.etahk.view.base.NavActivity
import hoo.etahk.view.dialog.InputDialog
import hoo.etahk.view.service.UpdateRoutesService
import kotlinx.android.synthetic.main.activity_container_spinner_tabs_progress_fab.container
import kotlinx.android.synthetic.main.activity_container_spinner_tabs_progress_fab.fab
import kotlinx.android.synthetic.main.activity_container_spinner_tabs_progress_fab.progress_bar
import kotlinx.android.synthetic.main.activity_container_spinner_tabs_progress_fab.spinner
import kotlinx.android.synthetic.main.activity_container_spinner_tabs_progress_fab.tabs
import kotlinx.android.synthetic.main.activity_container_spinner_tabs_progress_fab.toolbar
import kotlinx.android.synthetic.main.activity_follow_nav.nav
import kotlinx.android.synthetic.main.dialog_input.view.input
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.find
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivityForResult


class FollowActivity : NavActivity() {

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
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var menu: Menu? = null
    private var onTabSelectedListener: TabLayout.ViewPagerOnTabSelectedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_nav)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        viewModel = ViewModelProvider(this).get(FollowViewModel::class.java)
        viewModel.durationInMillis = SharedPrefs.DEFAULT_ETA_AUTO_REFRESH * Constants.Time.ONE_SECOND_IN_MILLIS
        viewModel.isLocationIdUsed = getExtra(Argument.ARG_LOCATION_ID, -1L) == -1L

        if (viewModel.needUpdateParentRoute()) {
            this.startServiceCompat(newIntent<UpdateRoutesService>())
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Setup Spinner
        spinnerAdapter = FollowSpinnerAdapter(this)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (userIsInteracting) {
                    val before = viewModel.selectedLocation.value
                    val after = spinnerAdapter.dataSource[position]

                    if (before == null || before.location.Id != after.location.Id) {
                        viewModel.selectedLocation.value = after
                        updateFragments(after)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<out Adapter>?) {

            }
        }

        // Setup Fragment
        pagerAdapter = FollowPagerAdapter(supportFragmentManager)
        container.adapter = pagerAdapter
        tabs.setupWithViewPager(container)

        // Setup Fab
        fab.setOnClickListener {
            val index = if (spinner.selectedItemPosition == 0 && spinner.count > 1) 1 else 0
            GlobalScope.launch(Dispatchers.Main) {
                spinner.setSelection(index, true)
            }
        }

        super.initNavigationDrawer()

        // Setup Progressbar
        progress_bar.max = viewModel.durationInMillis.toInt()
        progress_bar.progress = 0

        viewModel.initLocationsAndGroups()

        if (!viewModel.isSnackbarShowed) {
            viewModel.isSnackbarShowed = true
            val msg = getExtra(Argument.ARG_SNACKBAR_MSG, "")
            if (msg.isNotBlank()) {
                Snackbar.make(container, msg, Snackbar.LENGTH_SHORT).show()
            }
        }

        if (SharedPrefs.acceptedTerms) {
            checkAndRequestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            showPolicyDialog()
        }
    }

    override fun onResume() {
        nav.menu.findItem(R.id.nav_follow).isChecked = true
        super.onResume()
    }

    override fun onRequestPermissionResult(isSuccess: Boolean, permission: String) {
        if (isSuccess && permission == Manifest.permission.ACCESS_FINE_LOCATION) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener {
                    viewModel.lastLocation = it
                    subscribeUiChanges()
                }
            } catch (e: SecurityException) {
                loge("onRequestPermissionResult::fusedLocationClient failed!", e)
                subscribeUiChanges()
            }
        }
    }

    private fun showPolicyDialog() {
        SharedPrefs.resetParams()

        val privacyPolicy = getString(R.string.html_link, getString(R.string.privacy_policy_url), getString(
            R.string.pref_title_privacy_policy))
        val disclaimer = getString(R.string.html_link, getString(R.string.disclaimer_url), getString(
            R.string.pref_title_disclaimer))

        val dialog = AlertDialogBuilder(this)
            .setMessage(
                HtmlCompat.fromHtml(getString(R.string.content_accept_terms, privacyPolicy, disclaimer),
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                ))
            .setPositiveButton(android.R.string.ok) { dialog, which ->
                SharedPrefs.acceptedTerms = true
                checkAndRequestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .setNegativeButton(R.string.exit_app) { dialog, which ->
                finishAffinity()
            }
            .setCancelable(false)
            .show()

        dialog.find<TextView>(android.R.id.message).movementMethod = LinkMovementMethod.getInstance()
    }

    private fun subscribeUiChanges() {
        logd("subscribeUiChanges")
//        if (!viewModel.getFollowLocations().hasActiveObservers()) {
            viewModel.getFollowLocations().observe(this, Observer<List<LocationAndGroups>> {
                val lastLocation = viewModel.lastLocation

                spinnerAdapter.dataSource = if (lastLocation == null) it else it.sortedBy {
                    if (it.location.pin) -1.0f else lastLocation.distanceTo(it.location.location)
                }

                val selectedLocation = viewModel.selectedLocation.value

                logd("B4 = ${selectedLocation?.location}")
                if (!viewModel.isLocationIdUsed) {
                    val target = getExtra(Argument.ARG_LOCATION_ID, -1L)
                    spinnerAdapter.dataSource.forEach {
                        if (it.location.Id == target) {
                            viewModel.isLocationIdUsed = true
                            viewModel.selectedLocation.value = it
                        }
                    }
                } else if ( selectedLocation == null ||
                    ( it.isNotEmpty() && it.none { it.location.Id == selectedLocation.location.Id } ) ) {
                    var selectIndex = 0
                    if (lastLocation != null &&
                        spinnerAdapter.dataSource.size > 1 &&
                        spinnerAdapter.dataSource[0].location.pin &&
                        lastLocation.distanceTo(spinnerAdapter.dataSource[1].location.location) < SharedPrefs.DEFAULT_SHOW_FOLLOW_LOCATION_DISTANCE) {
                        selectIndex = 1
                    }
                    viewModel.selectedLocation.value = spinnerAdapter.dataSource[selectIndex]
                }
                logd("AF = ${viewModel.selectedLocation.value?.location}")

                if (it.isNotEmpty()){
                    if ( spinner.selectedItemPosition < 0 ||
                        (spinner.selectedItemPosition in spinnerAdapter.dataSource.indices &&
                        spinnerAdapter.dataSource[spinner.selectedItemPosition].location.Id != viewModel.selectedLocation.value!!.location.Id) ) {
                        spinnerAdapter.dataSource.forEachIndexed { index, locationAndGroups ->
                            if (locationAndGroups.location.Id == viewModel.selectedLocation.value!!.location.Id) {
                                logd("setSelection = ${locationAndGroups.location.Id}; index=$index")
                                GlobalScope.launch(Dispatchers.Main) {
                                    spinner.setSelection(index, true)
                                }
                            }
                        }
                    }
                    logd("spinner.selectedItemPosition = ${spinner.selectedItemPosition}")
                    updateFragments(viewModel.selectedLocation.value)
                }
            })
//        }

        if (!viewModel.getMillisLeft().hasActiveObservers()) {
            viewModel.getMillisLeft().observe(this, Observer<Long> {
                GlobalScope.launch(Dispatchers.Main) {
                    progress_bar.progress = it.toInt()
                }
            })
        }
    }

    private fun updateOptionsMenu() {
        val menu = this.menu

        if (menu != null) {
            val visible = !viewModel.isNearbyStops
            menu.findItem(R.id.menu_edit_location)?.isVisible = visible
            menu.findItem(R.id.menu_remove_location)?.isVisible = visible
            menu.findItem(R.id.menu_group_options)?.isVisible = visible
            menu.findItem(R.id.menu_sort_items)?.isVisible = visible

            if (!visible && viewModel.enableSorting.value == true) {
                menu.findItem(R.id.menu_sort_items)?.isChecked = false
                viewModel.enableSorting.value = false
            }
        }
    }

    private fun updateFab() {
        if (viewModel.isNearbyStops) {
            fab.setImageResource(R.drawable.ic_fab_follow)
        } else {
            fab.setImageResource(R.drawable.ic_fab_nearby)
        }
        fab.show()
    }

    private fun updateFragments(locationAndGroups: LocationAndGroups?) {
        if (locationAndGroups != null) {
            viewModel.isNearbyStops = locationAndGroups.location.pin
            updateOptionsMenu()
            updateFab()

            onTabSelectedListener?.let { tabs.removeOnTabSelectedListener(it) }

            onTabSelectedListener = object : TabLayout.ViewPagerOnTabSelectedListener(container) {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    super.onTabSelected(tab)
                    if (tabs.tabCount > 0)
                        locationAndGroups.selectedGroupPosition = tab.position
                    logd("S ${locationAndGroups.location.Id} ${locationAndGroups.selectedGroupPosition}")
                }
            }
            pagerAdapter.dataSource = locationAndGroups
            tabs.getTabAt(locationAndGroups.selectedGroupPosition)?.select()
            tabs.addOnTabSelectedListener(onTabSelectedListener!!)
        }
    }

    private fun updateLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener {
                    logd("fusedLocationClient.lastLocation.addOnSuccessListener")
                    viewModel.lastLocation = it
                    val lastLocation = viewModel.lastLocation
                    val originalDataSource = spinnerAdapter.dataSource

                    if (lastLocation != null && originalDataSource.isNotEmpty()) {
                        spinnerAdapter.dataSource = originalDataSource.sortedBy {
                            if (it.location.pin) -1.0f else lastLocation.distanceTo(it.location.location)
                        }
                        var selectIndex = 0
                        if (spinnerAdapter.dataSource.size > 1 &&
                            spinnerAdapter.dataSource[0].location.pin &&
                            lastLocation.distanceTo(spinnerAdapter.dataSource[1].location.location) < SharedPrefs.DEFAULT_SHOW_FOLLOW_LOCATION_DISTANCE) {
                            selectIndex = 1
                        }
                        spinner.setSelection(selectIndex)
                        updateFragments(spinnerAdapter.dataSource[selectIndex])
                    }
                }
            } catch (e: SecurityException) {
                loge("updateLastLocation::fusedLocationClient failed!", e)
                viewModel.initLocationsAndGroups()
                subscribeUiChanges()
            }
        }
    }


    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Constants.Request.REQUEST_LOCATION_ADD -> {
                if (resultCode == RESULT_OK) {
                    Snackbar.make(container, R.string.msg_add_location_success, Snackbar.LENGTH_SHORT).show()
                }
            }
            Constants.Request.REQUEST_LOCATION_UPDATE -> {
                if (resultCode == RESULT_OK) {
                    Snackbar.make(container, R.string.msg_one_location_updated, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_follow, menu)

        this.menu = menu
        updateOptionsMenu()

        viewModel.enableSorting.value = menu.findItem(R.id.menu_sort_items).isChecked
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val location = viewModel.selectedLocation.value

        return when (item.itemId) {
            R.id.menu_get_my_location -> {
                updateLastLocation()
                true
            }
            R.id.menu_add_location -> {
                startActivityForResult<LocationEditActivity>(
                    Constants.Request.REQUEST_LOCATION_ADD,
                    Argument.ARG_NAME to ""
                )
                true
            }
            R.id.menu_edit_location -> {
                if (location?.location != null) {
                    startActivityForResult<LocationEditActivity>(
                        Constants.Request.REQUEST_LOCATION_UPDATE,
                        Argument.ARG_LOCATION_ID to location.location.Id,
                        Argument.ARG_NAME to location.location.name,
                        Argument.ARG_LATITUDE to location.location.latitude,
                        Argument.ARG_LONGITUDE to location.location.longitude
                    )
                }
                true
            }
            R.id.menu_remove_location -> {
                if (location != null) {
                    AlertDialogBuilder(this)
                        .setTitle(R.string.title_conform_delete_location)
                        .setMessage(R.string.content_conform_delete)
                        .setPositiveButton(android.R.string.ok) { dialog, which ->
                            viewModel.deleteLocation(location.location)
                            Snackbar.make(container, R.string.msg_one_location_removed, Snackbar.LENGTH_SHORT).show()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                }
                true
            }
            R.id.menu_add_group -> {
                val inputDialog = InputDialog(this)
                inputDialog.setTitle(R.string.title_add_group)
                    .setHint(R.string.hint_group_name)
                    .setPositiveButton(listener = DialogInterface.OnClickListener {dialog, which ->
                        viewModel.insertGroup(inputDialog.view.input.text.toString())
                        Snackbar.make(container, R.string.msg_add_group_success, Snackbar.LENGTH_SHORT).show()
                        restart(intentFor<FollowActivity>(
                            Argument.ARG_LOCATION_ID to location?.location?.Id,
                            Argument.ARG_SNACKBAR_MSG to getString(R.string.msg_add_group_success)
                        ))
                    })
                    .show()
                true
            }
            R.id.menu_rename_group -> {
                if (location != null && location.groups.isNotEmpty()) {
                    val group = location.groups[container.currentItem]

                    val inputDialog = InputDialog(this)
                    inputDialog.setTitle(R.string.title_rename_group)
                        .setHint(R.string.hint_new_group_name)
                        .setText(group.name)
                        .setPositiveButton(listener = DialogInterface.OnClickListener { dialog, which ->
                            group.name = inputDialog.view.input.text.toString()
                            viewModel.updateGroup(group)
                            Snackbar.make(container, R.string.msg_one_group_renamed, Snackbar.LENGTH_SHORT).show()
                        })
                        .show()
                }
                true
            }
            R.id.menu_remove_group -> {
                if (location != null && location.groups.isNotEmpty()) {
                    val group = location.groups[container.currentItem]

                    AlertDialogBuilder(this)
                        .setTitle(R.string.title_conform_delete_group)
                        .setMessage(R.string.content_conform_delete)
                        .setPositiveButton(android.R.string.ok) { dialog, which ->
                            viewModel.deleteGroup(group)
                            Snackbar.make(container, R.string.msg_one_group_removed, Snackbar.LENGTH_SHORT).show()
                            restart(intentFor<FollowActivity>(
                                Argument.ARG_LOCATION_ID to location.location.Id,
                                Argument.ARG_SNACKBAR_MSG to getString(R.string.msg_one_group_removed)
                            ))
                        }
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
                if (location == null) {
                    createShortcut(
                        tag(),
                        R.string.sc_follow_s,
                        R.string.sc_follow_l,
                        R.drawable.ic_shortcut_follow,
                        newIntent<FollowActivity>(0)
                    )
                } else {
                    createShortcut(
                        tag() + "_" + location.location.Id,
                        location.location.name,
                        if (location.location.pin) R.drawable.ic_shortcut_nearby else R.drawable.ic_shortcut_follow,
                        intentFor<FollowActivity>(
                            Argument.ARG_LOCATION_ID to location.location.Id
                        )
                    )
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
