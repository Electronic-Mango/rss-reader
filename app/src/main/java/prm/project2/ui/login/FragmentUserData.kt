package prm.project2.ui.login

import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import prm.project2.R.string.email_sending_error
import prm.project2.R.string.verify_email_popup
import prm.project2.ui.CommonFragment
import prm.project2.utils.Firebase.firebaseAuth
import prm.project2.utils.Firebase.firebaseUser

/**
 * Common [androidx.fragment.app.Fragment] for fragments with email and password inputs -
 * [FragmentLogin] and [FragmentSignup].
 */
abstract class FragmentUserData : CommonFragment() {

    protected fun sendEmailVerification(callback: () -> Unit = { }) {
        firebaseUser?.sendEmailVerification()?.addOnCompleteListener {
            val messageId = if (it.isSuccessful) verify_email_popup else email_sending_error
            showSnackbar(messageId)
            firebaseAuth.signOut()
            callback()
        }
    }

    protected fun onEditorActionListenerAction(operation: () -> Unit): OnEditorActionListener {
        return OnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    operation()
                    true
                }
                else -> false
            }
        }
    }

    protected fun EditText.showSoftKeyboard() {
        (context?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(this, SHOW_IMPLICIT)
    }
}