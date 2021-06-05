package prm.project2.rssentries

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val NAMESPACE: String? = null
private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME

@Throws(XmlPullParserException::class, IOException::class)
fun parseRssStream(inputStream: InputStream): List<RssEntry> {
    inputStream.use {
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(it, null)
        parser.nextTag()
        return readFeed(parser)
    }
}

@Throws(XmlPullParserException::class, IOException::class)
private fun readFeed(parser: XmlPullParser): List<RssEntry> {
    val entries = mutableListOf<RssEntry>()
    parser.require(XmlPullParser.START_TAG, NAMESPACE, "rss")
    while (parser.next() != XmlPullParser.END_TAG) {
        if (parser.eventType != XmlPullParser.START_TAG || parser.name == "channel") {
            continue
        }
        if (parser.name == "item") {
            entries.add(readEntry(parser))
        } else {
            skip(parser)
        }
    }
    return entries
}

@Throws(XmlPullParserException::class, IOException::class)
private fun readEntry(parser: XmlPullParser): RssEntry {
    parser.require(XmlPullParser.START_TAG, NAMESPACE, "item")
    var guid = ""
    var title = ""
    var link: String? = null
    var description: String? = null
    var date: LocalDateTime? = null
    var enclosure: String? = null
    while (parser.next() != XmlPullParser.END_TAG) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            continue
        }
        when (parser.name) {
            "guid" -> guid = readTextField(parser, "guid")
            "title" -> title = readTextField(parser, "title")
            "link" -> link = readTextField(parser, "link")
            "description" -> description = readTextField(parser, "description")
            "pubDate" -> date = LocalDateTime.parse(readTextField(parser, "pubDate"), DATE_FORMATTER)
            "enclosure" -> enclosure = readEnclosure(parser)
            else -> skip(parser)
        }
    }
    return RssEntry(guid, title, link, description, date, enclosure)
}

@Throws(IOException::class, XmlPullParserException::class)
private fun readTextField(parser: XmlPullParser, tagName: String): String {
    parser.require(XmlPullParser.START_TAG, NAMESPACE, tagName)
    val title = readText(parser)
    parser.require(XmlPullParser.END_TAG, NAMESPACE, tagName)
    return title
}

@Throws(IOException::class, XmlPullParserException::class)
private fun readEnclosure(parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, NAMESPACE, "enclosure")
    val url = parser.getAttributeValue(null, "url")
    parser.nextTag()
    parser.require(XmlPullParser.END_TAG, NAMESPACE, "enclosure")
    return url
}


@Throws(IOException::class, XmlPullParserException::class)
private fun readText(parser: XmlPullParser): String {
    var result = ""
    if (parser.next() == XmlPullParser.TEXT) {
        result = parser.text
        parser.nextTag()
    }
    return result
}

@Throws(XmlPullParserException::class, IOException::class)
private fun skip(parser: XmlPullParser) {
    if (parser.eventType != XmlPullParser.START_TAG) {
        throw IllegalStateException()
    }
    var depth = 1
    while (depth != 0) {
        when (parser.next()) {
            XmlPullParser.END_TAG -> depth--
            XmlPullParser.START_TAG -> depth++
        }
    }
}
