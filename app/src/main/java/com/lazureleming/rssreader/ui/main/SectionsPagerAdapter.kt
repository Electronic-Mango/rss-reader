package com.lazureleming.rssreader.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.lazureleming.rssreader.R.string.tab_title_all
import com.lazureleming.rssreader.R.string.tab_title_favourites
import com.lazureleming.rssreader.ui.main.rssentriesall.RssEntriesAllFragment
import com.lazureleming.rssreader.ui.main.rssentriesfavourites.RssEntriesFavouritesFragment

/**
 * [androidx.fragment.app.FragmentStatePagerAdapter] handling tabs for new loaded RSS entries
 * [androidx.fragment.app.Fragment] and favourite entries [androidx.fragment.app.Fragment].
 */
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
