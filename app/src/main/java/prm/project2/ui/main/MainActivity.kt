package prm.project2.ui.main

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
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
import prm.project2.ui.rssentrydetails.RssEntryDetailsActivity
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.stream.Collectors.toList
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

private const val RSS_LINK_POLAND = "https://www.polsatnews.pl/rss/polska.xml"
private const val RSS_LINK_INTERNATIONAL = "https://www.polsatnews.pl/rss/swiat.xml"

class MainActivity : AppCompatActivity() {

    private val allRssEntriesViewModel: AllRssEntriesViewModel by viewModels()
    private val favouriteRssEntriesViewModel: FavouriteRssEntriesViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private val showRssEntryDetails = registerForActivityResult(StartActivityForResult()) {
        val displayedEntry = it.data?.let { allRssEntriesViewModel.getEntry(it.getStringExtra(INTENT_DATA_GUID)) }
        displayedEntry?.apply {
            favourite = it.data?.getBooleanExtra(INTENT_DATA_FAVOURITE, favourite) ?: favourite
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbarMainActivity)
        setContentView(binding.root)
        setupViewPager()
        loadRss()
        allRssEntriesViewModel.entryToDisplay.observe(this, { runFullRssEntryDetailsActivity(it) })
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
            val connection = URL(correctRssPath()).openConnection() as HttpsURLConnection
            parseRssStream(connection.inputStream).stream()
                .peek { Log.d("RSS-ENTRY", it.toString()) }
                .filter { it.guid != null && it.title != null }
                .peek { it.image = loadBitmap(it.imageUrl) }
                .collect(toList()).let {
                    runOnUiThread {
                        allRssEntriesViewModel.setEntries(it)
                        dialog.dismiss()
                    }
                }
        }
    }

    private fun loadBitmap(url: String?): Bitmap? {
        if (url == null) return null
        var bitmap: Bitmap? = null
        try {
            bitmap = URL(url).openStream().let { BitmapFactory.decodeStream(it) }
        } catch (exception: MalformedURLException) {
            Log.d("LOADING-IMG", "Malformed URL $url!")
        } catch (exception: IOException) {
            Log.d("LOADING-IMG", "I/O Exception when loading IMG!")
        }
        return bitmap
    }

    private fun correctRssPath() = RSS_LINK_POLAND

    private fun runFullRssEntryDetailsActivity(rssEntry: RssEntry) {
        Log.d("DISPLAY-ENTRY", "Showing full info about $rssEntry")
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

}