package prm.project2

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import prm.project2.databinding.ActivityMainBinding
import prm.project2.rssentries.RssEntry
import prm.project2.rssentries.parseRssStream
import prm.project2.ui.main.AllRssEntriesViewModel
import prm.project2.ui.main.FavouriteRssEntriesViewModel
import prm.project2.ui.main.SectionsPagerAdapter
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

private const val RSS_LINK_POLAND = "https://www.polsatnews.pl/rss/polska.xml"
private const val RSS_LINK_INTERNATIONAL = "https://www.polsatnews.pl/rss/swiat.xml"

class MainActivity : AppCompatActivity() {

    private val allRssEntriesViewModel: AllRssEntriesViewModel by viewModels()
    private val favouriteRssEntriesViewModel: FavouriteRssEntriesViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewPager()
        setupFab()
        loadRss()
        allRssEntriesViewModel.setEntries("Wszystkie wpisy RSS!")
        favouriteRssEntriesViewModel.setEntries("Ulubione wpisy RSS!")
    }

    private fun setupViewPager() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
    }

    private fun setupFab() {
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    private fun loadRss() {
        thread {
            val connection = URL(correctRssPath()).openConnection() as HttpsURLConnection
            try {
                val rssEntries = parseRssStream(connection.inputStream)
                logEntries(rssEntries)
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun correctRssPath() = RSS_LINK_POLAND

    private fun logEntries(rssEntries: List<RssEntry>) {
        rssEntries.forEach { Log.d("RSS-ENTRY", it.toString()) }
        Log.d("RSS-NUMBER-OF-ENTRIES", rssEntries.size.toString())
    }

}