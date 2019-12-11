package com.dvor.my.mydvor.firebase

import com.dvor.my.mydvor.data.User
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/**
 * Support to register users.
 */
object Registration {
    /**
     * Registrates users; calls 3 methods on user collision, weak password and other failures.
     */
    fun registrate(email: String, password: String, user: User, failedUserCollision: () -> Unit, failedWeakPassword: () -> Unit, failed: () -> Unit) {
        Auth.auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            UsersBranchDao.addUser(user)
        }.addOnFailureListener { exception ->
            when (exception) {
                is FirebaseAuthUserCollisionException -> failedUserCollision()
                is FirebaseAuthWeakPasswordException -> failedWeakPassword()
                else -> failed()
            }
        }
    }
}