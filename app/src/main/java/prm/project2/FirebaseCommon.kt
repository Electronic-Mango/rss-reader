package prm.project2

import android.widget.EditText
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import prm.project2.Common.toText

object FirebaseCommon {
    val firebaseAuth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    val firebaseUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    val firebaseUsername: String?
        get() = if (!firebaseUser?.displayName.isNullOrBlank()) firebaseUser?.displayName else firebaseUser?.email

    val firestoreData: CollectionReference
        get() = FirebaseFirestore.getInstance().collection(firebaseAuth.uid!!)

    fun FirebaseAuth.signInWithEmailAndPassword(email: EditText, password: EditText): Task<AuthResult> =
        signInWithEmailAndPassword(email.toText(), password.toText())

    fun FirebaseAuth.createUserWithEmailAndPassword(email: EditText, password: EditText): Task<AuthResult> =
        createUserWithEmailAndPassword(email.toText(), password.toText())
}