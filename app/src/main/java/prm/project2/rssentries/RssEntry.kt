package prm.project2.rssentries

import android.graphics.Bitmap
import java.time.LocalDateTime

data class RssEntry(
    val guid: String?,
    val title: String?,
    val link: String?,
    val description: String?,
    val date: LocalDateTime?,
    val imageUrl: String?,
    var image: Bitmap? = null,
    var favourite: Boolean = false,
    var read: Boolean = false
)
