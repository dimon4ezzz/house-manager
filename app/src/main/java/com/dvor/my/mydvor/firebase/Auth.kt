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
}
