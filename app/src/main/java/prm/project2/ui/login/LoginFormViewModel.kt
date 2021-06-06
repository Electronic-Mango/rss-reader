package prm.project2.ui.login

import android.util.Patterns
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import prm.project2.Common.toText
import prm.project2.R

class LoginFormViewModel : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm
    var justSignedIn: Boolean = false
        get() = field.apply { field = false }

    fun loginDataChanged(email: EditText, password: EditText) = loginDataChanged(email.toText(), password.toText())

    private fun loginDataChanged(email: String, password: String) {
        if (!isEmailValid(email)) {
            _loginForm.value = LoginFormState(emailError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password, isEmailValid = true)
        } else {
            _loginForm.value = LoginFormState(isEmailValid = true, isPasswordValid = true)
        }
    }

    fun resetLoginData() {
        _loginForm.value = LoginFormState()
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5 && password.matches(Regex(".*\\d.*"))
    }
}