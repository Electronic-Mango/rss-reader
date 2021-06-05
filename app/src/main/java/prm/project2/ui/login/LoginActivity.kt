package prm.project2.ui.login

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.google.firebase.auth.AuthResult
import prm.project2.R
import prm.project2.databinding.ActivityLoginBinding
import prm.project2.ui.FirebaseUtils.firebaseAuth

class LoginActivity : AppCompatActivity() {

    private val loginFormViewModel: LoginFormViewModel by viewModels()
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var login: Button
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarLoginActivity)

        email = binding.email
        password = binding.password
        login = binding.login
        loading = binding.loading

        loginFormViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer
            login.isEnabled = loginState.isDataValid
            loginState.emailError?.let { email.error = getString(loginState.emailError) }
            loginState.passwordError?.let { password.error = getString(loginState.passwordError) }
        })

        email.afterTextChanged { loginFormViewModel.loginDataChanged(email.toText(), password.toText()) }

        password.apply {
            afterTextChanged { loginFormViewModel.loginDataChanged(email.toText(), password.toText()) }

            setOnEditorActionListener { _, actionId, _ ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        loginOrSignup()
                        true
                    }
                    else -> false
                }
            }

            login.setOnClickListener { loginOrSignup() }
        }
    }

    private fun loginOrSignup() {
        loading.visibility = View.VISIBLE
        firebaseAuth.signInWithEmailAndPassword(email.toText(), password.toText())
            .addOnCompleteListener(this::handleSignIn)
    }

    private fun handleSignIn(signInTask: Task<AuthResult>) {
        if (signInTask.isSuccessful) {
            loading.visibility = View.GONE
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            firebaseAuth.createUserWithEmailAndPassword(email.toText(), password.toText())
                .addOnCompleteListener(this::handleSignUp)
        }
    }

    private fun handleSignUp(signUpTask: Task<AuthResult>) {
        if (signUpTask.isSuccessful) {
            loading.visibility = View.GONE
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            loading.visibility = View.GONE
            Snackbar.make(binding.container, getString(R.string.login_failed), LENGTH_INDEFINITE).show()
        }
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}

private fun EditText.toText(): String = this.text.toString()