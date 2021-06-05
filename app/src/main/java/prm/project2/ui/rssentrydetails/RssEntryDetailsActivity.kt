package prm.project2.ui.rssentrydetails

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import prm.project2.Common.IMAGE_TO_SHOW
import prm.project2.Common.INTENT_DATA_DATE
import prm.project2.Common.INTENT_DATA_DESCRIPTION
import prm.project2.Common.INTENT_DATA_FAVOURITE
import prm.project2.Common.INTENT_DATA_GUID
import prm.project2.Common.INTENT_DATA_LINK
import prm.project2.Common.INTENT_DATA_TITLE
import prm.project2.R
import prm.project2.databinding.ActivityRssEntryDetailsBinding
import prm.project2.rssentries.RssEntry
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")

class RssEntryDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRssEntryDetailsBinding
    private lateinit var rssEntry: RssEntry

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
        setFavouriteIcon(menu.findItem(R.id.favourite))
        return super.onCreateOptionsMenu(menu)
    }

    override fun onStart() {
        super.onStart()
        rssEntry = RssEntry(
            intent.getStringExtra(INTENT_DATA_GUID)!!,
            intent.getStringExtra(INTENT_DATA_TITLE)!!,
            intent.getStringExtra(INTENT_DATA_LINK),
            intent.getStringExtra(INTENT_DATA_DESCRIPTION),
            intent.getSerializableExtra(INTENT_DATA_DATE) as LocalDateTime?,
            null,
            IMAGE_TO_SHOW,
            intent.getBooleanExtra(INTENT_DATA_FAVOURITE, false)
        )
        IMAGE_TO_SHOW = null
        binding.rssEntryDetailsContent.rssEntryDetailsTitle.text = rssEntry.title
        binding.rssEntryDetailsContent.rssEntryDetailsDate.text = rssEntry.date?.format(DATE_FORMATTER)
        binding.rssEntryDetailsContent.rssEntryDetailsDescription.text = rssEntry.description ?: ""
        binding.rssEntryDetailsContent.rssEntryDetailsLink.text = rssEntry.link ?: ""
        rssEntry.image?.let { binding.rssEntryDetailsContent.rssEntryDetailsImage.setImageBitmap(it) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.share -> shareRssEntry()
            R.id.favourite -> switchFavourite(item)
            android.R.id.home -> finishActivity()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun shareRssEntry(): Boolean {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "${rssEntry.title}: ${rssEntry.link}")
            type = "text/plain"
        }.let {
            Intent.createChooser(it, "Udostępnij wpis")
        }
        startActivity(shareIntent)
        return true
    }

    private fun switchFavourite(item: MenuItem): Boolean {
        rssEntry.favourite = !rssEntry.favourite
        setFavouriteIcon(item)
        val snackMessage = if (rssEntry.favourite) "Wpis dodany do ulubionych..." else "Wpis usunięty z ulubionych..."
        Snackbar.make(binding.rssEntryDetailsContent.root, snackMessage, LENGTH_LONG)
            .setAction("Cofnij") {
                rssEntry.favourite = !rssEntry.favourite
                setFavouriteIcon(item)
            }.show()
        return true
    }

    private fun setFavouriteIcon(item: MenuItem) {
        val newIcon = if (rssEntry.favourite) R.drawable.ic_favorite_full else R.drawable.ic_favorite_border
        item.icon = ResourcesCompat.getDrawable(resources, newIcon, null)
    }

    private fun finishActivity(): Boolean {
        Intent().apply {
            putExtra(INTENT_DATA_GUID, rssEntry.guid)
            putExtra(INTENT_DATA_FAVOURITE, rssEntry.favourite)
            setResult(Activity.RESULT_OK, this)
        }
        finish()
        return true
    }
}
