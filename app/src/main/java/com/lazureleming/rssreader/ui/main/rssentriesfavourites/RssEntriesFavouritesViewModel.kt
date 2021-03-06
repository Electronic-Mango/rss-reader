package com.lazureleming.rssreader.ui.main.rssentriesfavourites

import android.content.Intent
import com.lazureleming.rssreader.rssentries.FAVOURITE
import com.lazureleming.rssreader.rssentries.GUID
import com.lazureleming.rssreader.rssentries.RssEntry
import com.lazureleming.rssreader.ui.main.RssEntriesViewModel

/**
 * [androidx.lifecycle.ViewModel] storing user's favourited RSS entries.
 * Makes sure that only favourited entries are displayed.
 * Entries which were unfavourited are removed from this [androidx.fragment.app.Fragment].
 */
class RssEntriesFavouritesViewModel : RssEntriesViewModel() {

    fun addEntry(rssEntry: RssEntry) {
        if (entryExists(rssEntry)) return
        (entries.value?.toMutableList() ?: ArrayList()).apply {
            add(rssEntry)
            sortByDescending { it.date }
            setEntries(this)
        }
    }

    fun removeEntry(rssEntry: RssEntry) {
        if (!entryExists(rssEntry)) return
        entries.value?.toMutableList()?.apply {
            remove(rssEntry)
            setEntries(this)
        }
    }

    fun updateEntryFromIntent(intent: Intent?, newRssEntry: RssEntry?) {
        val existingEntry = getEntry(intent?.getStringExtra(GUID) ?: return)
        if (existingEntry == null && newRssEntry?.favourite == true) {
            addEntry(newRssEntry)
        } else if (existingEntry != null) {
            existingEntry.favourite = intent.getBooleanExtra(FAVOURITE, existingEntry.favourite)
            if (!existingEntry.favourite) {
                removeEntry(existingEntry)
            } else {
                existingEntry.read = true
                refreshEntries()
            }
        }
    }
}
