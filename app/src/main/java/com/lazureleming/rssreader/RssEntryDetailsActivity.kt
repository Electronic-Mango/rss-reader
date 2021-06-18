package com.lazureleming.rssreader

import android.R.id.home
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import com.lazureleming.rssreader.R.drawable.ic_favorite_border
import com.lazureleming.rssreader.R.drawable.ic_favorite_full
import com.lazureleming.rssreader.R.id.favourite
import com.lazureleming.rssreader.R.id.share
import com.lazureleming.rssreader.R.menu.menu_rss_entry_details
import com.lazureleming.rssreader.R.string.*
import com.lazureleming.rssreader.databinding.ActivityRssEntryDetailsBinding
import com.lazureleming.rssreader.rssentries.FAVOURITE
import com.lazureleming.rssreader.rssentries.GUID
import com.lazureleming.rssreader.rssentries.RssEntry
import com.lazureleming.rssreader.rssentries.toRssEntry
import com.lazureleming.rssreader.ui.rssentrydetails.RssEntryDetailsViewModel
import com.lazureleming.rssreader.utils.Common.INTENT_FROM_NOTIFICATION

/**
 * Activity displaying details for a single RSS entry.
 */
class RssEntryDetailsActivity : CommonActivity() {

    private val rssEntryDetailsViewModel: RssEntryDetailsViewModel by viewModels()
    private lateinit var binding: ActivityRssEntryDetailsBinding
    private lateinit var rssEntry: RssEntry
    override val snackbarView: View
        get() = binding.container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRssEntryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(!intent.getBooleanExtra(INTENT_FROM_NOTIFICATION, false))
        supportActionBar?.setDisplayShowHomeEnabled(!intent.getBooleanExtra(INTENT_FROM_NOTIFICATION, false))
        rssEntry = intent.toRssEntry()!!
        addToFirestore(rssEntry)
    }

    override fun onResume() {
        super.onResume()
        rssEntryDetailsViewModel.setRssEntry(rssEntry)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(menu_rss_entry_details, menu)
        toggleFavouriteIcon(menu.findItem(favourite))
        return super.onCreateOptionsMenu(menu)
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

    override fun onBackPressed() {
        finishActivity()
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
        toggleFavouriteIcon(item)
        addToFirestore(rssEntry)
    }

    private fun toggleFavouriteIcon(item: MenuItem) {
        val newIcon = if (rssEntry.favourite) ic_favorite_full else ic_favorite_border
        item.icon = ResourcesCompat.getDrawable(resources, newIcon, null)
    }

    private fun finishActivity() {
        Intent().apply {
            putExtra(GUID, rssEntry.guid)
            putExtra(FAVOURITE, rssEntry.favourite)
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }
}
