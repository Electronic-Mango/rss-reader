package prm.project2.ui.main.rssentries.rssentriesfavourites

import android.util.Log
import prm.project2.rssentries.RssEntry
import prm.project2.ui.main.rssentries.RssEntriesViewModel

class RssEntriesFavouritesViewModel : RssEntriesViewModel() {

    fun updateEntry(guid: String?, favourite: Boolean, rssEntry: RssEntry?, markAsRead: Boolean = true): RssEntry? {
        val entry = getEntry(guid) ?: rssEntry ?: return null
        entry.read = entry.read || markAsRead
        Log.d("UPDATE-FAVOURITE-ENTRIES", "${entry.read}, $favourite, $entry")
        val entryExists = entryExists(entry)
        if (entryExists && !favourite) {
            removeEntry(entry)
        } else if (!entryExists && favourite) {
            addEntry(entry)
        } else {
            refreshEntries()
        }
        return entry
    }

    private fun addEntry(rssEntry: RssEntry) {
        (entries.value?.toMutableList() ?: ArrayList()).apply {
            add(0, rssEntry)
            setEntries(this)
        }
    }

    private fun entryExists(rssEntry: RssEntry): Boolean = entries.value?.contains(rssEntry) ?: false

    private fun removeEntry(rssEntry: RssEntry) {
        entries.value?.toMutableList()?.apply {
            remove(rssEntry)
            setEntries(this)
        }
    }
}
