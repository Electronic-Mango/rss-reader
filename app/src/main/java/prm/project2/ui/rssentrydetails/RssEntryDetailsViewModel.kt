package prm.project2.ui.rssentrydetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import prm.project2.rssentries.RssEntry

class RssEntryDetailsViewModel : ViewModel() {

    private val mutableRssEntry = MutableLiveData<RssEntry>()
    val rssEntry: LiveData<RssEntry> = mutableRssEntry

    fun setRssEntry(rssEntry: RssEntry) {
        mutableRssEntry.value = rssEntry
    }
}