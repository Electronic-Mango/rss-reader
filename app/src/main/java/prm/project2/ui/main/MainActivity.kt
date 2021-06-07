package prm.project2.ui.main

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.security.ProviderInstaller
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.QuerySnapshot
import prm.project2.Common.POLAND_COUNTRY_CODE
import prm.project2.Common.RSS_ENTRY_TO_SHOW
import prm.project2.FirebaseCommon.firebaseAuth
import prm.project2.FirebaseCommon.firebaseUsername
import prm.project2.FirebaseCommon.firestoreData
import prm.project2.R
import prm.project2.R.id.account_logout
import prm.project2.R.id.refresh
import prm.project2.R.string.*
import prm.project2.databinding.ActivityMainBinding
import prm.project2.rssentries.RssEntry
import prm.project2.rssentries.getEntry
import prm.project2.rssentries.parseRssStream
import prm.project2.rssentries.toRssEntry
import prm.project2.ui.CommonActivity
import prm.project2.ui.login.LoginActivity
import prm.project2.ui.main.rssentries.RssEntriesViewModel
import prm.project2.ui.main.rssentries.rssentriesfavourites.RssEntriesFavouritesViewModel
import prm.project2.ui.rssentrydetails.RssEntryDetailsActivity
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import kotlin.concurrent.thread

private const val RSS_LINK_POLAND = "https://www.polsatnews.pl/rss/polska.xml"
private const val RSS_LINK_INTERNATIONAL = "https://www.polsatnews.pl/rss/swiat.xml"
private const val LOCATION_REQUEST_ID = 100

private const val GOOGLE_PLAY_TAG = "MAIN-ACTIVITY-GP-SECURITY"
private const val BITMAP_LOADING_TAG = "MAIN-ACTIVITY-LOADING-IMG"
private const val LOADING_RSS_DATA_TAG = "MAIN-ACTIVITY-LOADING-RSS-DATA"

class MainActivity : CommonActivity() {

    private val rssEntriesAllViewModel: RssEntriesViewModel by viewModels()
    private val rssEntriesFavouritesViewModel: RssEntriesFavouritesViewModel by viewModels()
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationCancellationToken: CancellationToken
    private lateinit var binding: ActivityMainBinding
    private lateinit var showRssEntryDetailsActivityResult: ActivityResultLauncher<Intent>
    override val snackbarView: View
        get() = binding.viewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        setSupportActionBar(binding.toolbarMainActivity)
        setContentView(binding.root)

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCancellationToken = CancellationTokenSource().token

        setupViewPager()
        installSecurityProvider()
        checkLocationPermissionAndCurrentLocation()

        rssEntriesAllViewModel.entryToDisplay.observe(this, { launchFullRssEntryDetailsActivity(it) })
        rssEntriesFavouritesViewModel.entryToDisplay.observe(this, { launchFullRssEntryDetailsActivity(it) })
        rssEntriesAllViewModel.entryToToggleFavourite.observe(this, { toggleFavouriteRssEntry(it) })
        rssEntriesFavouritesViewModel.entryToToggleFavourite.observe(this, { toggleFavouriteRssEntry(it) })
        showRssEntryDetailsActivityResult = registerForActivityResult { handleRssEntryDetailsResponse() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            refresh -> {
                checkLocationPermissionAndCurrentLocation()
                true
            }
            account_logout -> {
                signOutAndSwitchToLoginActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = SectionsPagerAdapter(this, supportFragmentManager)
        binding.tabs.setupWithViewPager(binding.viewPager)
    }

    private fun installSecurityProvider() {
        try {
            ProviderInstaller.installIfNeeded(this)
        } catch (exception: GooglePlayServicesRepairableException) {
            Log.e(GOOGLE_PLAY_TAG, "Google Play is not available, but repairable!")
            GooglePlayServicesUtil.getErrorDialog(exception.connectionStatusCode, this, 0)
        } catch (exception: GooglePlayServicesNotAvailableException) {
            Log.e(GOOGLE_PLAY_TAG, "Google Play is not installed!")
        }
    }

    private fun checkLocationPermissionAndCurrentLocation() {
        showIndefiniteSnackbar(establishing_user_location)
        if (!checkPermissionsGranted(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)) {
            requestPermissions(arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION), LOCATION_REQUEST_ID)
        } else {
            checkCurrentLocationAndLoadRssData()
        }
    }

