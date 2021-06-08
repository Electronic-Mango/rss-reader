package prm.project2.ui.main.rssentriesfavourites

import android.content.Intent
import prm.project2.rssentries.FAVOURITE
import prm.project2.rssentries.GUID
import prm.project2.rssentries.RssEntry
import prm.project2.ui.main.RssEntriesViewModel

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
