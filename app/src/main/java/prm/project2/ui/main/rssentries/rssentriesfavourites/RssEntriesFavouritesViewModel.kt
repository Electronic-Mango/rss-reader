package prm.project2.ui.main.rssentries.rssentriesfavourites

import prm.project2.rssentries.RssEntry
import prm.project2.ui.main.rssentries.RssEntriesViewModel

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
}
