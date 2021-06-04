package prm.project2.ui.main.rssentries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import prm.project2.rssentries.RssEntry

abstract class RssEntriesViewModel : ViewModel() {
    private val mutableEntries = MutableLiveData<List<RssEntry>>()
    val entries: LiveData<List<RssEntry>> = mutableEntries

    private val mutableEntryToDisplay = MutableLiveData<RssEntry>()
    val entryToDisplay: LiveData<RssEntry> = mutableEntryToDisplay

    private val mutableEntryToToggleFavourite = MutableLiveData<RssEntry>()
    val entryToToggleFavourite = mutableEntryToToggleFavourite

    init {
        setEntries(ArrayList())
    }

    fun setEntries(entries: List<RssEntry>) {
        mutableEntries.value = entries
    }

    fun showEntry(entry: RssEntry) {
        mutableEntryToDisplay.value = entry
    }

    protected fun getEntry(guid: String?): RssEntry? {
        return entries.value?.stream()?.filter {
            it.guid == guid
        }?.findFirst()?.orElseGet { null }
    }

    fun toggleFavourite(rssEntry: RssEntry): Boolean {
        mutableEntryToToggleFavourite.value = rssEntry
        return true
    }

    protected fun refreshEntries() {
        entries.value?.let{ setEntries(it) }
    }
}