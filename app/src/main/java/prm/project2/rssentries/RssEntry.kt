package prm.project2.rssentries

import android.graphics.Bitmap
import com.google.firebase.firestore.DocumentSnapshot
import java.time.LocalDateTime

private const val GUID = "GUID"
private const val TITLE = "TITLE"
private const val LINK = "LINK"
private const val DESCRIPTION = "DESCRIPTION"
private const val DATE = "DATE"
private const val IMAGE_URL = "IMAGE_URL"
private const val FAVOURITE = "FAVOURITE"
private const val READ = "READ"

data class RssEntry(
    val guid: String,
    val title: String?,
    val link: String?,
    val description: String?,
    val date: LocalDateTime?,
    val imageUrl: String?,
    var image: Bitmap? = null,
    var favourite: Boolean = false,
    var read: Boolean = false
) {
    fun firebaseEntry(): HashMap<String, String?> {
        return if (favourite) fullEntry() else guidEntry()
    }

    private fun fullEntry(): HashMap<String, String?> {
        return hashMapOf(
            GUID to guid,
            TITLE to title,
            LINK to link,
            DESCRIPTION to description,
            DATE to date?.toString(),
            IMAGE_URL to imageUrl,
            FAVOURITE to favourite.toString(),
            READ to read.toString()
        )
    }

    private fun guidEntry(): HashMap<String, String?> {
        return hashMapOf(
            GUID to guid
        )
    }

    fun firestoreDocumentName(): String {
        return "${guid.replace("/", "_")}-${hashCode()}"
    }

    fun equals(guid: String): Boolean {
        return this.guid == guid
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RssEntry
        if (guid != other.guid) return false
        return true
    }

    override fun hashCode(): Int {
        return guid.hashCode()
    }
}

fun DocumentSnapshot.toRssEntry(): RssEntry = RssEntry(
    getString(GUID)!!, getString(TITLE), getString(LINK),
    getString(DESCRIPTION), getString(DATE)?.toLocalDateTime(), getString(IMAGE_URL), null,
    getString(FAVOURITE).toBoolean(), getString(READ)?.toBoolean() ?: true
)

fun List<RssEntry>.getEntry(rssEntry: RssEntry): RssEntry? = getOrNull(indexOf(rssEntry))

private fun String.toLocalDateTime(): LocalDateTime = LocalDateTime.parse(this)
