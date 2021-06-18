package com.lazureleming.rssreader.rssentries

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import com.google.firebase.firestore.DocumentSnapshot
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import kotlin.reflect.KClass

const val GUID = "GUID"
const val TITLE = "TITLE"
const val LINK = "LINK"
const val DESCRIPTION = "DESCRIPTION"
const val DATE = "DATE"
const val ENCLOSURES = "ENCLOSURES"
const val URL = "URL"
const val LENGTH = "LENGTH"
const val TYPE = "TYPE"
const val FAVOURITE = "FAVOURITE"
const val READ = "READ"

/**
 * Data class storing information about enclosures (images data) for [com.lazureleming.rssreader.rssentries.RssEntry].
 */
data class Enclosure(
    val url: String,
    val length: Long?,
    val type: String
) : Serializable {
    fun firestoreEntity(): HashMap<String, Any?> = hashMapOf(URL to url, LENGTH to length, TYPE to type)
}

/**
 * Class storing information about a single RSS entry.
 */
class RssEntry(
    val guid: String,
    val title: String?,
    val link: String?,
    val description: String?,
    val date: LocalDateTime?,
    private val enclosures: List<Enclosure> = ArrayList(),
    var image: Bitmap? = null,
    var favourite: Boolean = false,
    var read: Boolean = false
) {
    fun getSmallestImageUrl(): String? {
        return enclosures.minByOrNull { it.length ?: 0 }?.url
    }

    fun getLargestImageUrl(): String? {
        return enclosures.maxByOrNull { it.length ?: 0 }?.url
    }

    fun firestoreDocumentName(): String {
        return "${guid.replace("/", "_")}-${hashCode()}"
    }

    fun firestoreEntry(): HashMap<String, Any?> {
        return if (favourite) fullFirestoreEntry() else guidFirestoreEntry()
    }

    private fun fullFirestoreEntry(): HashMap<String, Any?> {
        return hashMapOf(
            GUID to guid,
            TITLE to title,
            LINK to link,
            DESCRIPTION to description,
            DATE to date?.toString(),
            ENCLOSURES to enclosures.map { it.firestoreEntity() },
            FAVOURITE to favourite,
            READ to read
        )
    }

    private fun guidFirestoreEntry(): HashMap<String, Any?> {
        return hashMapOf(
            GUID to guid
        )
    }

    fun toIntent(packageContext: Context, kClass: KClass<*>): Intent = Intent(packageContext, kClass.java).apply {
        putExtra(GUID, guid)
        putExtra(TITLE, title)
        putExtra(LINK, link)
        putExtra(DESCRIPTION, description)
        putExtra(DATE, date)
        putExtra(ENCLOSURES, enclosures.toTypedArray())
        putExtra(FAVOURITE, favourite)
        putExtra(READ, read)
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

fun DocumentSnapshot.toRssEntry(): RssEntry {
    val enclosuresEntries = get(ENCLOSURES) as List<*>? ?: ArrayList<Enclosure>()
    val enclosures = enclosuresEntries
        .filterIsInstance<HashMap<*, *>>()
        .map { Enclosure(it[URL] as String, it[LENGTH] as Long?, it[TYPE] as String) }
    return RssEntry(
        getString(GUID)!!, getString(TITLE), getString(LINK), getString(DESCRIPTION), getLocalDateTime(DATE),
        enclosures, favourite = getBoolean(FAVOURITE) ?: false, read = getBoolean(READ) ?: true
    )
}

fun String.toLocalDateTime(): LocalDateTime? = tryParse(this)

fun List<RssEntry>.getEntry(rssEntry: RssEntry): RssEntry? = getOrNull(indexOf(rssEntry))

fun Intent.toRssEntry(read: Boolean = true): RssEntry? {
    val guid = getStringExtra(GUID) ?: return null
    val title = getStringExtra(TITLE)
    val link = getStringExtra(LINK)
    val description = getStringExtra(DESCRIPTION)
    val date = getSerializableExtra(DATE) as LocalDateTime
    val favourite = getBooleanExtra(FAVOURITE, false)
    val enclosures = (getSerializableExtra(ENCLOSURES) as Array<*>).filterIsInstance<Enclosure>().toList()
    return RssEntry(guid, title, link, description, date, enclosures, favourite = favourite, read = read)
}

private fun DocumentSnapshot.getLocalDateTime(field: String): LocalDateTime? = getString(field)?.toLocalDateTime()

private fun tryParse(text: String): LocalDateTime? {
    return try {
        LocalDateTime.parse(text)
    } catch (_: DateTimeParseException) {
        null
    }
}
