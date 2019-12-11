package com.dvor.my.mydvor.firebase

import com.dvor.my.mydvor.data.Building
import com.google.firebase.database.*

/**
 * Support object to get Building from Firebase database.
 *
 * Works with `streets/id/buildings` branch.
 */
object BuildingsBranchDao {
    /**
     * Constant names in the database.
     */
    private const val streets = "streets"
    private const val buildings = "buildings"

    /**
     * Database reference.
     */
    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var buildingsBranch: DatabaseReference

    /**
     * Database value listener.
     */
    private var buildingsBranchListener: ValueEventListener? = null

    /**
     * Calls `f` function with street with `streetId` and building with `buildingId` from Firebase.
     * Detaches last listener from branch.
     *
     * @param streetId id of the street
     * @param buildingId id of the building
     * @param f function to call, e.g. updateUI
     * @sample listenBuildingsBranch("1", "0", updateUI)
     * @throws com.google.firebase.database.DatabaseException
     *  when `streets/streetId` or `./buildings/buildingId` is not exist,
     *  or Firebase cancels request
     */
    fun listenBuildingsBranch(streetId: String, buildingId: String, f: (building: Building) -> Unit) {
        stopListenBuildingsBranch()
        Auth.checkUserLoginAndThrow()

        buildingsBranch = buildingsBranch
                .child(streetId)
                .child(buildings)
                .child(buildingId)

        buildingsBranchListener = object : ValueEventListener {
            // when data changes, call `f` with generated street from snapshot
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                f(getBuilding(dataSnapshot))
            }

            // when Firebase cancels request, throws DatabaseException
            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }
        }
    }

    /**
     * Detaches listener from `buildings` branch.
     *
     * Set `buildings` branch to default path, `listener` to `null`.
     */
    fun stopListenBuildingsBranch() {
        buildingsBranchListener?.let {
            buildingsBranch.removeEventListener(buildingsBranchListener!!)
        }

        buildingsBranch = database.child(streets)
        buildingsBranchListener = null
    }

    /**
     * Gets building data from database.
     *
     * @return Building POJO
     * @throws com.google.firebase.database.DatabaseException
     *  when cannot find fields in the branch
     */
    private fun getBuilding(dataSnapshot: DataSnapshot): Building =
            Building(
                    id = dataSnapshot.key,
                    number = dataSnapshot.child("number").value.toString()
            )
}