package prm.project2

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.widget.EditText
import com.google.android.material.snackbar.BaseTransientBottomBar.Behavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

private const val BITMAP_LOADING_TAG = "MAIN-ACTIVITY-LOADING-IMG"

private val CANNOT_DISMISS_SNACKBAR_BEHAVIOR = object : Behavior() {
    override fun canSwipeDismissView(child: View): Boolean = false
}

object Common {

    const val POLAND_COUNTRY_CODE = "PL"

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

    fun loadBitmap(url: String?): Bitmap? = url?.let {
        try {
            URL(url).openStream().let { BitmapFactory.decodeStream(it) }
        } catch (exception: MalformedURLException) {
            Log.e(BITMAP_LOADING_TAG, "Malformed URL $url!")
            null
        } catch (exception: IOException) {
            Log.e(BITMAP_LOADING_TAG, "I/O Exception when loading an image from $url!")
            null
        }
    }

    fun EditText.toText(): String = this.text.toString()
}
