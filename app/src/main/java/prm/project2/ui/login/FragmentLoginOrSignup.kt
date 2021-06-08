package prm.project2.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import prm.project2.CommonFirebase.firebaseAuth
import prm.project2.R.id.move_to_login
import prm.project2.R.id.move_to_signup
import prm.project2.R.string.default_web_client_id
import prm.project2.R.string.google_login_failed
import prm.project2.databinding.FragmentLoginOrSignupBinding
import prm.project2.ui.CommonFragment
import prm.project2.ui.main.MainActivity

class FragmentLoginOrSignup : CommonFragment() {

    private lateinit var binding: FragmentLoginOrSignupBinding
    private lateinit var googleLoginActivityResult: ActivityResultLauncher<Intent>
    private lateinit var googleSingInClient: GoogleSignInClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginOrSignupBinding.inflate(inflater, container, false)

        googleLoginActivityResult = registerForActivityResult { handleGoogleLoginResult(it) }
        googleSingInClient = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(default_web_client_id))
            .requestEmail()
            .build().let { GoogleSignIn.getClient(activity, it) }

        binding.moveToLogin.setOnClickListener { findNavController().navigate(move_to_login) }
        binding.moveToSignup.setOnClickListener { findNavController().navigate(move_to_signup) }
        binding.loginGoogle.setOnClickListener { googleLoginActivityResult.launch(googleSingInClient.signInIntent) }

        return binding.root
    }

    private fun handleGoogleLoginResult(activityResult: ActivityResult) {
        val signInTask = GoogleSignIn.getSignedInAccountFromIntent(activityResult.data)
        try {
            val account = signInTask.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken)
        } catch (exception: ApiException) {
            showSnackbar(google_login_failed)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(activity) {
            if (it.isSuccessful) {
                startActivity(Intent(context, MainActivity::class.java))
                activity.finish()
            } else {
                showSnackbar(google_login_failed)
            }
        }
    }
}