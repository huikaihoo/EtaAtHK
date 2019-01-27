package hoo.etahk.view.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import hoo.etahk.R
import hoo.etahk.common.constants.Argument
import hoo.etahk.common.extensions.extras
import hoo.etahk.common.extensions.getExtra
import hoo.etahk.common.extensions.logd
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.relation.RouteAndStops
import hoo.etahk.view.base.MapsActivity
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.startActivity


class RoutesMapsActivity : MapsActivity(), OnMapReadyCallback {

    private lateinit var viewModel: RoutesMapViewModel
    private lateinit var spinnerAdapter: RoutesSpinnerAdapter
    private var spinner: Spinner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(RoutesMapViewModel::class.java)
        viewModel.routeKey = RouteKey(getExtra(Argument.ARG_COMPANY), getExtra(
            Argument.ARG_ROUTE_NO), -1L, -1L)

        extras.putLong(Argument.ARG_GOTO_BOUND, -1L)

        spinnerAdapter = RoutesSpinnerAdapter(this)

        supportActionBar?.title = viewModel.routeKey!!.routeNo
        supportActionBar?.subtitle = viewModel.routeKey!!.getCompanyName()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)

        // Set up OnInfoWindowClickListener
        googleMap.setOnInfoWindowClickListener { marker ->
            val textView = spinner?.selectedView?.findViewById(R.id.title) as TextView?
            val subtitle = viewModel.routeKey!!.getCompanyName() + " " +
                    viewModel.routeKey!!.routeNo + if (textView != null) " - " + textView.text else ""

            startActivity<StreetViewActivity>(
                Argument.ARG_ACTIONBAR_TITLE to marker.title,
                Argument.ARG_ACTIONBAR_SUBTITLE to subtitle,
                Argument.ARG_LATITUDE to marker.position.latitude,
                Argument.ARG_LONGITUDE to marker.position.longitude
            )
        }
        subscribeUiChanges()
    }

    private fun subscribeUiChanges() {
        viewModel.getRouteAndStopsList().observe(this, Observer<List<RouteAndStops>> {
            val isEmptyBefore = spinnerAdapter.dataSource.isEmpty()
            spinnerAdapter.dataSource = it
            spinner?.isEnabled = (it.size > 1)
            if (isEmptyBefore) {
                if (!viewModel.isGotoBoundUsed) {
                    val gotoBound = getExtra<Long>(Argument.ARG_GOTO_BOUND)
                    logd("gotoBound = $gotoBound")
                    if (gotoBound > 0) {
                        viewModel.isGotoBoundUsed = true
                        it.forEachWithIndex { i, routeAndStops ->
                            val routeKey = routeAndStops.route.routeKey
                            logd("routeKey = $routeKey")
                            if (routeKey.bound == gotoBound && routeKey.variant == 1L)
                                viewModel.selectedRoutePosition = i
                        }
                    }
                    logd("viewModel.selectedRoutePosition = ${viewModel.selectedRoutePosition}")
                }
                spinner?.setSelection(viewModel.selectedRoutePosition)
                if (viewModel.selectedRoutePosition < it.size)
                    showPathsAndStops(false)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_maps, menu)

        spinner = menu.findItem(R.id.menu_spinner).actionView as Spinner
        spinner!!.adapter = spinnerAdapter

        spinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val beforePosition = viewModel.selectedRoutePosition
                if (beforePosition != position) {
                    viewModel.selectedRoutePosition = position
                    showPathsAndStops(true)
                }
            }

            override fun onNothingSelected(parent: AdapterView<out Adapter>?) {

            }
        }

        return true
    }

    private fun showPathsAndStops(withAnimation: Boolean) {
        // Clear all markers on maps
        googleMap?.clear()

        val latLngBoundsBuilder = LatLngBounds.Builder()

        // Set paths on map
        val paths = spinnerAdapter.dataSource[viewModel.selectedRoutePosition].paths

        if (paths.isNotEmpty()) {
            val polylineOptions = PolylineOptions().color(getColor(R.color.colorLineRed))
            paths.forEachIndexed { i, path ->
                polylineOptions.add(path.location)
                latLngBoundsBuilder.include(path.location)
            }
            googleMap?.addPolyline(polylineOptions)
        }

        // Set stops on map
        val stops = spinnerAdapter.dataSource[viewModel.selectedRoutePosition].stops.sortedBy { it.seq }

        if (stops.isNotEmpty()) {
            stops.forEachIndexed { i, stop ->
                val title = (i + 1).toString() + ". " + stop.name.value
                val markerColor = when(i) {
                    0 -> BitmapDescriptorFactory.HUE_GREEN
                    stops.size - 1 -> BitmapDescriptorFactory.HUE_RED
                    else -> BitmapDescriptorFactory.HUE_AZURE
                }

                latLngBoundsBuilder.include(stop.location)

                val markerOptions = MarkerOptions().position(stop.location).title(title).icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                val marker = googleMap?.addMarker(markerOptions)

                if (i == 0) {
                    marker?.showInfoWindow()
                }
            }

            if (withAnimation)
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBoundsBuilder.build(), 50))
            else
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBoundsBuilder.build(), 50))

            // Check for location permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                // TODO("Zoom to nearest stop")
            }
        }
    }
}
