package com.dvor.my.mydvor.firebase

import com.dvor.my.mydvor.data.User
import com.google.firebase.database.*

/**
 * Support object to get User from the Firebase database.
 *
 * Works with `users` branch.
 */
object UsersBranchDao {
    /**
     * Constant names in the database.
     */
    private const val users = "users"

    /**
     * Database reference.
     */
    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var usersBranch: DatabaseReference

    /**
     * Database value listener.
     */
    private var usersBranchListener: ValueEventListener? = null

    /**
     * Calls `f` function with user with `userId` from Firebase. Detaches last listener from branch.
     *
     * @param userId uid, e.g. from FirebaseAuth
     * @param f function to call, e.g. updateUI
     * @sample listenUsersBranch("Zya1L9AadzMXZfDkih2zjWpkq0I2", updateUI)
     * @throws com.google.firebase.database.DatabaseException
     *  when `users/userId` branch does not exists,
     *  or Firebase cancels request
     */
    fun listenUsersBranch(userId: String, f: (user: User) -> Unit) {
        stopListenUsersBranch()
        checkUserLogin()

        usersBranch = usersBranch.child(userId)
        usersBranchListener = object : ValueEventListener {
            // when data changes, call `f` with generated user from snapshot
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                f(getUser(dataSnapshot))
            }

            // when Firebase cancels request, throws DatabaseException
            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }
        }

        usersBranch.addValueEventListener(usersBranchListener!!)
    }

    /**
     * Removes listener from `users` branch. Set `usersBranch` to default path, `listener` to `null`.
     */
    private fun stopListenUsersBranch() {
        usersBranchListener?.let {
            usersBranch.removeEventListener(usersBranchListener!!)
        }

        usersBranch = database.child(users)
        usersBranchListener = null
    }

    /**
     * Checks if user is not logged in, and throws.
     *
     * @throws IllegalAccessException when user is not logged in
     * @see Auth.isLoggedIn
     */
    private fun checkUserLogin() {
        if (!Auth.isLoggedIn())
            throw IllegalAccessException("user is not logged in")
    }

    /**
     * Gets user data from database.
     *
     * @return User POJO without `building` and `street` fields.
     */
    private fun getUser(dataSnapshot: DataSnapshot): User =
            User(
                    name = dataSnapshot.child("name").value.toString(),
                    surname = dataSnapshot.child("surname").value.toString(),
                    apartment = dataSnapshot.child("apartment").value.toString(),
                    building_id = dataSnapshot.child("building_id").value.toString(),
                    street_id = dataSnapshot.child("street_id").value.toString(),

                    building = null,
                    street = null
            )
}