    private fun checkPermissionsGranted(vararg permissions: String): Boolean {
        return permissions.map { checkSelfPermission(it) == PERMISSION_GRANTED }.all { it }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_REQUEST_ID -> handleLocationPermissionsRequestResult(permissions, grantResults)
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun handleLocationPermissionsRequestResult(permissions: Array<out String>, grantResults: IntArray) {
        if (checkLocationPermissionsRequestResults(permissions, grantResults)) {
            checkCurrentLocationAndLoadRssData()
        } else {
            loadRssDataWithoutLocationData(getString(no_access_to_location_data))
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

    @SuppressLint("MissingPermission")
    private fun checkCurrentLocationAndLoadRssData() {
        if (isLocationEnabled()) {
            requestNewLocationData()
        } else {
            loadRssDataWithoutLocationData(getString(location_isnt_enabled))
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(GPS_PROVIDER) || locationManager.isProviderEnabled(NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        locationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, locationCancellationToken)
            .addOnCompleteListener { locationTask ->
                locationTask.result?.let { loadRssDataWithCorrectSource(it) } ?: loadRssDataWithoutLocationData()
            }
    }

    private fun loadRssDataWithCorrectSource(location: Location) {
        val countryCode = getCountryCodeFromLocation(location)
        if (countryCode == null) {
            loadRssDataWithoutLocationData(getString(location_couldnt_be_established))
        } else {
            val rssLink = if (countryCode == POLAND_COUNTRY_CODE) RSS_LINK_POLAND else RSS_LINK_INTERNATIONAL
            val loadingMessage = if (rssLink == RSS_LINK_POLAND) from_poland_label else international_label
            loadRssData(rssLink, getString(loading_news_label, getString(loadingMessage)))
        }
    }

    private fun getCountryCodeFromLocation(location: Location): String? {
        return Geocoder(this, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)
            .firstOrNull()?.countryCode
    }

    private fun loadRssDataWithoutLocationData(message: String = getString(no_location_data)) {
        loadRssData(RSS_LINK_INTERNATIONAL, getString(loading_international_news_tail_label, message))
    }

    private fun loadRssData(rssLink: String, message: String) {
        val loadingDataSnackbar = showIndefiniteSnackbar(message)
        thread {
            firestoreData.get()
                .addOnSuccessListener { firestoreDataRetrieveSuccess(it, rssLink, loadingDataSnackbar, message) }
                .addOnFailureListener { firestoreDataRetrieveFailure(rssLink, message) }
        }
    }

    private fun firestoreDataRetrieveSuccess(
        query: QuerySnapshot,
        rssLink: String,
        loadingDataSnackbar: Snackbar,
        loadingDataMessage: String
    ) {
        thread {
            try {
                loadAndHandleRssDataThread(query, rssLink, loadingDataSnackbar)
            } catch (e: Exception) {
                Log.e(LOADING_RSS_DATA_TAG, "Exception encountered when loading RSS: ${e.stackTraceToString()}.")
                firestoreDataRetrieveFailure(rssLink, loadingDataMessage)
            }
        }
    }

    private fun firestoreDataRetrieveFailure(rssLink: String, message: String) {
        showSnackbar(loading_rss_data_error).setAction(getString(repeat_operation)) {
            loadRssData(rssLink, message)
        }
    }

    private fun loadAndHandleRssDataThread(query: QuerySnapshot, rssLink: String, loadingDataSnackbar: Snackbar) {
        val firebaseEntries = query.documents.map { snapshot -> snapshot.toRssEntry() }
        val connection = URL(rssLink).openConnection() as HttpURLConnection
        val newEntries = parseRssStream(connection.inputStream).asSequence()
            .filter { it.guid.isNotBlank() }
            .filter { it.title.isNotBlankNorNull() }
            .onEach { it.image = loadBitmap(it.imageUrl) }
            .onEach {
                firebaseEntries.getEntry(it)?.let { firebaseEntry ->
                    it.read = firebaseEntry.read
                    it.favourite = firebaseEntry.favourite
                }
            }
            .sortedByDescending { it.date }
            .toList()
        val favouriteFirebaseEntries = firebaseEntries.asSequence()
            .filter { it.favourite }
            .onEach { it.image = loadBitmap(it.imageUrl) }
            .sortedByDescending { it.date }
            .toList()
        runOnUiThread {
            rssEntriesAllViewModel.setEntries(newEntries)
            rssEntriesFavouritesViewModel.setEntries(favouriteFirebaseEntries)
            (binding.viewPager.adapter as SectionsPagerAdapter).apply {
                allRssEntries.binding.rssEntriesRecyclerView.smoothScrollToPosition(0)
                favouriteRssEntries.binding.rssEntriesRecyclerView.smoothScrollToPosition(0)
            }
            loadingDataSnackbar.dismiss()
        }

    }

    private fun loadBitmap(url: String?): Bitmap? = url?.let {
        try {
            URL(url).openStream().let { BitmapFactory.decodeStream(it) }
        } catch (exception: MalformedURLException) {
            Log.e(BITMAP_LOADING_TAG, "Malformed URL $url!")
            null
        } catch (exception: IOException) {
            Log.e(BITMAP_LOADING_TAG, "I/O Exception when loading an image from $url!")
            null
        }
    }

    private fun launchFullRssEntryDetailsActivity(rssEntry: RssEntry) {
        RSS_ENTRY_TO_SHOW = rssEntry
        showRssEntryDetailsActivityResult.launch(Intent(this, RssEntryDetailsActivity::class.java))
        markAsRead(rssEntry)
    }

    private fun markAsRead(rssEntry: RssEntry) {
        rssEntry.read = true
        rssEntriesAllViewModel.refreshEntries()
        if (rssEntry.favourite) {
            rssEntriesFavouritesViewModel.refreshEntries()
        }
        addToFirestore(rssEntry)
    }

    private fun toggleFavouriteRssEntry(rssEntry: RssEntry) {
        val newFavouriteValue = !rssEntry.favourite
        val popupMessage = if (newFavouriteValue) add_entry_to_favourites else remove_entry_from_favourites
        AlertDialog.Builder(this)
            .setMessage(popupMessage)
            .setCancelable(true)
            .setPositiveButton(getString(yes_label)) { _, _ ->
                toggleFavourite(rssEntry)
                val snackMessage = if (newFavouriteValue) entry_added_to_favourites else entry_removed_from_favourites
                showSnackbar(snackMessage).setAction(getString(undo_favouriting)) {
                    toggleFavourite(rssEntry)
                }
            }
            .setNegativeButton(getString(no_label)) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun handleRssEntryDetailsResponse() {
        rssEntriesAllViewModel.refreshEntries()
        toggleFavouriteInViewModel(RSS_ENTRY_TO_SHOW!!)
    }

    private fun toggleFavourite(rssEntry: RssEntry) {
        rssEntry.favourite = !rssEntry.favourite
        rssEntriesAllViewModel.refreshEntries()
        toggleFavouriteInViewModel(rssEntry)
        if (!rssEntry.read && !rssEntry.favourite) {
            removeFromFirestore(rssEntry)
        } else {
            addToFirestore(rssEntry)
        }
    }

    private fun toggleFavouriteInViewModel(rssEntry: RssEntry) {
        if (rssEntry.favourite) {
            rssEntriesFavouritesViewModel.addEntry(rssEntry)
        } else {
            rssEntriesFavouritesViewModel.removeEntry(rssEntry)
        }
    }

    private fun signOutAndSwitchToLoginActivity() {
        AlertDialog.Builder(this)
            .setMessage(getString(logout_message, firebaseUsername!!))
            .setCancelable(true)
            .setPositiveButton(getString(yes_label)) { _, _ ->
                firebaseAuth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton(getString(no_label)) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}

private fun String?.isNotBlankNorNull(): Boolean = this?.isNotBlank() ?: false
