package prm.project2

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import prm.project2.databinding.ActivityMainBinding
import prm.project2.rssentries.RssEntryImg
import prm.project2.rssentries.parseRssStream
import prm.project2.ui.main.AllRssEntriesViewModel
import prm.project2.ui.main.FavouriteRssEntriesViewModel
import prm.project2.ui.main.SectionsPagerAdapter
import java.net.URL
import java.util.stream.Collectors
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
        setContentView(R.layout.activity_main)
        setupViewPager()
        setupFab()
        setSupportActionBar(binding.toolbarMainActivity)
        loadRss()
//        allRssEntriesViewModel.setEntries("Wszystkie wpisy RSS!")
//        favouriteRssEntriesViewModel.setEntries("Ulubione wpisy RSS!")
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
        val dialog = ProgressDialog.show(this, "", "Ładowanie danych...")
        thread {
            val connection = URL(correctRssPath()).openConnection() as HttpsURLConnection
            try {
                parseRssStream(connection.inputStream).stream()
                    .peek { Log.d("RSS-ENTRY", it.toString()) }
                    .filter {it.guid != null && it.title != null}
                    .map(RssEntryImg::newInstance)
                    .collect(Collectors.toList()).let {
                        runOnUiThread {
                            allRssEntriesViewModel.setEntries(it)
                            dialog.dismiss()
                        }
                    }
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun correctRssPath() = RSS_LINK_POLAND

}