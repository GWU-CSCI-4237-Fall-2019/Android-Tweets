package edu.gwu.androidtweetsfall2019

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import org.jetbrains.anko.doAsync

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private lateinit var confirm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        // Triggers the map to load, will call onMapReady when complete
        mapFragment.getMapAsync(this)

        confirm = findViewById(R.id.confirm)
        confirm.isEnabled = false
    }

    /**
     * Manipulates the map once available.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapLongClickListener { latLng ->
            mMap.clear()

            // Start running some code on the background for geocoding
            doAsync {
                // Retrieve address results from the Geocoder
                val geocoder = Geocoder(this@MapsActivity)
                val results: List<Address> = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5)

                // Switch back to UI thread to update the UI
                runOnUiThread {
                    if (results.isNotEmpty()) {
                        // We'll just display the 1st address, which would have the highest accuracy / confidence
                        val firstAddress = results[0]
                        val title = firstAddress.getAddressLine(0)

                        // Place a map marker
                        mMap.addMarker(
                            MarkerOptions().position(latLng).title(title)
                        )

                        // Pan the camera over to the map marker and zoom in
                        val zoomLevel = 12.0f
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel)
                        )

                        // Update button state
                        updateConfirmButton(firstAddress)

                        // Setup / override the onClickListener to send the user to the
                        // TweetsActivity with the new selected location data.
                        confirm.setOnClickListener {
                            val intent = Intent(this@MapsActivity, TweetsActivity::class.java)
                            intent.putExtra("latitude", latLng.latitude)
                            intent.putExtra("longitude", latLng.longitude)
                            intent.putExtra("address", title)
                            startActivity(intent)
                        }
                    } else {
                        Log.e("MapsActivity", "No results found")
                    }
                }
            }
        }
    }

    private fun updateConfirmButton(address: Address) {
        // Update the button color -- need to load the color from resources first
        val greenColor = ContextCompat.getColor(
            this, R.color.buttonGreen
        )
        val checkIcon = ContextCompat.getDrawable(
            this, R.drawable.ic_check_white
        )
        confirm.setBackgroundColor(greenColor)

        // Update the left-aligned icon
        confirm.setCompoundDrawablesWithIntrinsicBounds(checkIcon, null, null, null)

        //Update button text
        confirm.text = address.getAddressLine(0)
        confirm.isEnabled = true
    }
}
