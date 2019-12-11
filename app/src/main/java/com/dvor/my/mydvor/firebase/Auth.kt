package com.dvor.my.mydvor.firebase

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
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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
}
