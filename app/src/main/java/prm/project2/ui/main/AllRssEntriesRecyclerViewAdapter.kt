package prm.project2.ui.main

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import prm.project2.databinding.FragmentRssEntryBinding
import prm.project2.rssentries.RssEntry
import prm.project2.ui.main.AllRssEntriesRecyclerViewAdapter.RssEntryViewHolder

class AllRssEntriesRecyclerViewAdapter(val viewModel: AllRssEntriesViewModel, private val rssEntries: List<RssEntry>) :
    Adapter<RssEntryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RssEntryViewHolder {
        return RssEntryViewHolder(FragmentRssEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RssEntryViewHolder, position: Int) {
        rssEntries[position].let { holder.bind(it) }
    }

    override fun getItemCount(): Int = rssEntries.size

    inner class RssEntryViewHolder(binding: FragmentRssEntryBinding) : ViewHolder(binding.root) {
        private val rssEntryLayout: ConstraintLayout = binding.rssEntry
        private val title: TextView = binding.rssEntryTitle
        private val shortDescription: TextView = binding.rssEntryShortDescription
        private val image: ImageView = binding.rssEntryImage

        fun bind(rssEntry: RssEntry) {
            title.text = rssEntry.title
            shortDescription.text = rssEntry.description ?: ""
            rssEntry.image?.square()?.let { image.setImageBitmap(it) }
            rssEntryLayout.setOnClickListener { viewModel.showEntry(rssEntry) }
        }

        private fun Bitmap.square() = createBitmap(this, width / 2 - height / 2, 0, height, height)
    }
}