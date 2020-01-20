package com.systematic.netcore.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.systematic.netcore.R
import com.systematic.netcore.utility.Constants
import kotlinx.android.synthetic.main.activity_map_choice.*


class MapChoice : AppCompatActivity(), OnMapReadyCallback {
    //private lateinit var mMap : GoogleMap
    private lateinit var mMap : GoogleMap
    private var LOCATION_PERMISSION_REQUEST_CODE = 1

    public var pickupLat : Double = 0.0
    public var pickupLng : Double = 0.0
    private var gpsLat: Double = 0.0
    private var gpsLng: Double = 0.0
    private var cellLat: Double = 0.0
    private var cellLng: Double = 0.0

    val LOCATION_UPDATE_MIN_DISTANCE = 10
    val LOCATION_UPDATE_MIN_TIME = 500

    private var mLocationManager: LocationManager? = null

    var is_edit = ""

    private val mLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            if (location != null) {
                //Logger.d(String.format("%f, %f", location.getLatitude(), location.getLongitude()));
                //drawMarker(location)
                val gps = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 21f))
                mLocationManager?.removeUpdates(this)

                if (location.provider == LocationManager.GPS_PROVIDER) {//cell location
                    cellLat = location!!.latitude
                    cellLng = location!!.longitude
                }
                if (location.provider == LocationManager.NETWORK_PROVIDER) {//gps location
                    gpsLat = location!!.latitude
                    gpsLng = location!!.longitude
                }
                Log.i("cellLat", cellLat.toString())
                Log.i("gpsLat", gpsLat.toString())
            } else {
                //Logger.d("Location is null");
            }
        }

        override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {

        }

        override fun onProviderEnabled(s: String) {

        }

        override fun onProviderDisabled(s: String) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_choice)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = (supportFragmentManager.findFragmentById(R.id.ShowNearestLocationMapViewMapFrag) as SupportMapFragment)
        mapFragment.getMapAsync(this)

        //mapChoice.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(16.799999, 96.150002), 5f)) // move map to Myanmar location

//        var mapFragment : SupportMapFragment?=null
//        mapFragment = fragmentManager.findFragmentById(R.id.mapChoice) as SupportMapFragment?

        if (intent.getStringExtra(Constants.key_is_edit) != null) {
            Log.i("is_edit", "not_null")
            is_edit = intent.getStringExtra(Constants.key_is_edit)
            if (is_edit == "") {
                Log.i("is_edit", "blank")
                mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            }
        }
        else {
            Log.i("is_edit", "null")
            mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        //getCurrentLocation()

        llBtnGetLoc.setOnClickListener {
            pickupLat = mMap.cameraPosition.target.latitude
            pickupLng = mMap.cameraPosition.target.longitude

            if(pickupLat > 0 && pickupLng > 0)
                onBackPressed()
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
        mMap = googleMap
        setUpMap()


//        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun setUpMap() {
        mMap.uiSettings.isZoomControlsEnabled = true

        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        mMap.isMyLocationEnabled = true

        var lm = getSystemService(LOCATION_SERVICE) as LocationManager

        //Get the best provider >>
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_COARSE
        criteria.isCostAllowed = false
        val provider = lm.getBestProvider(criteria, false)
        // <<
        
//        var provider = LocationManager.GPS_PROVIDER
        var location = lm.getLastKnownLocation(provider)
        if (location != null) {
            // add location to the location listener for location changes
            //var test = "Prov : " + location!!.provider + " Lat : " + location!!.latitude + " Lon : " + location!!.longitude

            var currentLatitude = 0.0
            var currentLongitude = 0.0
            var latLng = LatLng(currentLatitude, currentLongitude)

            if (intent.getStringExtra(Constants.key_is_edit) != null) {
                is_edit = intent.getStringExtra(Constants.key_is_edit)
                if (is_edit == "yes") {
                    Log.i("currentLatitude", intent.getDoubleExtra(Constants.key_latitude, 0.0).toString())
                    Log.i("currentLongitude", intent.getDoubleExtra(Constants.key_longitude, 0.0).toString())
                    currentLatitude = intent.getDoubleExtra(Constants.key_latitude, 0.0)
                    currentLongitude = intent.getDoubleExtra(Constants.key_longitude, 0.0)
                    gpsLat = intent.getDoubleExtra("gpsLat", 0.0)
                    gpsLng = intent.getDoubleExtra("gpsLng", 0.0)
                    cellLat = intent.getDoubleExtra("cellLat", 0.0)
                    cellLng = intent.getDoubleExtra("cellLng", 0.0)
                    latLng = LatLng(currentLatitude, currentLongitude)
                    mMap.addMarker(MarkerOptions().position(latLng).title("Pickup Location"))
                }
                else {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    latLng = LatLng(currentLatitude, currentLongitude)
                }
            }

            //val latLng = LatLng(currentLatitude, currentLongitude)
            //mMap.addMarker(MarkerOptions().position(latLng).title("Pickup Location"))

            val zoomLevel = 21.0f //This goes up to 21
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
        }

        getCurrentLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val isGPSEnabled = mLocationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = mLocationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        var location: Location? = null
        if (!(isGPSEnabled == true || isNetworkEnabled == true)) {
            //Snackbar.make(mMapView, R.string.error_location_provider, Snackbar.LENGTH_INDEFINITE).show();
        } else {
            if (isNetworkEnabled == true) {
                mLocationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME.toLong(), LOCATION_UPDATE_MIN_DISTANCE.toFloat(), mLocationListener)
                location = mLocationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }

            if (isGPSEnabled == true) {
                mLocationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME.toLong(), LOCATION_UPDATE_MIN_DISTANCE.toFloat(), mLocationListener)
                location = mLocationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
        }
        if (location != null) {
            /*Logger.d(String.format("getCurrentLocation(%f, %f)", location.getLatitude(),
                    location.getLongitude()));*/
            //drawMarker(location)
            val gps = LatLng(location.latitude, location.longitude)
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 21f))
        }
    }

    private fun drawMarker(location: Location) {
        if (mMap != null) {
            mMap.clear()

            val gps = LatLng(location.latitude, location.longitude)
            mMap.addMarker(MarkerOptions()
                    .position(gps)
                    .title("Current Position"))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 21f))
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        // if you want to send any type of data back, such as an object, string, int etc
        // you have to create an intent and use this bit of code
        // otherwise you can just use setResult(Activity.RESULT_OK) and finish()
        val intent = Intent()
        intent.putExtra("pickupLat", pickupLat)
        intent.putExtra("pickupLng", pickupLng)
        intent.putExtra("gpsLat", gpsLat)
        intent.putExtra("gpsLng", gpsLng)
        intent.putExtra("cellLat", cellLat)
        intent.putExtra("cellLng", cellLng)
        intent.putExtra(Constants.key_selected_date, intent.getStringExtra(Constants.key_selected_date))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
