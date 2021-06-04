package prm.project2.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import prm.project2.R

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(fragmentManager) {

    private val transactionsListFragment by lazy { AllRssEntriesFragment() }
    private val monthBalanceGraph by lazy { FavouriteRssEntriesFragment() }
    private val pages by lazy {
        arrayOf(
            Pair(R.string.tab_title_all, transactionsListFragment),
            Pair(R.string.tab_title_favourites, monthBalanceGraph)
        )
    }

    override fun getItem(position: Int): Fragment = pages[position].second

    override fun getPageTitle(position: Int): String = context.getString(pages[position].first)

    override fun getCount(): Int = pages.size
}