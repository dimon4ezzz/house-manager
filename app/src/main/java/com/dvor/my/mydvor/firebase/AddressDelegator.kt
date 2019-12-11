package com.dvor.my.mydvor.firebase

import com.dvor.my.mydvor.data.Address

/**
 * Support object to get full address of user from Firebase database.
 *
 * Works with `streets` and `streets/id/buildings` branches.
 */
object AddressDelegator {
    /**
     * Calls `f` function with street with `streetId` and building with `buildingId` from Firebase.
     *
     * @param streetId id of the street
     * @param buildingId id of the building
     * @param apartment user apartment
     * @sample listenAddress("1", "0", "12", updateUI)
     * @throws com.google.firebase.database.DatabaseException
     *  when `streets/streetId` branch is not exist,
     *  or `streets/id/buildings/buildingId` branch is not exist,
     *  or Firebase cancels request
     * @throws IllegalAccessException
     *  when user is not logged in
     */
    fun listenAddress(streetId: String, buildingId: String, apartment: String, f: (address: Address) -> Unit) {
        val address = Address("", "", apartment)
        stopListenAddress()

        StreetsBranchDao.listenStreetsBranch(streetId) { street ->
            address.street = street.name

            BuildingsBranchDao.listenBuildingsBranch(streetId, buildingId) { building ->
                address.building = building.number

                f(address)
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