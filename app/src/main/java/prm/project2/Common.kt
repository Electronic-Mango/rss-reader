package prm.project2

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import android.widget.EditText
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG

object Common {
    const val DB_NAME = "read-rss-entries.db"

    const val POLAND_COUNTRY_CODE = "PL"

    const val INTENT_DATA_GUID = "INTENT_DATA_GUID"
    const val INTENT_DATA_TITLE = "INTENT_DATA_TITLE"
    const val INTENT_DATA_LINK = "INTENT_DATA_LINK"
    const val INTENT_DATA_DESCRIPTION = "INTENT_DATA_DESCRIPTION"
    const val INTENT_DATA_DATE = "INTENT_DATA_DATE"
    const val INTENT_DATA_FAVOURITE = "INTENT_DATA_FAVOURITE"
    var IMAGE_TO_SHOW: Bitmap? = null

    fun showIndefiniteSnackbar(view: View, message: String, show: Boolean = true): Snackbar {
        return Snackbar.make(view, message, LENGTH_INDEFINITE).apply {
            behavior = object : BaseTransientBottomBar.Behavior() {
                override fun canSwipeDismissView(child: View): Boolean = false
            }
            if (show) {
                show()
            }
        }
    }

    fun showSnackbar(view: View, messageId: Int, activity: Activity): Snackbar {
        return showSnackbar(view, activity.getString(messageId))
    }

    fun showSnackbar(view: View, message: String): Snackbar {
        return Snackbar.make(view, message, LENGTH_LONG).apply { show() }
    }

    fun EditText.toText(): String = this.text.toString()
}

open class Callback {
    open fun call() {}
}