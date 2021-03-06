package com.lazureleming.rssreader.ui.rssentrydetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.lazureleming.rssreader.databinding.FragmentRssEntryDetailsBinding
import com.lazureleming.rssreader.rssentries.RssEntry
import com.lazureleming.rssreader.ui.CommonFragment
import com.lazureleming.rssreader.utils.RemoteResourcesLoader.loadBitmap
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")

/**
 * [androidx.fragment.app.Fragment] displaying details of RSS entry
 * in [com.lazureleming.rssreader.RssEntryDetailsActivity] activity.
 */
class RssEntryDetailsFragment : CommonFragment() {

    private val rssEntryDetailsViewModel: RssEntryDetailsViewModel by activityViewModels()
    private lateinit var binding: FragmentRssEntryDetailsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRssEntryDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rssEntryDetailsViewModel.rssEntry.observe(viewLifecycleOwner) {
            loadAndDisplayEntryImage(it)
            binding.rssEntryDetailsTitle.text = it.title ?: ""
            binding.rssEntryDetailsDate.text = it.date?.format(DATE_FORMATTER) ?: ""
            binding.rssEntryDetailsDescription.text = it.description ?: ""
            binding.rssEntryDetailsLink.text = it.link ?: ""
        }
    }

    private fun loadAndDisplayEntryImage(rssEntry: RssEntry) {
        thread {
            loadBitmap(rssEntry.getLargestImageUrl())?.let {
                activity.runOnUiThread { binding.rssEntryDetailsImage.setImageBitmap(it) }
            }
        }
    }
}