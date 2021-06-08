package prm.project2.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import prm.project2.Common
import prm.project2.FirebaseCommon.firestoreData
import prm.project2.R.string.*
import prm.project2.rssentries.RssEntry
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import kotlin.concurrent.thread

private const val BITMAP_LOADING_TAG = "MAIN-ACTIVITY-LOADING-IMG"

abstract class CommonActivity : AppCompatActivity() {

    abstract val snackbarView: View

    protected fun showIndefiniteSnackbar(messageId: Int, show: Boolean = true): Snackbar {
        return Common.showIndefiniteSnackbar(snackbarView, getString(messageId), show)
    }

    protected fun showIndefiniteSnackbar(message: String, show: Boolean = true): Snackbar {
        return Common.showIndefiniteSnackbar(snackbarView, message, show)
    }

    protected fun showSnackbar(messageId: Int): Snackbar {
        return Common.showSnackbar(snackbarView, messageId, this)
    }

    protected fun registerForActivityResult(callback: (ActivityResult) -> Unit): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { callback(it) }
    }

    protected fun loadBitmap(url: String?): Bitmap? = url?.let {
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

    protected fun addToFirestore(rssEntry: RssEntry) {
        thread {
            firestoreData.document(rssEntry.firestoreDocumentName()).set(rssEntry.firestoreEntry())
                .addOnFailureListener {
                    showSnackbar(firestore_insert_error).setAction(getString(repeat_operation)) {
                        addToFirestore(rssEntry)
                    }
                }
        }
    }

    protected fun removeFromFirestore(rssEntry: RssEntry) {
        thread {
            firestoreData.document(rssEntry.firestoreDocumentName()).delete().addOnFailureListener {
                showSnackbar(firestore_remove_error).setAction(getString(repeat_operation)) {
                    removeFromFirestore(rssEntry)
                }
            }
        }
    }
}