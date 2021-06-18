package prm.project2.ui.main.rssentriesfavourites

import androidx.fragment.app.activityViewModels
import prm.project2.ui.main.RssEntriesFragment

/**
 * [androidx.fragment.app.Fragment] representing a list of users favourited RSS entries.
 * It differs from new entries fragment by used [androidx.lifecycle.ViewModel].
 */
class RssEntriesFavouritesFragment : RssEntriesFragment() {
    override val viewModel: RssEntriesFavouritesViewModel by activityViewModels()
}
