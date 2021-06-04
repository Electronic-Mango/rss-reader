package prm.project2.rssentries

import java.time.LocalDateTime

data class RssEntry(val guid: String?, val title: String?, val link: String?, val description: String?, val date: LocalDateTime?, val enclosure: String?) {
    override fun toString(): String = "RSS Entry={guid=[$guid] title=[$title] link=[$link] description=[$description] date=[${date.toString()}] enclosure=[$enclosure]}"
}




