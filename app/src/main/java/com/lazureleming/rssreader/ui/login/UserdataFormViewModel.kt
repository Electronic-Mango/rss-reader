package com.lazureleming.rssreader.ui.login

import android.util.Patterns
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lazureleming.rssreader.R.string.invalid_email
import com.lazureleming.rssreader.R.string.invalid_password
import com.lazureleming.rssreader.utils.Common.toText

/**
 * [ViewModel] storing infromation about entered email and password during login or signup.
 * Notifies relevant fragments whether entered data is structurally correct.
 */
class UserdataFormViewModel : ViewModel() {

    private val _userdataForm = MutableLiveData<UserdataFormState>()
    val userdataFormState: LiveData<UserdataFormState> = _userdataForm
    var justSignedIn: Boolean = false
        get() = field.apply { field = false }

    fun loginDataChanged(email: EditText, password: EditText) = loginDataChanged(email.toText(), password.toText())

    private fun loginDataChanged(email: String, password: String) {
        val emailErrorMessage = if (!isEmailValid(email) && email.isNotEmpty()) invalid_email else null
        val passwordErrorMessage = if (!isPasswordValid(password) && password.isNotEmpty()) invalid_password else null
        _userdataForm.value =
            UserdataFormState(isEmailValid(email), emailErrorMessage, isPasswordValid(password), passwordErrorMessage)
    }

    fun resetLoginData() {
        _userdataForm.value = UserdataFormState()
    }

    private fun isEmailValid(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun isPasswordValid(password: String): Boolean = password.length >= 6
}