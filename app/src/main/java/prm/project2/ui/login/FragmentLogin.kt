package prm.project2.ui.login

import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import prm.project2.Common.toText
import prm.project2.FirebaseCommon.firebaseAuth
import prm.project2.FirebaseCommon.firebaseUser
import prm.project2.FirebaseCommon.signInWithEmailAndPassword
import prm.project2.R.string.*
import prm.project2.databinding.FragmentLoginBinding
import prm.project2.ui.main.MainActivity

class FragmentLogin : AbstractFragmentUserData() {

    private val loginFormViewModel: LoginFormViewModel by activityViewModels()
    private lateinit var binding: FragmentLoginBinding
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var login: Button
    private lateinit var resetPassword: Button
    private lateinit var resendEmailVerification: Button
    private lateinit var loadingSnackbar: Snackbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        loginFormViewModel.resetLoginData()
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        email = binding.emailLogin
        password = binding.passwordLogin
        login = binding.login
        resetPassword = binding.resetPassword
        resendEmailVerification = binding.resendEmailVerification
        resendEmailVerification.visibility = if (loginFormViewModel.justSignedIn) View.VISIBLE else View.GONE

        loginFormViewModel.loginFormState.observe(viewLifecycleOwner) {
            val loginState = it ?: return@observe
            login.isEnabled = loginState.isDataValid
            resetPassword.isEnabled = loginState.isEmailValid
            resendEmailVerification.isEnabled = loginState.isDataValid
            loginState.emailError?.let { email.error = getString(loginState.emailError) }
            loginState.passwordError?.let { password.error = getString(loginState.passwordError) }
        }
        email.afterTextChanged { loginFormViewModel.loginDataChanged(email, password) }
        password.apply {
            afterTextChanged { loginFormViewModel.loginDataChanged(email, password) }
            setOnEditorActionListener(onEditorActionListenerAction { login() })
        }
        login.setOnClickListener { login() }
        resetPassword.setOnClickListener { resetPassword() }
        resendEmailVerification.setOnClickListener { resendEmailVerification() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingSnackbar = showIndefiniteSnackbar(info_loading_user_data, false)
        email.requestFocus()
        email.showSoftKeyboard()
    }

    private fun login() {
        loadingSnackbar.show()
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this::handleLogin)
    }

    private fun handleLogin(signInTask: Task<AuthResult>) {
        if (signInTask.isSuccessful) {
            handleLoginSuccessful()
        } else {
            handleLoginFailed()
        }
    }

    private fun handleLoginSuccessful() {
        if (firebaseUser!!.isEmailVerified) {
            loadingSnackbar.dismiss()
            startActivity(Intent(context, MainActivity::class.java))
            activity.finish()
        } else {
            showSnackbar(email_not_verified).setAction(resend_verification_email_action) { sendEmailVerification() }
            firebaseAuth.signOut()
        }
    }

    private fun handleLoginFailed() {
        showSnackbar(login_failed).setAction(reset_password) { resetPassword() }
    }

    private fun resendEmailVerification() {
        loadingSnackbar.show()
        firebaseAuth.signInWithEmailAndPassword(email.toText(), password.toText())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    sendEmailVerification()
                } else {
                    handleLoginFailed()
                }
            }
    }

    private fun resetPassword() {
        firebaseAuth.sendPasswordResetEmail(email.toText()).addOnCompleteListener {
            if (it.isSuccessful) {
                showSnackbar(password_reset_email_sent)
            } else {
                showSnackbar(email_sending_error)
                    .setAction(getString(resent_password_reset_email_action)) { resetPassword() }
            }
        }
    }

    private fun EditText.showSoftKeyboard() {
        (context?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(email, SHOW_IMPLICIT)
    }
}