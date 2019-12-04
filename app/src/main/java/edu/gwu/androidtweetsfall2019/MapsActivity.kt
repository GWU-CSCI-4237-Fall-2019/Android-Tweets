package edu.gwu.androidtweetsfall2019

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.doAsync


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        val INTENT_KEY_ADDRESS = "address"
    }

    private lateinit var mMap: GoogleMap

    private lateinit var confirm: Button

    private lateinit var currentLocation: ImageButton

    private lateinit var locationProvider: FusedLocationProviderClient

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        val email = firebaseAuth.currentUser?.email
        title = "Welcome $email"

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        // Triggers the map to load, will call onMapReady when complete
        mapFragment.getMapAsync(this)

        confirm = findViewById(R.id.confirm)
        confirm.isEnabled = false

        currentLocation = findViewById(R.id.current_location)
        currentLocation.setOnClickListener {
            firebaseAnalytics.logEvent("current_location_clicked", null)
            checkPermissions()
        }

        locationProvider = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun checkPermissions() {
        // Check if the user has already granted the location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Yes, we have the location permission already
            firebaseAnalytics.logEvent("permission_location_already_granted", null)
            getCurrentLocation()
        } else {
            // No, we don't have the location permission -- prompt the user for it
            // We also have the option of calling shouldShowRequestPermissionRationale first to see
            // if we should show an extra justification for needing the permission to the user (e.g. via an AlertDialog)
            firebaseAnalytics.logEvent("permission_location_needed", null)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                200
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // Using the same code as was used with ActivityCompat.requestPermissions to verify this
        // permissions result is for the ACCESS_FINE_LOCATION prompt
        if (requestCode == 200) {
            // We only requested one permission, so it's result will be at the first index
            val locationResult = grantResults[0]
            if (locationResult == PackageManager.PERMISSION_GRANTED) {
                // The user clicked "Allow"
                firebaseAnalytics.logEvent("permission_location_granted", null)
                getCurrentLocation()
            } else {
                // The user clicked "Deny"

                // We want to call "shouldShowRequestPermissionRationale" to check if the user
                // denied permanently, but that function only exists on Android Marshmallow or higher.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // User is running a device with API 23 / Android 6.0 / Marshmallow or higher

                    if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // User denied permanently and would need to go to the Settings app to reverse the decision
                        firebaseAnalytics.logEvent("permission_location_denied_permanent", null)

                        // Here we show a Toast, but it is also possible to launch the Settings app directly
                        // to make it easier for the user to do this (would need to give them a heads-up first via an AlertDialog).
                        Toast.makeText(
                            this,
                            "Please go into your Settings and enable Location for Android Tweets",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // Else, the user hit "Deny", but it's not permanent, so we're free to re-prompt next time
                        // No action needed
                        firebaseAnalytics.logEvent("permission_location_denied", null)
                    }
                } else {
                    // Else the device is running an Android version below Marshmallow (where the location permission
                    // would be granted at installation), so there shouldn't be a valid scenario that hits this else-block.
                }
            }
        }
    }

    private fun getCurrentLocation() {
        // We could also use .lastLocation as shown during Lecture, which is easy-to-use, but
        // is possible for it to be inaccurate.
        // requestLocationUpdates allows us to get a "fresh' location.
        locationProvider.requestLocationUpdates(
            LocationRequest.create(),
            locationCallback,
            null
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            firebaseAnalytics.logEvent("location_retrieved", null)

            // We only need one location update, so we can stop listening for updates now.
            // Otherwise, this function would be called repeatedly with new updates.
            locationProvider.removeLocationUpdates(this)

            // Get most recent result (index 0)
            val location = locationResult.locations[0]
            val latLng = LatLng(location.latitude, location.longitude)

            doGeocoding(latLng)
        }
    }

    private fun doGeocoding(latLng: LatLng) {
        mMap.clear()

        // Start running some code on the background for geocoding
        doAsync {
            // Retrieve address results from the Geocoder
            val geocoder = Geocoder(this@MapsActivity)
            val results: List<Address> = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5)

            // Switch back to UI thread to update the UI
            runOnUiThread {
                if (results.isNotEmpty()) {
                    firebaseAnalytics.logEvent("location_geocoded", null)

                    // We'll just display the 1st address, which would have the highest accuracy / confidence
                    val firstAddress = results[0]
                    val title = firstAddress.getAddressLine(0)
                    val state = firstAddress.adminArea ?: "unknown"

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
                        intent.putExtra(INTENT_KEY_ADDRESS, state)
                        intent.putExtra("latitude", latLng.latitude)
                        intent.putExtra("longitude", latLng.longitude)
                        intent.putExtra("address", title)
                        startActivity(intent)
                    }
                } else {
                    Log.e("MapsActivity", "No results found")
                    firebaseAnalytics.logEvent("location_no_geocode_results", null)
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapLongClickListener { latLng ->
            doGeocoding(latLng)
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
