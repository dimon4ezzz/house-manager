package com.dvor.my.mydvor.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

/**
 * Support object to get Auth info from the Firebase.
 *
 * Works with FirebaseAuth.
 * @see com.google.firebase.auth.FirebaseAuth
 */
object Auth {
    /**
     * FirebaseAuth instance.
     */
    internal val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private var listener: FirebaseAuth.AuthStateListener? = null

    /**
     * Checks if current user is present.
     *
     * @return `true` when user is logged in
     */
    fun isLoggedIn(): Boolean =
            auth.currentUser != null

    /**
     * Checks if user is not logged in, and throws.
     *
     * @throws IllegalAccessException when user is not logged in
     * @see isLoggedIn
     */
    fun checkUserLoginAndThrow() {
        if (!isLoggedIn())
            throw IllegalAccessException("user is not logged in")
    }

    /**
     * Gets current user id from Firebase and returns it.
     *
     * @return id of current user from Firebase (uid)
     * @throws IllegalAccessException when user is not logged in
     */
    internal fun getCurrentUserId(): String {
        if (!isLoggedIn())
            throw IllegalAccessException("user is not logged in")

        return auth.currentUser!!.uid
    }

    /**
     * Calls `f` function with `loggedIn` state from Firebase.
     * Detaches last listener from auth.
     *
     * @param f function to call, e.g. moveToLogin
     * @sample listenAuthState(moveToLogin)
     */
    fun listenAuthState(f: (loggedIn: Boolean) -> Unit) {
        stopListenAuthState()

        listener = FirebaseAuth.AuthStateListener {
            f(isLoggedIn())
        }
        auth.addAuthStateListener(listener!!)
    }

    /**
     * Detaches listener from auth, set `listener` to `null`.
     */
    fun stopListenAuthState() {
        listener?.let {
            auth.removeAuthStateListener(listener!!)
        }

        listener = null
    }

    /**
     * Tries to sign-in to Firebase with email and password credentials.
     * If Firebase cancels authorization, it calls `actionOnFail`.
     * Writes to debug log Firebase exception message.
     *
     * @param email user email for signin
     * @param password user password for signin
     * @param actionOnFail function to call when auth fails
     */
    fun signIn(email: String, password: String, actionOnFail: () -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnFailureListener { exception ->
            Log.d("state", exception.message.toString())
            actionOnFail()
        }
    }

    /**
     * Sign out user from Firebase.
     */
    fun signOut() =
            auth.signOut()
}
