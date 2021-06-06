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
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.room.Room
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.security.ProviderInstaller
import com.google.android.gms.tasks.CancellationTokenSource
import prm.project2.Common.DB_NAME
import prm.project2.Common.IMAGE_TO_SHOW
import prm.project2.Common.INTENT_DATA_DATE
import prm.project2.Common.INTENT_DATA_DESCRIPTION
import prm.project2.Common.INTENT_DATA_FAVOURITE
import prm.project2.Common.INTENT_DATA_GUID
import prm.project2.Common.INTENT_DATA_LINK
import prm.project2.Common.INTENT_DATA_TITLE
import prm.project2.Common.POLAND_COUNTRY_CODE
import prm.project2.FirebaseCommon.firebaseAuth
import prm.project2.FirebaseCommon.firebaseUser
import prm.project2.R
import prm.project2.R.string.*
import prm.project2.database.ReadRssGuid
import prm.project2.database.ReadRssGuidDatabase
import prm.project2.databinding.ActivityMainBinding
import prm.project2.rssentries.RssEntry
import prm.project2.rssentries.parseRssStream
import prm.project2.ui.CommonActivity
import prm.project2.ui.login.LoginActivity
import prm.project2.ui.main.rssentries.rssentriesall.RssEntriesAllViewModel
import prm.project2.ui.main.rssentries.rssentriesfavourites.RssEntriesFavouritesViewModel
import prm.project2.ui.rssentrydetails.RssEntryDetailsActivity
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.stream.Collectors.toList
import kotlin.concurrent.thread

private const val RSS_LINK_POLAND = "https://www.polsatnews.pl/rss/polska.xml"
private const val RSS_LINK_INTERNATIONAL = "https://www.polsatnews.pl/rss/swiat.xml"
private const val LOCATION_REQUEST_ID = 100

class MainActivity : CommonActivity() {

    private val rssEntriesAllViewModel: RssEntriesAllViewModel by viewModels()
    private val rssEntriesFavouritesViewModel: RssEntriesFavouritesViewModel by viewModels()
    private val locationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val locationCancellationToken by lazy { CancellationTokenSource().token }
    private val database by lazy { Room.databaseBuilder(this, ReadRssGuidDatabase::class.java, DB_NAME).build() }
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
        setupViewPager()
        installSecurityProvider()
        checkLocationPermissionAndCurrentLocation()

        rssEntriesAllViewModel.entryToDisplay.observe(this, { runFullRssEntryDetailsActivity(it) })
        rssEntriesFavouritesViewModel.entryToDisplay.observe(this, { runFullRssEntryDetailsActivity(it) })
        rssEntriesAllViewModel.entryToToggleFavourite.observe(this, { toggleFavouriteOnRssEntry(it) })
        rssEntriesFavouritesViewModel.entryToToggleFavourite.observe(this, { toggleFavouriteOnRssEntry(it) })
        showRssEntryDetailsActivityResult = registerForActivityResult { handleRssEntryDetailsResponse(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                checkLocationPermissionAndCurrentLocation()
                true
            }
            R.id.account_logout -> {
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
            Log.w("GP-SECURITY", "Google Play is not available, but repairable!")
            GooglePlayServicesUtil.getErrorDialog(exception.connectionStatusCode, this, 0)
        } catch (exception: GooglePlayServicesNotAvailableException) {
            Log.w("GP-SECURITY", "Google Play is not installed!")
        }
    }

