package prm.project2.ui.main.rssentries.rssentriesfavourites

import androidx.fragment.app.activityViewModels
import prm.project2.ui.main.rssentries.RssEntriesFragment
import prm.project2.ui.main.rssentries.RssEntriesViewModel

class RssEntriesFavouritesFragment : RssEntriesFragment() {
    private val viewModel: RssEntriesFavouritesViewModel by activityViewModels()
    override fun viewModel(): RssEntriesViewModel = viewModel
}