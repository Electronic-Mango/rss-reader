package prm.project2.ui.login

import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import prm.project2.FirebaseCommon
import prm.project2.R
import prm.project2.ui.CommonFragment

abstract class AbstractFragmentUserData : CommonFragment() {

    protected fun sendEmailVerification(callback: () -> Unit = { }) {
        FirebaseCommon.firebaseUser?.sendEmailVerification()?.addOnCompleteListener {
            val messageId = if (it.isSuccessful) R.string.verify_email_popup else R.string.verify_email_resend_error
            showSnackbar(messageId)
            FirebaseCommon.firebaseAuth.signOut()
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
}