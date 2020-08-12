package com.albertbonet.testlibrary.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.albertbonet.testlibrary.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

class GPSManager(var gpsInterface: GPSInterface) : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    companion object {
        const val MIN_UPDATE_DISTANCE = 1000 //1km
    }

    var mGoogleApiClient: GoogleApiClient? = null
    var lat: Double = 0.0
    var lon: Double = 0.0
    private var lastValidLocation: Location? = null
    private var mLocation: Location? = null
    private var mLocationManager: android.location.LocationManager? = null

    private var mLocationRequest: LocationRequest? = null
    private val listener: com.google.android.gms.location.LocationListener? = null
    private val UPDATE_INTERVAL = (7000).toLong()  /* 7 secs */
    private val FASTEST_INTERVAL: Long = 5000 /* 5 sec */

    private var locationManager: android.location.LocationManager? = null
    private val isLocationEnabled: Boolean
        get() {
            locationManager = (this.gpsInterface as Activity).getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
            return locationManager!!.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
        }
    init {
        mGoogleApiClient = GoogleApiClient.Builder((this.gpsInterface as Activity))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        mLocationManager = (this.gpsInterface as Activity).getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        checkLocation()
        /*if(checkLocation()) {
            //check whether location service is enable or not in your  phone
            if (mGoogleApiClient != null) {
                mGoogleApiClient!!.connect()
            }
        }*/
    }

    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {
        try {
            if (ActivityCompat.checkSelfPermission((this.gpsInterface as Activity), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission((this.gpsInterface as Activity), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((this.gpsInterface as Activity), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 225)
                return
            }
            startLocationUpdates()
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
            if (mLocation == null) {
                startLocationUpdates()
            }
            if (mLocation != null) {
                lat = mLocation!!.latitude
                lon = mLocation!!.longitude
            } else {
                Toast.makeText(this.gpsInterface as Activity, (this.gpsInterface as Activity).resources.getString(
                    R.string.no_location), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("onConnected", e.message.toString())
        }
    }

    override fun onConnectionSuspended(i: Int) {
        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.connect()
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Toast.makeText((this.gpsInterface as AppCompatActivity), (this.gpsInterface as Activity).resources.getString(R.string.gps_error), Toast.LENGTH_SHORT).show()
    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
        // Request location updates
        try {
            if (ActivityCompat.checkSelfPermission((this.gpsInterface as Activity), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission((this.gpsInterface as Activity), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((this.gpsInterface as Activity), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 225)
                return
            }
            if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest, this)
            }
        } catch (e: TypeCastException) {
            Log.e("LocationUpdateException", e.message.toString())
        }
    }

    override fun onLocationChanged(location: Location) {
        if (location.accuracy > 20) {
            startLocationUpdates()
        }
//        Log.d("GPS", location.latitude.toString()+" "+location.longitude.toString()+" "+location.accuracy.toString())
        this.gpsInterface.locationChanged(location.longitude, location.latitude, location.accuracy)
    }

    private fun checkLocation(): Boolean {
        if (!isLocationEnabled)
            showLocationAlert((this.gpsInterface as Activity))
        return isLocationEnabled
    }

    private fun showLocationAlert(context: Context) {
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle(context.resources.getString(R.string.enable_loc))
                .setMessage(context.resources.getString(R.string.enable_loc_desc))
                .setPositiveButton(context.resources.getString(R.string.loc_settings)) { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    ContextCompat.startActivity(context, myIntent, null)
                }
                .setNegativeButton(context.resources.getString(R.string.cancel)) { paramDialogInterface, paramInt -> }
        dialog.show()
    }

    fun requestMoreAccuracy(){
        startLocationUpdates()
    }

    fun checkLocationEnabled(): Boolean {
        Log.d("GPSManager", isLocationEnabled.toString())
        return isLocationEnabled
    }

    fun endTracking() {
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected) {
            mGoogleApiClient!!.disconnect()
        }
    }

    fun startTracking() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.connect()
        }
    }
}