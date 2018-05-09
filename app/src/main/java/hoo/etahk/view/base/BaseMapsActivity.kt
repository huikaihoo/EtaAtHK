package hoo.etahk.view.base

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import hoo.etahk.R
import hoo.etahk.common.Constants.Permission.PERMISSIONS_REQUEST_LOCATION
import kotlinx.android.synthetic.main.activity_maps.*

abstract class BaseMapsActivity : TransparentActivity(), OnMapReadyCallback {

    companion object {
        private const val TAG = "BaseMapsActivity"
    }

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

        this.googleMap!!.setPadding(0, pendingTop, 0, pendingBottom)
        this.googleMap!!.uiSettings.isZoomControlsEnabled = true

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION )
                != PackageManager.PERMISSION_GRANTED) {
            // Request the permission.
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION ),
                    PERMISSIONS_REQUEST_LOCATION)
        } else {
            try {
                this.googleMap!!.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                Log.e(TAG, "setMyLocationEnabled failed!")
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
                        Log.e(TAG, "setMyLocationEnabled failed!")
                    }
                } else {
                    // permission denied
                }
                return
            }
        }
    }
}
