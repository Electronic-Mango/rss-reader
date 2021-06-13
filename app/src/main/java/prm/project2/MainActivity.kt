package prm.project2

import android.app.AlertDialog
import android.content.Intent
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
import com.google.android.gms.security.ProviderInstaller
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import prm.project2.R.id.account_logout
import prm.project2.R.id.refresh
import prm.project2.R.string.*
import prm.project2.databinding.ActivityMainBinding
import prm.project2.rssentries.RssEntry
import prm.project2.rssentries.getEntry
import prm.project2.rssentries.toRssEntry
import prm.project2.ui.main.SectionsPagerAdapter
import prm.project2.ui.main.rssentriesall.RssEntriesAllViewModel
import prm.project2.ui.main.rssentriesfavourites.RssEntriesFavouritesViewModel
import prm.project2.utils.Common.POLAND_COUNTRY_CODE
import prm.project2.utils.CountryGeoLocator
import prm.project2.utils.Firebase.firebaseAuth
import prm.project2.utils.Firebase.firebaseUsername
import prm.project2.utils.Firebase.firestoreData
import prm.project2.utils.RemoteResourcesLoader.tryToLoadAndParseRssData
import kotlin.concurrent.thread

private const val RSS_LINK_POLAND = "https://www.polsatnews.pl/rss/polska.xml"
private const val RSS_LINK_INTERNATIONAL = "https://www.polsatnews.pl/rss/swiat.xml"
const val LOCATION_REQUEST_ID = 100

private const val GOOGLE_PLAY_TAG = "MAIN-ACTIVITY-GP-SECURITY"

class MainActivity : CommonActivity() {

    private val rssEntriesAllViewModel: RssEntriesAllViewModel by viewModels()
    private val rssEntriesFavouritesViewModel: RssEntriesFavouritesViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var countryGeoLocator: CountryGeoLocator
    private lateinit var showRssEntryDetailsActivityResult: ActivityResultLauncher<Intent>
    override val snackbarView: View
        get() = binding.viewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarMainActivity)
        setContentView(binding.root)

        setupViewPager()
        installSecurityProvider()
        countryGeoLocator = CountryGeoLocator(
            activity = this,
            locationEstablishedCallback = this::loadRssDataWithCorrectCountryCode,
            locationNotEstablishedCallback = { loadRssDataWithoutLocationData() },
            locationNotEnabledCallback = { loadRssDataWithoutLocationData(getString(location_is_not_enabled)) },
            locationNotPermittedCallback = { loadRssDataWithoutLocationData(getString(no_access_to_location_data)) }
        )
        checkLocationPermissionAndCurrentLocation()

        rssEntriesAllViewModel.entryToDisplay.observe(this, { launchFullRssEntryDetailsActivity(it) })
        rssEntriesFavouritesViewModel.entryToDisplay.observe(this, { launchFullRssEntryDetailsActivity(it) })
        rssEntriesAllViewModel.entryToToggleFavourite.observe(this, { toggleFavouriteRssEntry(it) })
        rssEntriesFavouritesViewModel.entryToToggleFavourite.observe(this, { toggleFavouriteRssEntry(it) })
        showRssEntryDetailsActivityResult = registerForActivityResult { handleRssEntryDetailsResponse(it.data) }
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
        countryGeoLocator.runCallbackForCurrentCountryCodeOrRequestPermissions()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_REQUEST_ID -> countryGeoLocator.handleLocationPermissionsRequestResult(permissions, grantResults)
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun loadRssDataWithCorrectCountryCode(countryCode: String?) {
        if (countryCode == null) {
            loadRssDataWithoutLocationData(getString(location_could_not_be_established))
        } else {
            val rssLink = if (countryCode == POLAND_COUNTRY_CODE) RSS_LINK_POLAND else RSS_LINK_INTERNATIONAL
            val loadingMessage = if (rssLink == RSS_LINK_POLAND) from_poland_label else international_label
            loadNewAndFavouritedRssData(rssLink, getString(loading_news_label, getString(loadingMessage)))
        }
    }

    private fun loadRssDataWithoutLocationData(message: String = getString(no_location_data)) {
        loadNewAndFavouritedRssData(RSS_LINK_INTERNATIONAL, getString(loading_international_news_tail_label, message))
    }

    private fun loadNewAndFavouritedRssData(rssLink: String, message: String) {
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
            tryToLoadAndParseRssData(
                rssLink,
                { rssEntries -> loadAndHandleRssDataThread(query, rssEntries, loadingDataSnackbar, rssLink) },
                { firestoreDataRetrieveFailure(rssLink, loadingDataMessage) })
        }
    }

    private fun firestoreDataRetrieveFailure(rssLink: String, message: String) {
        showSnackbar(loading_rss_data_error).setAction(getString(repeat_operation)) {
            loadNewAndFavouritedRssData(rssLink, message)
        }
    }

    private fun loadAndHandleRssDataThread(
        query: QuerySnapshot,
        newRssData: List<RssEntry>,
        loadingDataSnackbar: Snackbar,
        rssLink: String
    ) {
        val firebaseEntries = query.documents.map(DocumentSnapshot::toRssEntry).sortedByDescending(RssEntry::date)
        val newEntries = newRssData.onEach { newRssEntry ->
            firebaseEntries.getEntry(newRssEntry)?.let { firebaseEntry ->
                newRssEntry.read = firebaseEntry.read
                newRssEntry.favourite = firebaseEntry.favourite
            }
        }
        val favouriteFirebaseEntries = firebaseEntries.asSequence().filter(RssEntry::favourite).toList()
        runOnUiThread {
            updateEntries(newEntries, favouriteFirebaseEntries)
            loadingDataSnackbar.dismiss()
        }
        newEntries.firstOrNull()?.let { setupWorker(this, it, rssLink) }
    }

    private fun updateEntries(newEntries: List<RssEntry>, favouriteFirebaseEntries: List<RssEntry>) {
        rssEntriesAllViewModel.setEntries(newEntries)
        rssEntriesFavouritesViewModel.setEntries(favouriteFirebaseEntries)
        (binding.viewPager.adapter as SectionsPagerAdapter).apply {
            allRssEntries.binding.rssEntriesRecyclerView.smoothScrollToPosition(0)
            favouriteRssEntries.binding.rssEntriesRecyclerView.smoothScrollToPosition(0)
        }
    }

    private fun launchFullRssEntryDetailsActivity(rssEntry: RssEntry) {
        showRssEntryDetailsActivityResult.launch(rssEntry.toIntent(this, RssEntryDetailsActivity::class))
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
                showSnackbar(snackMessage).setAction(getString(undo_favouriting)) { toggleFavourite(rssEntry) }
            }
            .setNegativeButton(getString(no_label)) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun handleRssEntryDetailsResponse(data: Intent?) {
        val existingRssEntry = rssEntriesAllViewModel.updateEntryFromIntent(data)
        rssEntriesFavouritesViewModel.updateEntryFromIntent(data, existingRssEntry)
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
                cancelWorker(this)
                notificationManager.cancelAll()
                firebaseAuth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton(getString(no_label)) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}
