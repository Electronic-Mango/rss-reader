package prm.project2.ui.main.rssentriesall

import android.content.Intent
import prm.project2.rssentries.FAVOURITE
import prm.project2.rssentries.GUID
import prm.project2.rssentries.RssEntry
import prm.project2.ui.main.RssEntriesViewModel

class RssEntriesAllViewModel : RssEntriesViewModel() {

    fun updateEntryFromIntent(intent: Intent?): RssEntry? {
        return getEntry(intent?.getStringExtra(GUID) ?: return null)?.apply {
            favourite = intent.getBooleanExtra(FAVOURITE, favourite)
            read = true
            refreshEntries()
        }
    }
}
