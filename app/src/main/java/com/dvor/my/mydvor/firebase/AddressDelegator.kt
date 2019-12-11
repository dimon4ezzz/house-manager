package com.dvor.my.mydvor.firebase

import com.dvor.my.mydvor.data.User

/**
 * Support object to get full address of user from Firebase database.
 *
 * Works with `streets` and `streets/id/buildings` branches.
 */
object AddressDelegator {
    /**
     * Calls `f` function with street with `street_id` and building with `building_id` from Firebase.
     *
     * @param user user to complete
     * @sample listenAddress(user, updateUI)
     * @throws com.google.firebase.database.DatabaseException
     *  when `streets/id` branch is not exist,
     *  or `streets/id/buildings/buildingId` branch is not exist,
     *  or Firebase cancels request
     * @throws IllegalAccessException
     *  when user is not logged in
     */
    fun listenAddress(user: User, f: (user: User) -> Unit) {
        stopListenAddress()

        StreetsBranchDao.listenStreetsBranch(user.street_id) { street ->
            user.street = street.name

            BuildingsBranchDao.listenBuildingsBranch(user.street_id, user.building_id) { building ->
                user.building = building.number

                f(user)
            }
        }
    }

    /**
     * Detaches listeners from `streets` and `streets/id/buildings` branch.
     */
    fun stopListenAddress() {
        StreetsBranchDao.stopListenStreetsBranch()
        BuildingsBranchDao.stopListenBuildingsBranch()
    }
}