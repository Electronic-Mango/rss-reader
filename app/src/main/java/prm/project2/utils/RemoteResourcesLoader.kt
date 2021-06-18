package prm.project2.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import prm.project2.rssentries.RssEntry
import prm.project2.rssentries.parseRssStream
import prm.project2.utils.Common.isNotBlankNorNull
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.net.UnknownServiceException

private const val LOADING_RSS_DATA_TAG = "MAIN-ACTIVITY-LOADING-RSS-DATA"
private const val BITMAP_LOADING_TAG = "MAIN-ACTIVITY-LOADING-IMG"

/**
 * Object responsible for loading remote (network) resources via their URLs.
 */
object RemoteResourcesLoader {

    fun tryToLoadAndParseRssData(
        rssLink: String,
        dataLoadedCallback: (List<RssEntry>) -> Unit,
        dataNotLoadedCallback: () -> Unit
    ): Boolean {
        return try {
            dataLoadedCallback(loadAndParseRssData(rssLink))
            true
        } catch (e: Exception) {
            Log.e(LOADING_RSS_DATA_TAG, "Exception encountered when loading RSS: ${e.stackTraceToString()}.")
            dataNotLoadedCallback()
            false
        }
    }

    @Throws(IOException::class, UnknownServiceException::class)
    private fun loadAndParseRssData(rssLink: String): List<RssEntry> {
        return URL(rssLink).openStream().parseRssStream().asSequence()
            .filter { it.guid.isNotBlank() }
            .filter { it.title.isNotBlankNorNull() }
            .sortedByDescending(RssEntry::date)
            .toList()
    }

    private fun InputStream.parseRssStream(): List<RssEntry> = parseRssStream(this)

    fun loadBitmap(url: String?): Bitmap? = url?.let {
        try {
            URL(it).openStream().let { urlStream -> BitmapFactory.decodeStream(urlStream) }
        } catch (e: MalformedURLException) {
            Log.e(BITMAP_LOADING_TAG, "Malformed URL $it: ${e.stackTraceToString()}")
            null
        } catch (e: IOException) {
            Log.e(BITMAP_LOADING_TAG, "I/O Exception when loading an image from $it: ${e.stackTraceToString()}")
            null
        }
    }
}