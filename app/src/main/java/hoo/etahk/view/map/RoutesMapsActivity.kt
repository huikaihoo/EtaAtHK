package hoo.etahk.view.map

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.support.v7.widget.ThemedSpinnerAdapter
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import hoo.etahk.R
import hoo.etahk.view.base.BaseMapsActivity
import kotlinx.android.synthetic.main.item_t_textview.view.*


class RoutesMapsActivity : BaseMapsActivity(), OnMapReadyCallback {

    companion object {
        private const val TAG = "RoutesMapsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment

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
        super.onMapReady(googleMap)

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        this.googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_maps, menu)

        val spinner = menu.findItem(R.id.menu_spinner).actionView as Spinner
        spinner.adapter = MyAdapter(this, arrayOf("Section 1", "Section 2", "Section 3"))
        return true
    }

    private class MyAdapter(context: Context, objects: Array<String>) : ArrayAdapter<String>(context, R.layout.item_t_textview, objects), ThemedSpinnerAdapter {
        private val mDropDownHelper: ThemedSpinnerAdapter.Helper = ThemedSpinnerAdapter.Helper(context)

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View

            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                val inflater = mDropDownHelper.dropDownViewInflater
                view = inflater.inflate(R.layout.item_t_textview, parent, false)
            } else {
                view = convertView
            }

            view.title.text = getItem(position)
            //view.subtitle.text = getItem(position)

            return view
        }

        override fun getDropDownViewTheme(): Resources.Theme? {
            return mDropDownHelper.dropDownViewTheme
        }

        override fun setDropDownViewTheme(theme: Resources.Theme?) {
            mDropDownHelper.dropDownViewTheme = theme
        }
    }
}
