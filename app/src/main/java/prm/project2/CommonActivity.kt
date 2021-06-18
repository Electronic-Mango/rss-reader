package prm.project2

import android.app.NotificationManager
import android.content.Intent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import prm.project2.R.string.*
import prm.project2.rssentries.RssEntry
import prm.project2.utils.Common
import prm.project2.utils.Firebase.firestoreData
import kotlin.concurrent.thread

/**
 * Common abstract class for Activities used throughout the app.
 */
abstract class CommonActivity : AppCompatActivity() {

    abstract val snackbarView: View
    protected val notificationManager: NotificationManager
        get() = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

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