    private fun checkLocationPermissionAndCurrentLocation() {
        showIndefiniteSnackbar(establishing_user_location)
        if (!checkPermissionsGranted(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)) {
            requestPermissions(arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION), LOCATION_REQUEST_ID)
        } else {
            checkCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_REQUEST_ID -> handleLocationRequestResult(permissions, grantResults)
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun handleLocationRequestResult(permissions: Array<out String>, grantResults: IntArray) {
        if (checkLocationRequestResults(permissions, grantResults)) {
            checkCurrentLocation()
        } else {
            loadRssWithoutLocationData(getString(no_access_to_location_data))
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkCurrentLocation() {
        if (isLocationEnabled()) {
            requestNewLocationData()
        } else {
            loadRssWithoutLocationData(getString(location_isnt_enabled))
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(GPS_PROVIDER) || locationManager.isProviderEnabled(NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        locationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, locationCancellationToken)
            .addOnCompleteListener { handleLocationUpdate(it.result) }
    }

    private fun handleLocationUpdate(location: Location?) {
        if (location == null) {
            loadRssWithoutLocationData()
            return
        }
        val countryCode = getCountryCodeFromLocation(location)
        if (countryCode == null) {
            loadRssWithoutLocationData(getString(location_couldnt_be_established))
        } else {
            val rssLink = if (countryCode == POLAND_COUNTRY_CODE) RSS_LINK_POLAND else RSS_LINK_INTERNATIONAL
            val loadingMessage = if (rssLink == RSS_LINK_POLAND) from_poland_label else international_label
            loadRss(rssLink, getString(loading_news_label, getString(loadingMessage)))
        }
    }

    private fun loadRssWithoutLocationData(message: String = getString(no_location_data)) {
        loadRss(RSS_LINK_INTERNATIONAL, getString(loading_international_news_tail_label, message))
    }

    private fun loadRss(rssLink: String, message: String) {
        val loadingDataSnackbar = showIndefiniteSnackbar(message)
        thread {
            val existingReadEntries = database.readRssGuidDao().getAll()
            val connection = URL(rssLink).openConnection() as HttpURLConnection
            val loadedEntries = parseRssStream(connection.inputStream).stream()
                .filter { newEntry -> newEntry.guid.isNotBlank() && newEntry.title.isNotBlank() }
                .peek { newEntry -> newEntry.image = loadBitmap(newEntry.imageUrl) }
                .peek(this::markAsReadAndFavouriteIfRequired)
                .peek { newEntry -> newEntry.read = existingReadEntries.contains(newEntry.guid.toEntity()) }
                .collect(toList())
            database.readRssGuidDao().deleteAll()
            loadedEntries.stream()
                .filter { it.read }
                .map { it.guid.toEntity() }
                .collect(toList()).let { database.readRssGuidDao().insertAll(it) }
            runOnUiThread {
                rssEntriesAllViewModel.setEntries(loadedEntries)
                loadingDataSnackbar.dismiss()
            }
        }
    }

    private fun loadBitmap(url: String?): Bitmap? = url?.let {
        try {
            URL(url).openStream().let { BitmapFactory.decodeStream(it) }
        } catch (exception: MalformedURLException) {
            Log.w("LOADING-IMG", "Malformed URL $url!")
            null
        } catch (exception: IOException) {
            Log.w("LOADING-IMG", "I/O Exception when loading an image from $url!")
            null
        }
    }

    private fun markAsReadAndFavouriteIfRequired(rssEntry: RssEntry) {
        rssEntriesAllViewModel.getEntry(rssEntry.guid)?.let {
            rssEntry.read = it.read
            rssEntry.favourite = it.favourite
        }
    }

    private fun runFullRssEntryDetailsActivity(rssEntry: RssEntry) {
        val intent = Intent(this, RssEntryDetailsActivity::class.java).apply {
            putExtra(INTENT_DATA_GUID, rssEntry.guid)
            putExtra(INTENT_DATA_TITLE, rssEntry.title)
            putExtra(INTENT_DATA_LINK, rssEntry.link)
            putExtra(INTENT_DATA_DESCRIPTION, rssEntry.description)
            putExtra(INTENT_DATA_DATE, rssEntry.date)
            putExtra(INTENT_DATA_FAVOURITE, rssEntry.favourite)
        }
        IMAGE_TO_SHOW = rssEntry.image
        showRssEntryDetailsActivityResult.launch(intent)
    }

    private fun toggleFavouriteOnRssEntry(rssEntry: RssEntry) {
        val newFavouriteValue = !rssEntry.favourite
        val popupMessage = if (newFavouriteValue) add_entry_to_favourites else remove_entry_from_favourites
        AlertDialog.Builder(this)
            .setMessage(popupMessage)
            .setCancelable(false)
            .setPositiveButton(getString(yes_label)) { _, _ ->
                updateEntries(rssEntry.guid, newFavouriteValue, false)
                val snackMessage = if (newFavouriteValue) entry_added_to_favourites else entry_removed_from_favourites
                showSnackbar(snackMessage).setAction(getString(undo_favouriting)) {
                    updateEntries(rssEntry.guid, !newFavouriteValue, false)
                }
            }
            .setNegativeButton(getString(no_label)) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun handleRssEntryDetailsResponse(activityResult: ActivityResult) {
        activityResult.data?.apply {
            val guid = getStringExtra(INTENT_DATA_GUID)
            val favourite = getBooleanExtra(INTENT_DATA_FAVOURITE, false)
            updateEntries(guid, favourite)
        }
    }

    private fun updateEntries(guid: String?, favourite: Boolean, markAsRead: Boolean = true) {
        val modifiedEntry = rssEntriesAllViewModel.updateEntry(guid, favourite, markAsRead)
        val modifiedEntryFav = rssEntriesFavouritesViewModel.updateEntry(guid, favourite, modifiedEntry, markAsRead)
        thread {
            if (markAsRead) {
                modifiedEntry?.guid?.let { ReadRssGuid(it) }?.let { database.readRssGuidDao().insert(it) }
                modifiedEntryFav?.guid?.let { ReadRssGuid(it) }?.let { database.readRssGuidDao().insert(it) }
            }
        }
    }

    private fun checkPermissionsGranted(vararg permissions: String): Boolean {
        return permissions.map { checkSelfPermission(it) == PERMISSION_GRANTED }.all { it }
    }

    private fun checkLocationRequestResults(permissions: Array<out String>, grantResults: IntArray): Boolean {
        return permissions.contains(ACCESS_COARSE_LOCATION)
                && permissions.contains(ACCESS_FINE_LOCATION)
                && grantResults[permissions.indexOf(ACCESS_COARSE_LOCATION)] == PERMISSION_GRANTED
                && grantResults[permissions.indexOf(ACCESS_FINE_LOCATION)] == PERMISSION_GRANTED
    }

    private fun getCountryCodeFromLocation(location: Location): String? {
        return Geocoder(this, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)
            .firstOrNull()?.countryCode
    }

    private fun signOutAndSwitchToLoginActivity() {
        AlertDialog.Builder(this)
            .setMessage(getString(logout_message, firebaseUser!!.displayName ?: firebaseUser!!.email))
            .setCancelable(false)
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

private fun String.toEntity(): ReadRssGuid = ReadRssGuid(this)
