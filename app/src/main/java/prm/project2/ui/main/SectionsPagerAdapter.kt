package prm.project2.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import prm.project2.R.string.tab_title_all
import prm.project2.R.string.tab_title_favourites
import prm.project2.ui.main.rssentries.rssentriesall.RssEntriesAllFragment
import prm.project2.ui.main.rssentries.rssentriesfavourites.RssEntriesFavouritesFragment

class SectionsPagerAdapter(private val context: Context, fragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(fragmentManager) {

    val allRssEntries by lazy { RssEntriesAllFragment() }
    val favouriteRssEntries by lazy { RssEntriesFavouritesFragment() }
    private val pages by lazy {
        arrayOf(
            Pair(tab_title_all, allRssEntries),
            Pair(tab_title_favourites, favouriteRssEntries)
        )
    }

    override fun getItem(position: Int): Fragment = pages[position].second

    override fun getPageTitle(position: Int): String = context.getString(pages[position].first)

    override fun getCount(): Int = pages.size
}
