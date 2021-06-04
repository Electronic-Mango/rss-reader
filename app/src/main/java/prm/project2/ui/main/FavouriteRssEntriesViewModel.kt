package prm.project2.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class FavouriteRssEntriesViewModel : ViewModel() {

    private val mutableEntries = MutableLiveData<String>()

    val text: LiveData<String> = Transformations.map(mutableEntries) {
        it
    }

    fun setEntries(entries: String) {
        mutableEntries.value = entries
    }
}