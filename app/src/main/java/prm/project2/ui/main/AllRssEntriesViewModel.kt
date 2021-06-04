package prm.project2.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import prm.project2.rssentries.RssEntry
import prm.project2.rssentries.RssEntryImg

class AllRssEntriesViewModel : ViewModel() {

    private val mutableEntries = MutableLiveData<List<RssEntryImg>>()

    val entries: LiveData<List<RssEntryImg>> = Transformations.map(mutableEntries) {
        it
    }

//    fun setEntries(entries: String) {
//        mutableEntries.value = entries
//    }

    fun setEntries(entries: List<RssEntryImg>) {
        mutableEntries.value = entries
    }
}