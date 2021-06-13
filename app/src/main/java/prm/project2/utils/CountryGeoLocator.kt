package prm.project2.utils

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import android.util.Log
import androidx.appcompat.app.AppCompatActivity.LOCATION_SERVICE
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import prm.project2.LOCATION_REQUEST_ID
import java.util.*

private const val LOG_TAG = "GEOLOCATION"

class CountryGeoLocator(
    private val activity: Activity,
    private val locationEstablishedCallback: (countryCode: String?) -> Unit,
    private val locationNotEstablishedCallback: () -> Unit,
    private val locationNotEnabledCallback: () -> Unit,
    private val locationNotPermittedCallback: () -> Unit,
) {

    private val locationClient = LocationServices.getFusedLocationProviderClient(activity)
    private val locationCancellationToken = CancellationTokenSource().token

    fun runCallbackForCurrentCountryCodeOrRequestPermissions() {
        if (!checkPermissionsGranted(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)) {
            activity.requestPermissions(arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION), LOCATION_REQUEST_ID)
        } else {
            checkCurrentLocationAndLoadRssData()
        }
    }

    fun handleLocationPermissionsRequestResult(permissions: Array<out String>, grantResults: IntArray) {
        if (checkLocationPermissionsRequestResults(permissions, grantResults)) {
            checkCurrentLocationAndLoadRssData()
        } else {
            locationNotPermittedCallback()
        }
    }

    private fun checkPermissionsGranted(vararg permissions: String): Boolean {
        return permissions.map { activity.checkSelfPermission(it) == PERMISSION_GRANTED }.all { it }
    }

    private fun checkCurrentLocationAndLoadRssData() {
        if (isLocationEnabled()) {
            requestNewLocationData()
        } else {
            locationNotEnabledCallback()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = activity.getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(GPS_PROVIDER) || locationManager.isProviderEnabled(NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        locationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, locationCancellationToken)
            .addOnCompleteListener { locationTask ->
                locationTask.result?.let {
                    val locatedCountryCode = getCountryCodeFromLocation(it)
                    locationEstablishedCallback(locatedCountryCode)
                } ?: locationNotEstablishedCallback()
            }
    }

    private fun getCountryCodeFromLocation(location: Location): String? {
        return try {
            Geocoder(activity, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)
                .firstOrNull()?.countryCode
        } catch (exception: Exception) {
            Log.e(LOG_TAG, "Exception when extracting country from location: ${exception.stackTraceToString()}")
            null
        }
    }

    private fun checkLocationPermissionsRequestResults(
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        return permissions.contains(ACCESS_COARSE_LOCATION)
                && permissions.contains(ACCESS_FINE_LOCATION)
                && grantResults[permissions.indexOf(ACCESS_COARSE_LOCATION)] == PERMISSION_GRANTED
                && grantResults[permissions.indexOf(ACCESS_FINE_LOCATION)] == PERMISSION_GRANTED
    }
}