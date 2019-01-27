package hoo.etahk.view.map

import android.os.Bundle
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.LatLng
import hoo.etahk.R
import hoo.etahk.common.constants.Argument
import hoo.etahk.common.extensions.getExtra
import hoo.etahk.view.base.TransparentActivity

class StreetViewActivity : TransparentActivity(), OnStreetViewPanoramaReadyCallback {

    private var latLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_street_view)

        // Setup ActionBar
        setupActionBar()

        // Obtain the SupportStreetViewPanoramaFragment and get notified when street view is ready to be used.
        val supportStreetViewPanoramaFragment = supportFragmentManager
            .findFragmentById(R.id.street_view) as SupportStreetViewPanoramaFragment

        // Get Activity based data
        supportActionBar?.title = getExtra(Argument.ARG_ACTIONBAR_TITLE)
        supportActionBar?.subtitle = getExtra(Argument.ARG_ACTIONBAR_SUBTITLE)
        latLng = LatLng(
            intent.getDoubleExtra(Argument.ARG_LATITUDE, 0.0),
            intent.getDoubleExtra(Argument.ARG_LONGITUDE, 0.0)
        )

        supportStreetViewPanoramaFragment.getStreetViewPanoramaAsync(this)
    }

    override fun onStreetViewPanoramaReady(streetViewPanorama: StreetViewPanorama) {
        streetViewPanorama.setPosition(latLng)
    }
}
