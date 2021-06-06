package prm.project2.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import prm.project2.FirebaseCommon
import prm.project2.FirebaseCommon.createUserWithEmailAndPassword
import prm.project2.R
import prm.project2.databinding.FragmentSignupBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class FragmentSignup : AbstractFragmentUserData() {

    private val loginFormViewModel: LoginFormViewModel by activityViewModels()
    private lateinit var binding: FragmentSignupBinding
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var signup: Button
    private lateinit var loadingSnackbar: Snackbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        loginFormViewModel.resetLoginData()
        binding = FragmentSignupBinding.inflate(inflater, container, false)

        email = binding.emailSignup
        password = binding.passwordSignup
        signup = binding.signup

        loginFormViewModel.loginFormState.observe(viewLifecycleOwner) {
            val loginState = it ?: return@observe
            signup.isEnabled = loginState.isDataValid
            loginState.emailError?.let { email.error = getString(loginState.emailError) }
            loginState.passwordError?.let { password.error = getString(loginState.passwordError) }
        }
        email.afterTextChanged { loginFormViewModel.loginDataChanged(email, password) }
        password.apply {
            afterTextChanged { loginFormViewModel.loginDataChanged(email, password) }
            setOnEditorActionListener(onEditorActionListenerAction { signup() })
        }
        signup.setOnClickListener { signup() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingSnackbar = showIndefiniteSnackbar(R.string.info_loading_user_data, false)
    }

    private fun signup() {
        loadingSnackbar.show()
        FirebaseCommon.firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this::handleCreateUser)
    }

    private fun handleCreateUser(signInTask: Task<AuthResult>) {
        if (signInTask.isSuccessful) {
            sendEmailVerification {
                loginFormViewModel.justSignedIn = true
                findNavController().navigate(R.id.from_signup_to_login)
                loginFormViewModel.resetLoginData()
                email.text.clear()
                password.text.clear()
            }
        } else {
            showSnackbar(R.string.signup_failed)
        }
    }
}