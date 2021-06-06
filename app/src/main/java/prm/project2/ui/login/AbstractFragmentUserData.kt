package prm.project2.ui.login

import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import prm.project2.FirebaseCommon
import prm.project2.R.string.email_sending_error
import prm.project2.R.string.verify_email_popup
import prm.project2.ui.CommonFragment

abstract class AbstractFragmentUserData : CommonFragment() {

    protected fun sendEmailVerification(callback: () -> Unit = { }) {
        FirebaseCommon.firebaseUser?.sendEmailVerification()?.addOnCompleteListener {
            val messageId = if (it.isSuccessful) verify_email_popup else email_sending_error
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