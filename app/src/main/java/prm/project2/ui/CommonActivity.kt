package prm.project2.ui

import android.content.Intent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import prm.project2.Common

abstract class CommonActivity : AppCompatActivity() {

    abstract val snackbarView: View

    protected fun showIndefiniteSnackbar(messageId: Int, show: Boolean = true): Snackbar {
        return Common.showIndefiniteSnackbar(snackbarView, getString(messageId), show)
    }

    protected fun showIndefiniteSnackbar(message: String, show: Boolean = true): Snackbar {
        return Common.showIndefiniteSnackbar(snackbarView, message, show)
    }

    protected fun showSnackbar(message: String): Snackbar {
        return Common.showSnackbar(snackbarView, message)
    }

    protected fun showSnackbar(messageId: Int): Snackbar {
        return Common.showSnackbar(snackbarView, messageId, this)
    }

    protected fun registerForActivityResult(callback: (ActivityResult) -> Unit): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { callback(it) }
    }
}