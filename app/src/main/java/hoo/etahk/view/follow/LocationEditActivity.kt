package hoo.etahk.view.follow

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.mcxiaoke.koi.ext.onClick
import com.mcxiaoke.koi.ext.onTextChange
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.constants.Argument
import hoo.etahk.common.extensions.getExtra
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_location_edit.*


class LocationEditActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var viewModel: LocationEditViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private var menuSave: MenuItem? = null

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_edit)

        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(LocationEditViewModel::class.java)
        if (!viewModel.isInit) {
            viewModel.isInit = true
            viewModel.locationId = getExtra(Argument.ARG_LOCATION_ID)
            viewModel.name = getExtra(Argument.ARG_NAME)
            viewModel.latitude = getExtra(Argument.ARG_LATITUDE, -1.0)
            viewModel.longitude = getExtra(Argument.ARG_LONGITUDE, -1.0)
            logd("${viewModel.locationId} ${viewModel.name} ${viewModel.latitude} ${viewModel.longitude}")

            if (viewModel.name.isNotBlank()) {
                viewModel.nameHistory.add(viewModel.name)
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Setup Actionbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(if (viewModel.locationId == null) R.string.title_add_location else R.string.title_edit_location)

        supportActionBar?.setHomeAsUpIndicator(R.drawable.abc_ic_clear_material)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        @Suppress("CAST_NEVER_SUCCEEDS")
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        @Suppress("CAST_NEVER_SUCCEEDS")
        (mapFragment as Fragment).view?.isClickable = false
        mapFragment.getMapAsync(this)

        input.onTextChange { text, start, before, count ->
            viewModel.name = text.toString()
            menuSave?.isEnabled = text.isNotBlank()
        }
        button.onClick {
            startActivityForResult(PlacePicker.IntentBuilder().build(this), Constants.Request.REQUEST_PLACE_PICKER)
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark))

        googleMap.isIndoorEnabled = false
        googleMap.isBuildingsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = false

        if (viewModel.latitude == null && viewModel.longitude == null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Constants.Permission.PERMISSIONS_REQUEST_LOCATION
                )
            } else {
                getCurrentLocation()
            }
        } else {
            applyLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            Constants.Permission.PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted
                    getCurrentLocation()
                } else {
                    // permission denied
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Constants.Request.REQUEST_PLACE_PICKER -> {
                if (resultCode == RESULT_OK) {
                    val place = PlacePicker.getPlace(this, data)
                    viewModel.name = place.name.toString()
                    viewModel.latitude = place.latLng.latitude
                    viewModel.longitude = place.latLng.longitude
                    applyLocation()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_location_edit, menu)

        menuSave = menu.findItem(R.id.menu_save)
        menuSave?.isEnabled = viewModel.name.isNotBlank()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.menu_save -> {
                if (viewModel.saveLocation()) {
                    setResult(RESULT_OK, intent)
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getCurrentLocation() {
        if (viewModel.latitude == null && viewModel.longitude == null) {
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
                        if (location != null) {
                            viewModel.latitude = location.latitude
                            viewModel.longitude = location.longitude
                            applyLocation()
                        }
                    }
            } catch (e: SecurityException) {
                loge("getCurrentLocation::fusedLocationClient failed!", e)
            }
        }
    }

    private fun applyLocation() {
        googleMap?.clear()

        val latitude = viewModel.latitude
        val longitude = viewModel.longitude
        if (latitude != null && longitude != null) {
            val latlng = LatLng(latitude, longitude)
            val markerOptions = MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            googleMap?.addMarker(markerOptions)
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15f))
        }

        input.setText(viewModel.name)

        if (viewModel.name.isNotBlank()) {
            viewModel.nameHistory.add(viewModel.name)
        }

        if (!viewModel.nameHistory.isEmpty()) {
            input.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, viewModel.nameHistory.toList()))
        }
    }
}
