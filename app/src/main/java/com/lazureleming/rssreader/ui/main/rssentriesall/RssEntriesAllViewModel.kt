package com.lazureleming.rssreader.ui.main.rssentriesall

import android.content.Intent
import com.lazureleming.rssreader.rssentries.FAVOURITE
import com.lazureleming.rssreader.rssentries.GUID
import com.lazureleming.rssreader.rssentries.RssEntry
import com.lazureleming.rssreader.ui.main.RssEntriesViewModel

/**
 * [androidx.lifecycle.ViewModel] storing loaded new RSS entries data.
 */
class RssEntriesAllViewModel : RssEntriesViewModel() {

    fun updateEntryFromIntent(intent: Intent?): RssEntry? {
        return getEntry(intent?.getStringExtra(GUID) ?: return null)?.apply {
            favourite = intent.getBooleanExtra(FAVOURITE, favourite)
            read = true
            refreshEntries()
        }
    }
}
