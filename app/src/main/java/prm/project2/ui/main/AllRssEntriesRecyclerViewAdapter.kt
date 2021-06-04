package prm.project2.ui.main

import android.graphics.Bitmap.createBitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import prm.project2.databinding.FragmentRssEntryBinding
import prm.project2.rssentries.RssEntryImg
import prm.project2.ui.main.AllRssEntriesRecyclerViewAdapter.RssEntryViewHolder

class AllRssEntriesRecyclerViewAdapter(private val rssEntries: List<RssEntryImg>) : Adapter<RssEntryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RssEntryViewHolder {
        return RssEntryViewHolder(FragmentRssEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RssEntryViewHolder, position: Int) {
        val item = rssEntries[position]
        holder.title.text = item.title
        holder.shortDescription.text = item.description ?: ""
        item.image?.let {
            createBitmap(it, it.width / 2 - it.height / 2, 0, it.height, it.height).let { cropped_image ->
                holder.image.setImageBitmap(cropped_image)
            }
        }
    }

    override fun getItemCount(): Int = rssEntries.size

    inner class RssEntryViewHolder(binding: FragmentRssEntryBinding) : ViewHolder(binding.root) {
        val title: TextView = binding.rssEntryTitle
        val shortDescription: TextView = binding.rssEntryShortDescription
        val image: ImageView = binding.rssEntryImage
    }

}