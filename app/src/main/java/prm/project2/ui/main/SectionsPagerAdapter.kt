package prm.project2.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import prm.project2.R
import prm.project2.ui.main.rssentries.rssentriesall.RssEntriesAllFragment
import prm.project2.ui.main.rssentries.rssentriesfavourites.RssEntriesFavouritesFragment

class SectionsPagerAdapter(private val context: Context, fragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(fragmentManager) {

    private val allRssEntries by lazy { RssEntriesAllFragment() }
    private val favouriteRssEntries by lazy { RssEntriesFavouritesFragment() }
    private val pages by lazy {
        arrayOf(
            Pair(R.string.tab_title_all, allRssEntries),
            Pair(R.string.tab_title_favourites, favouriteRssEntries)
        )
    }

    override fun getItem(position: Int): Fragment = pages[position].second

    override fun getPageTitle(position: Int): String = context.getString(pages[position].first)

    override fun getCount(): Int = pages.size
}
