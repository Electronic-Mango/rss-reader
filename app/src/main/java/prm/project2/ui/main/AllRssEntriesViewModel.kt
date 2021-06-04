package prm.project2.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import prm.project2.rssentries.RssEntry

class AllRssEntriesViewModel : ViewModel() {
    private val mutableEntries = MutableLiveData<List<RssEntry>>()
    val entries: LiveData<List<RssEntry>> = mutableEntries
    private val mutableEntryToDisplay = MutableLiveData<RssEntry>()
    val entryToDisplay: LiveData<RssEntry> = mutableEntryToDisplay

    fun setEntries(entries: List<RssEntry>) {
        mutableEntries.value = entries
    }

    fun showEntry(entry: RssEntry) {
        mutableEntryToDisplay.value = entry
    }

    fun getEntry(guid: String?): RssEntry? {
        return entries.value?.stream()?.filter {
            it.guid == guid
        }?.findFirst()?.orElseGet { null }
    }
}