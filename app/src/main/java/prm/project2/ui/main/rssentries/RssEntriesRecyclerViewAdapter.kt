package prm.project2.ui.main.rssentries

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import prm.project2.databinding.FragmentRssEntryBinding
import prm.project2.rssentries.RssEntry
import prm.project2.ui.main.rssentries.RssEntriesRecyclerViewAdapter.RssEntryViewHolder

class RssEntriesRecyclerViewAdapter(val viewModel: RssEntriesViewModel, lifecycleOwner: LifecycleOwner) :
    Adapter<RssEntryViewHolder>() {

    private val rssEntries: MutableList<RssEntry> = ArrayList()

    init {
        viewModel.entries.observe(lifecycleOwner, {
            rssEntries.clear()
            rssEntries.addAll(it)
            notifyDataSetChanged()
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RssEntryViewHolder {
        return RssEntryViewHolder(FragmentRssEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RssEntryViewHolder, position: Int) {
        rssEntries[position].let { holder.bind(it) }
    }

    override fun getItemCount(): Int = rssEntries.size

    inner class RssEntryViewHolder(binding: FragmentRssEntryBinding) : ViewHolder(binding.root) {
        private val rssEntryLayout = binding.rssEntry
        private val title = binding.rssEntryTitle
        private val shortDescription = binding.rssEntryShortDescription
        private val image = binding.rssEntryImage
        private val favouriteMark = binding.favouriteMark
        private val defaultTextColors = binding.textColorMarker.textColors
        private val readTextColors = binding.textColorMarker.textColors.withAlpha(90)

        fun bind(rssEntry: RssEntry) {
            title.text = rssEntry.title
            shortDescription.text = rssEntry.description ?: ""
            rssEntry.image?.square()?.let { image.setImageBitmap(it) }
            setTextColor(rssEntry.read)
            favouriteMark.visibility = if (rssEntry.favourite) View.VISIBLE else View.INVISIBLE
            rssEntryLayout.setOnClickListener { viewModel.showEntry(rssEntry) }
            rssEntryLayout.setOnLongClickListener { viewModel.toggleFavourite(rssEntry) }
        }

        private fun setTextColor(isRead: Boolean) {
            val color = if (isRead) readTextColors else defaultTextColors
            title.setTextColor(color)
            shortDescription.setTextColor(color)
        }
    }
}

private fun Bitmap.square() = createBitmap(this, width / 2 - height / 2, 0, height, height)
