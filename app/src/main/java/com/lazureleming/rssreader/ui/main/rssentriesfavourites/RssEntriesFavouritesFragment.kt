package com.lazureleming.rssreader.ui.main.rssentriesfavourites

import androidx.fragment.app.activityViewModels
import com.lazureleming.rssreader.ui.main.RssEntriesFragment

/**
 * [androidx.fragment.app.Fragment] representing a list of users favourited RSS entries.
 * It differs from new entries fragment by used [androidx.lifecycle.ViewModel].
 */
class RssEntriesFavouritesFragment : RssEntriesFragment() {
    override val viewModel: RssEntriesFavouritesViewModel by activityViewModels()
}
