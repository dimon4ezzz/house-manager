package com.dvor.my.mydvor.firebase

import com.dvor.my.mydvor.data.Street
import com.google.firebase.database.*

/**
 * Support object to get Street from the Firebase database.
 *
 * Works with `streets` branch.
 */
object StreetsBranchDao {
    /**
     * Constant name in the database.
     */
    private const val streets = "streets"

    /**
     * Database reference.
     */
    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var streetsBranch: DatabaseReference

    /**
     * Database value listener.
     */
    private var streetsBranchListener: ValueEventListener? = null

    /**
     * Calls `f` function with street with `streetId` from Firebase. Detaches last listener from branch.
     *
     * @param streetId id of the street
     * @param f function to call, e.g. updateUI
     * @sample listenStreetsBranch("0", updateUI)
     * @throws com.google.firebase.database.DatabaseException
     *  when `streets/streetId` branch is not exist,
     *  or Firebase cancels request
     * @throws IllegalAccessException
     *  when user is not logged in
     */
    fun listenStreetsBranch(streetId: String, f: (street: Street) -> Unit) {
        stopListenStreetsBranch()
        Auth.checkUserLoginAndThrow()

        streetsBranch = streetsBranch.child(streetId)
        streetsBranchListener = object : ValueEventListener {
            // when data changes, call `f` with generated street from snapshot
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                f(getStreet(dataSnapshot))
            }

            // when Firebase cancels request, throws DatabaseException
            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }
        }
    }

    /**
     * Detaches listener from `streets` branch.
     *
     * Set `streetsBranch` to default path, `listener` to `null`.
     */
    fun stopListenStreetsBranch() {
        streetsBranchListener?.let {
            streetsBranch.removeEventListener(streetsBranchListener!!)
        }

        streetsBranch = database.child(streets)
        streetsBranchListener = null
    }

    /**
     * Gets street data from database.
     *
     * @return Street POJO
     * @throws com.google.firebase.database.DatabaseException
     *  when cannot find fields in the branch
     */
    private fun getStreet(dataSnapshot: DataSnapshot): Street =
            Street(
                    id = dataSnapshot.key.toString(),
                    name = dataSnapshot.child("name").value.toString()
            )
}