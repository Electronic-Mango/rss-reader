package prm.project2.ui.main

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import prm.project2.Common.IMAGE_TO_SHOW
import prm.project2.Common.INTENT_DATA_DATE
import prm.project2.Common.INTENT_DATA_DESCRIPTION
import prm.project2.Common.INTENT_DATA_FAVOURITE
import prm.project2.Common.INTENT_DATA_GUID
import prm.project2.Common.INTENT_DATA_LINK
import prm.project2.Common.INTENT_DATA_TITLE
import prm.project2.R
import prm.project2.databinding.ActivityMainBinding
import prm.project2.rssentries.RssEntry
import prm.project2.rssentries.parseRssStream
import prm.project2.ui.main.rssentries.rssentriesall.RssEntriesAllViewModel
import prm.project2.ui.main.rssentries.rssentriesfavourites.RssEntriesFavouritesViewModel
import prm.project2.ui.rssentrydetails.RssEntryDetailsActivity
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.stream.Collectors.toList
import kotlin.concurrent.thread

private const val RSS_LINK_POLAND = "https://www.polsatnews.pl/rss/polska.xml"
private const val RSS_LINK_INTERNATIONAL = "https://www.polsatnews.pl/rss/swiat.xml"

class MainActivity : AppCompatActivity() {

    private val rssEntriesAllViewModel: RssEntriesAllViewModel by viewModels()
    private val rssEntriesFavouritesViewModel: RssEntriesFavouritesViewModel by viewModels()
    private val showRssEntryDetails = registerForActivityResult(StartActivityForResult()) {
        handleRssEntryDetailsResponse(it)
    }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbarMainActivity)
        setContentView(binding.root)
        setupViewPager()
        loadRss()
        rssEntriesAllViewModel.entryToDisplay.observe(this, { runFullRssEntryDetailsActivity(it) })
        rssEntriesFavouritesViewModel.entryToDisplay.observe(this, { runFullRssEntryDetailsActivity(it) })
        rssEntriesAllViewModel.entryToToggleFavourite.observe(this, { toggleFavouriteOnRssEntry(it) })
        rssEntriesFavouritesViewModel.entryToToggleFavourite.observe(this, { toggleFavouriteOnRssEntry(it) })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                loadRss()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupViewPager() {
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
    }

    private fun loadRss() {
        val dialog = ProgressDialog.show(this, "", "Ładowanie danych...")
        thread {
            val connection = URL(RSS_LINK_POLAND).openConnection() as HttpURLConnection
            parseRssStream(connection.inputStream).stream()
                .filter { it.guid != null && it.title != null }
                .peek { it.image = loadBitmap(it.imageUrl) }
                .collect(toList()).let {
                    runOnUiThread {
                        rssEntriesAllViewModel.setEntries(it)
                        dialog.dismiss()
                    }
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
        showRssEntryDetails.launch(intent)
    }

    private fun toggleFavouriteOnRssEntry(rssEntry: RssEntry) {
        val newFavouriteValue = !rssEntry.favourite
        val popupMessage = if (newFavouriteValue) "Dodać wpis do ulubionych?" else "Usunąć wpis z ulubionych?"
        AlertDialog.Builder(this)
            .setMessage(popupMessage)
            .setCancelable(false)
            .setPositiveButton("Tak") { _, _ -> updateEntries(rssEntry.guid, newFavouriteValue, false) }
            .setNegativeButton("Nie") { dialog, _ -> dialog.dismiss() }
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
        rssEntriesFavouritesViewModel.updateEntry(guid, favourite, modifiedEntry, markAsRead)
    }

}