package prm.project2.ui.main.rssentries.rssentriesall

import android.util.Log
import prm.project2.rssentries.RssEntry
import prm.project2.ui.main.rssentries.RssEntriesViewModel

class RssEntriesAllViewModel : RssEntriesViewModel() {

    fun updateEntry(guid: String?, favourite: Boolean, markAsRead: Boolean = true): RssEntry? {
        val entry = getEntry(guid)?.apply {
            this.favourite = favourite
            read = read || markAsRead
        }
        Log.d("UPDATE-ALL-ENTRIES", "${entry?.read}, $favourite, $entry")
        refreshEntries()
        return entry
    }
}
