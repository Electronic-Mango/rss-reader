package com.lazureleming.rssreader.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.lazureleming.rssreader.R.id.from_signup_to_login
import com.lazureleming.rssreader.R.string.*
import com.lazureleming.rssreader.databinding.FragmentSignupBinding
import com.lazureleming.rssreader.utils.Firebase.createUserWithEmailAndPassword
import com.lazureleming.rssreader.utils.Firebase.firebaseAuth

/**
 * [androidx.fragment.app.Fragment] allowing creating new account via email and password.
 */
class FragmentSignup : FragmentUserData() {

    private val userdataFormViewModel: UserdataFormViewModel by activityViewModels()
    private lateinit var binding: FragmentSignupBinding
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var signup: Button
    private lateinit var loadingSnackbar: Snackbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        userdataFormViewModel.resetLoginData()
        binding = FragmentSignupBinding.inflate(inflater, container, false)

        email = binding.emailSignup
        password = binding.passwordSignup
        signup = binding.signup

        userdataFormViewModel.userdataFormState.observe(viewLifecycleOwner) {
            val loginState = it ?: return@observe
            signup.isEnabled = loginState.isDataValid
            loginState.emailErrorMessage?.let { errorMessage -> email.error = getString(errorMessage) }
            loginState.passwordErrorMessage?.let { errorMessage -> password.error = getString(errorMessage) }
        }

        email.afterTextChanged { userdataFormViewModel.loginDataChanged(email, password) }
        password.apply {
            afterTextChanged { userdataFormViewModel.loginDataChanged(email, password) }
            setOnEditorActionListener(onEditorActionListenerAction { signup() })
        }

        signup.setOnClickListener { signup() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingSnackbar = showUserDataLoadingSnackbar()
        email.requestFocus()
        email.showSoftKeyboard()
    }

    private fun signup() {
        loadingSnackbar.show()
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this::handleCreateUser)
    }

    private fun handleCreateUser(signInTask: Task<AuthResult>) {
        if (signInTask.isSuccessful) {
            sendEmailVerification {
                userdataFormViewModel.justSignedIn = true
                findNavController().navigate(from_signup_to_login)
                userdataFormViewModel.resetLoginData()
                email.text.clear()
                password.text.clear()
            }
        } else {
            showSnackbar(signup_failed)
        }
    }
}