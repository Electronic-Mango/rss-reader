package com.lazureleming.rssreader.utils

import android.widget.EditText
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.lazureleming.rssreader.utils.Common.toText

/**
 * Helper object with properties and methods connected with <a href="https://firebase.google.com/">Firebase</a>
 * used throughout the app.
 */
object Firebase {
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