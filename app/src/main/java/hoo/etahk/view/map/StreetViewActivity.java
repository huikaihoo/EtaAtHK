package hoo.etahk.view.map;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import hoo.etahk.R;
import hoo.etahk.common.Constants.Argument;
import hoo.etahk.view.base.TransparentActivity;

/**
 * TODO("Convert to Kotlin")
 */
public class StreetViewActivity extends TransparentActivity implements OnStreetViewPanoramaReadyCallback {

    private static final String TAG = "StreetViewActivity";

    private String mTitle;
    private String mSubtitle;
    private LatLng mLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_view);

        // Setup ActionBar
        setupActionBar();

        // Obtain the SupportStreetViewPanoramaFragment and get notified when street view is ready to be used.
        SupportStreetViewPanoramaFragment supportStreetViewPanoramaFragment =
                (SupportStreetViewPanoramaFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.street_view);

        // Get Activity based data
        mTitle = getIntent().getStringExtra(Argument.ACTIONBAR_TITLE);
        mSubtitle = getIntent().getStringExtra(Argument.ACTIONBAR_SUBTITLE);
        mLatLng = new LatLng(getIntent().getDoubleExtra(Argument.LATITUDE, 0),
                getIntent().getDoubleExtra(Argument.LONGITUDE, 0));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mTitle);
        actionBar.setSubtitle(mSubtitle);

        supportStreetViewPanoramaFragment.getStreetViewPanoramaAsync(this);
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        streetViewPanorama.setPosition(mLatLng);
    }
}
