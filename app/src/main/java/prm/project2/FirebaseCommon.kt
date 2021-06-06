package prm.project2

import android.widget.EditText
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import prm.project2.Common.toText

object FirebaseCommon {
    val firebaseAuth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    val firebaseUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    fun FirebaseAuth.signInWithEmailAndPassword(email: EditText, password: EditText): Task<AuthResult> =
        signInWithEmailAndPassword(email.toText(), password.toText())

    fun FirebaseAuth.createUserWithEmailAndPassword(email: EditText, password: EditText): Task<AuthResult> =
        createUserWithEmailAndPassword(email.toText(), password.toText())
}