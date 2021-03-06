package com.lazureleming.rssreader.ui.main.rssentriesall

import androidx.fragment.app.activityViewModels
import com.lazureleming.rssreader.ui.main.RssEntriesFragment

/**
 * [androidx.fragment.app.Fragment] representing view with new loaded RSS entries.
 * It differs from favourites fragment by used [androidx.lifecycle.ViewModel].
 */
class RssEntriesAllFragment : RssEntriesFragment() {
    override val viewModel: RssEntriesAllViewModel by activityViewModels()
}
