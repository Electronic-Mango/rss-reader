package prm.project2.rssentries

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.time.LocalDateTime

data class RssEntry(val guid: String?, val title: String?, val link: String?, val description: String?, val date: LocalDateTime?, val imageUrl: String?) {
    override fun toString(): String = "RSS Entry={guid=[$guid] title=[$title] link=[$link] description=[$description] date=[$date] imageUrl=[$imageUrl]}"
}

data class RssEntryImg(val guid: String, val title: String, val link: String?, val description: String?, val date: LocalDateTime?, val image: Bitmap?) {
    companion object {
        fun newInstance(rssEntry: RssEntry): RssEntryImg {
            val image = rssEntry.imageUrl?.let { loadBitmap(it) }
            return RssEntryImg(rssEntry.guid!!, rssEntry.title!!, rssEntry.link, rssEntry.description, rssEntry.date, image)
        }

        private fun loadBitmap(url: String): Bitmap? {
            var bitmap: Bitmap? = null
            try {
                bitmap = URL(url).openStream().let { BitmapFactory.decodeStream(it) }
            } catch (exception: MalformedURLException) {
                Log.d("LOADING-IMG", "Malformed URL $url!")
            } catch (exception: IOException) {
                Log.d("LOADING-IMG", "I/O Exception when loading IMG!")
            }
            return bitmap
        }
    }
}



