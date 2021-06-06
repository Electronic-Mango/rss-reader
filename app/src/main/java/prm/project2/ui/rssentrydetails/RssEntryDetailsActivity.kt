package prm.project2.ui.rssentrydetails

import android.R.id.home
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.res.ResourcesCompat
import prm.project2.Common.RSS_ENTRY_TO_SHOW
import prm.project2.R
import prm.project2.R.id.favourite
import prm.project2.R.id.share
import prm.project2.R.string.*
import prm.project2.databinding.ActivityRssEntryDetailsBinding
import prm.project2.rssentries.RssEntry
import prm.project2.ui.CommonActivity
import java.time.format.DateTimeFormatter

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")

class RssEntryDetailsActivity : CommonActivity() {

    private lateinit var binding: ActivityRssEntryDetailsBinding
    private lateinit var rssEntry: RssEntry
    override val snackbarView: View
        get() = binding.rssEntryDetailsContent.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRssEntryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarRrsEntryDetails)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_rss_entry_details, menu)
        toggleFavouriteIcon(menu.findItem(favourite))
        return super.onCreateOptionsMenu(menu)
    }

    override fun onStart() {
        super.onStart()
        rssEntry = RSS_ENTRY_TO_SHOW!!
        binding.rssEntryDetailsContent.rssEntryDetailsTitle.text = rssEntry.title
        binding.rssEntryDetailsContent.rssEntryDetailsDate.text = rssEntry.date?.format(DATE_FORMATTER)
        binding.rssEntryDetailsContent.rssEntryDetailsDescription.text = rssEntry.description ?: ""
        binding.rssEntryDetailsContent.rssEntryDetailsLink.text = rssEntry.link ?: ""
        rssEntry.image?.let { binding.rssEntryDetailsContent.rssEntryDetailsImage.setImageBitmap(it) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            share -> {
                shareRssEntry()
                true
            }
            favourite -> {
                switchFavourite(item)
                true
            }
            home -> {
                finishActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun shareRssEntry() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "${rssEntry.title}: ${rssEntry.link}")
            type = "text/plain"
        }.let {
            Intent.createChooser(it, getString(share_rss_entry))
        }
        startActivity(shareIntent)
    }

    private fun switchFavourite(item: MenuItem) {
        toggleFavouriteEntryAndIcon(item)
        val snackMessage = if (rssEntry.favourite) entry_added_to_favourites else entry_removed_from_favourites
        showSnackbar(snackMessage).setAction(getString(undo_favouriting)) {
            toggleFavouriteEntryAndIcon(item)
        }
    }

    private fun toggleFavouriteEntryAndIcon(item: MenuItem) {
        rssEntry.favourite = !rssEntry.favourite
        addToFirestore(rssEntry)
        toggleFavouriteIcon(item)
    }

    private fun toggleFavouriteIcon(item: MenuItem) {
        val newIcon = if (rssEntry.favourite) R.drawable.ic_favorite_full else R.drawable.ic_favorite_border
        item.icon = ResourcesCompat.getDrawable(resources, newIcon, null)
    }

    private fun finishActivity() {
        setResult(Activity.RESULT_OK, Intent())
        finish()
    }
}
