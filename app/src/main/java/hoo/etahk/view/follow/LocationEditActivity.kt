package hoo.etahk.view.follow

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Constants.Argument
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_route.*

class LocationEditActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var viewModel: LocationEditViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_edit)

        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(LocationEditViewModel::class.java)
        viewModel.locationId = intent.extras.getLong(Argument.ARG_LOCATION_ID)
        viewModel.name = intent.extras.getString(Argument.ARG_NAME) ?: ""
        viewModel.latitude = intent.extras.getDouble(Argument.ARG_LATITUDE)
        viewModel.longitude = intent.extras.getDouble(Argument.ARG_LOCATION_ID)
        logd("${viewModel.locationId} ${viewModel.name} ${viewModel.latitude} ${viewModel.longitude}")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Setup Actionbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(if (viewModel.locationId == null) R.string.title_add_location else R.string.title_rename_location)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.view?.isClickable = false
        mapFragment.getMapAsync(this)
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
                // Request the permission.
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

        val latlng = LatLng(viewModel.latitude!!, viewModel.longitude!!)
        val markerOptions = MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        googleMap?.addMarker(markerOptions)

        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15f))
    }
}
