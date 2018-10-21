package hoo.etahk.view.base

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MapStyleOptions
import hoo.etahk.R
import hoo.etahk.common.Constants.Permission.PERMISSIONS_REQUEST_LOCATION
import hoo.etahk.common.extensions.loge
import kotlinx.android.synthetic.main.activity_maps.*

abstract class MapsActivity : TransparentActivity(), OnMapReadyCallback {

    protected var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Setup ActionBar
        setupActionBar()

        // Set up SwipeRefreshLayout
        refresh_layout.setProgressViewOffset(true,
                refresh_layout.progressViewStartOffset,
                refresh_layout.progressViewEndOffset + refresh_layout.progressViewStartOffset + pendingTop)
        refresh_layout.isEnabled = false
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
        googleMap.setPadding(0, pendingTop, 0, pendingBottom)
        
        googleMap.isIndoorEnabled = false
        googleMap.isBuildingsEnabled = true

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION )
                != PackageManager.PERMISSION_GRANTED) {
            // Request the permission.
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION ),
                    PERMISSIONS_REQUEST_LOCATION)
        } else {
            try {
                googleMap.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                loge("onMapReady::setMyLocationEnabled failed!", e)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted
                    try {
                        this.googleMap?.isMyLocationEnabled = true
                    } catch (e: SecurityException) {
                        loge("onRequestPermissionsResult::setMyLocationEnabled failed!", e)
                    }
                } else {
                    // permission denied
                }
                return
            }
        }
    }
}
