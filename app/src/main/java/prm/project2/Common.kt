package prm.project2

import android.app.Activity
import android.view.View
import android.widget.EditText
import com.google.android.material.snackbar.BaseTransientBottomBar.Behavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import prm.project2.rssentries.RssEntry

private val CANNOT_DISMISS_SNACKBAR_BEHAVIOR = object : Behavior() {
    override fun canSwipeDismissView(child: View): Boolean = false
}

object Common {

    const val POLAND_COUNTRY_CODE = "PL"

    var RSS_ENTRY_TO_SHOW: RssEntry? = null

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
}
