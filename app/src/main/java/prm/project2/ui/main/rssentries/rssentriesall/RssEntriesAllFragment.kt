package prm.project2.ui.main.rssentries.rssentriesall

import androidx.fragment.app.activityViewModels
import prm.project2.ui.main.rssentries.RssEntriesFragment
import prm.project2.ui.main.rssentries.RssEntriesViewModel

class RssEntriesAllFragment : RssEntriesFragment() {
    private val viewModel: RssEntriesAllViewModel by activityViewModels()
    override fun viewModel(): RssEntriesViewModel = viewModel
}
