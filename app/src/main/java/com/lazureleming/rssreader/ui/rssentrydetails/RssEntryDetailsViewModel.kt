package com.lazureleming.rssreader.ui.rssentrydetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lazureleming.rssreader.rssentries.RssEntry

/**
 * [ViewModel] storing data representing full detailed information for RSS entry
 * used in [com.lazureleming.rssreader.RssEntryDetailsActivity] activity.
 */
class RssEntryDetailsViewModel : ViewModel() {

    private val mutableRssEntry = MutableLiveData<RssEntry>()
    val rssEntry: LiveData<RssEntry> = mutableRssEntry

    fun setRssEntry(rssEntry: RssEntry) {
        mutableRssEntry.value = rssEntry
    }
}