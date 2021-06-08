package prm.project2.ui

import android.app.Activity
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import prm.project2.common.Common
import prm.project2.R.string.info_loading_user_data

abstract class CommonFragment : Fragment() {

    protected val activity: Activity
        get() = requireActivity()

    protected fun showUserDataLoadingSnackbar(): Snackbar {
        return Common.showIndefiniteSnackbar(requireView(), getString(info_loading_user_data), false)
    }

    protected fun showSnackbar(messageId: Int): Snackbar {
        return Common.showSnackbar(requireView(), getString(messageId))
    }

    protected fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    protected fun registerForActivityResult(callback: (ActivityResult) -> Unit): ActivityResultLauncher<Intent> {
        return registerForActivityResult(StartActivityForResult()) { callback(it) }
    }
}