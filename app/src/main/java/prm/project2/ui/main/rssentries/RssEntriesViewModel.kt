package prm.project2.ui.main.rssentries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import prm.project2.rssentries.RssEntry

open class RssEntriesViewModel : ViewModel() {
    private val mutableEntries = MutableLiveData<List<RssEntry>>().apply { value = ArrayList() }
    val entries: LiveData<List<RssEntry>> = mutableEntries

    private val mutableEntryToDisplay = MutableLiveData<RssEntry>()
    val entryToDisplay: LiveData<RssEntry> = mutableEntryToDisplay

    private val mutableEntryToToggleFavourite = MutableLiveData<RssEntry>()
    val entryToToggleFavourite = mutableEntryToToggleFavourite

    fun setEntries(entries: List<RssEntry>) {
        mutableEntries.value = entries
    }

    fun showEntry(entry: RssEntry) {
        mutableEntryToDisplay.value = entry
    }

    protected fun entryExists(rssEntry: RssEntry): Boolean = entries.value?.contains(rssEntry) ?: false

    fun toggleFavourite(rssEntry: RssEntry): Boolean {
        mutableEntryToToggleFavourite.value = rssEntry
        return true
    }

    fun refreshEntries() = entries.value?.let { setEntries(it) }
}
