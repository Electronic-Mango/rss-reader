package com.lazureleming.rssreader.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.lazureleming.rssreader.MainActivity
import com.lazureleming.rssreader.R.string.*
import com.lazureleming.rssreader.databinding.FragmentLoginBinding
import com.lazureleming.rssreader.utils.Common.toText
import com.lazureleming.rssreader.utils.Firebase.firebaseAuth
import com.lazureleming.rssreader.utils.Firebase.firebaseUser
import com.lazureleming.rssreader.utils.Firebase.signInWithEmailAndPassword

/**
 * [androidx.fragment.app.Fragment] allowing login users via email and password.
 */
class FragmentLogin : FragmentUserData() {

    private val userdataFormViewModel: UserdataFormViewModel by activityViewModels()
    private lateinit var binding: FragmentLoginBinding
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var login: Button
    private lateinit var resetPassword: Button
    private lateinit var resendEmailVerification: Button
    private lateinit var loadingSnackbar: Snackbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        userdataFormViewModel.resetLoginData()
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        email = binding.emailLogin
        password = binding.passwordLogin
        login = binding.login
        resetPassword = binding.resetPassword
        resendEmailVerification = binding.resendEmailVerification
        resendEmailVerification.visibility = if (userdataFormViewModel.justSignedIn) View.VISIBLE else View.GONE

        userdataFormViewModel.userdataFormState.observe(viewLifecycleOwner) {
            val loginState = it ?: return@observe
            login.isEnabled = loginState.isDataValid
            resetPassword.isEnabled = loginState.isEmailValid
            resendEmailVerification.isEnabled = loginState.isDataValid
            loginState.emailErrorMessage?.let { errorMessage -> email.error = getString(errorMessage) }
            loginState.passwordErrorMessage?.let { errorMessage -> password.error = getString(errorMessage) }
        }

        email.afterTextChanged { userdataFormViewModel.loginDataChanged(email, password) }
        password.apply {
            afterTextChanged { userdataFormViewModel.loginDataChanged(email, password) }
            setOnEditorActionListener(onEditorActionListenerAction { login() })
        }

        login.setOnClickListener { login() }
        resetPassword.setOnClickListener { resetPassword() }
        resendEmailVerification.setOnClickListener { resendEmailVerification() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingSnackbar = showUserDataLoadingSnackbar()
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
            showSnackbar(email_not_verified).setAction(resend_verification_email_action) { resendEmailVerification() }
            firebaseAuth.signOut()
        }
    }

    private fun handleLoginFailed() {
        showSnackbar(login_failed).setAction(reset_password) { resetPassword() }
    }

    private fun resendEmailVerification() {
        loadingSnackbar.show()
        firebaseAuth.signInWithEmailAndPassword(email.toText(), password.toText()).addOnCompleteListener {
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
            }
        }
    }
}