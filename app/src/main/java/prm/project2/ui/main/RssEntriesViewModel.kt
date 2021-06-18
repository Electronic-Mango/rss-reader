package prm.project2.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import prm.project2.rssentries.RssEntry

/**
 * [ViewModel] storing data about RSS entries.
 * Used as common parent for [ViewModel] storing new loaded entries, or favourite ones.
 */
abstract class RssEntriesViewModel : ViewModel() {

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

    protected fun getEntry(guid: String): RssEntry? = entries.value?.firstOrNull { it.guid == guid }

    protected fun entryExists(rssEntry: RssEntry): Boolean = entries.value?.contains(rssEntry) ?: false

    fun toggleFavourite(rssEntry: RssEntry): Boolean {
        mutableEntryToToggleFavourite.value = rssEntry
        return true
    }

    fun refreshEntries() = entries.value?.let { setEntries(it) }
}
