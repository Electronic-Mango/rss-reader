package prm.project2.utils

import android.app.Activity
import android.view.View
import android.widget.EditText
import com.google.android.material.snackbar.BaseTransientBottomBar.Behavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG

private val CANNOT_DISMISS_SNACKBAR_BEHAVIOR = object : Behavior() {
    override fun canSwipeDismissView(child: View): Boolean = false
}

object Common {

    const val SHARED_PREFERENCES_LOCATION = "shared-preferences"
    const val POLAND_COUNTRY_CODE = "PL"
    const val NOTIFICATION_CHANNEL_ID = "CHANNEL_ID"
    const val INTENT_FROM_NOTIFICATION = "INTENT_FROM_NOTIFICATION"
    const val LATEST_LOADED_RSS_ENTRY = "LATEST_LOADED_RSS_ENTRY"
    const val RSS_SOURCE = "RSS_SOURCE"

    fun showIndefiniteSnackbar(view: View, message: String, show: Boolean = true): Snackbar {
        return Snackbar.make(view, message, LENGTH_INDEFINITE).apply {
            behavior = CANNOT_DISMISS_SNACKBAR_BEHAVIOR
            if (show) show()
        }
    }

    fun showSnackbar(view: View, messageId: Int, activity: Activity): Snackbar {
        return showSnackbar(view, activity.getString(messageId))
    }

    fun showSnackbar(view: View, message: String): Snackbar {
        return Snackbar.make(view, message, LENGTH_LONG).apply { show() }
    }

    fun EditText.toText(): String = this.text.toString()

    fun String?.isNotBlankNorNull(): Boolean = this?.isNotBlank() ?: false
}
