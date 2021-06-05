package prm.project2

import android.graphics.Bitmap

object Common {
    const val DB_NAME = "read-rss-entries.db"

    const val INTENT_DATA_GUID = "INTENT_DATA_GUID"
    const val INTENT_DATA_TITLE = "INTENT_DATA_TITLE"
    const val INTENT_DATA_LINK = "INTENT_DATA_LINK"
    const val INTENT_DATA_DESCRIPTION = "INTENT_DATA_DESCRIPTION"
    const val INTENT_DATA_DATE = "INTENT_DATA_DATE"
    const val INTENT_DATA_FAVOURITE = "INTENT_DATA_FAVOURITE"
    var IMAGE_TO_SHOW: Bitmap? = null
}