package com.lazureleming.rssreader.ui.login

import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import com.lazureleming.rssreader.R.string.email_sending_error
import com.lazureleming.rssreader.R.string.verify_email_popup
import com.lazureleming.rssreader.ui.CommonFragment
import com.lazureleming.rssreader.utils.Firebase.firebaseAuth
import com.lazureleming.rssreader.utils.Firebase.firebaseUser

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