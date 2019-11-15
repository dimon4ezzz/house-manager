package com.dvor.my.mydvor.data

import com.google.firebase.database.IgnoreExtraProperties

/**
 * User is from database.
 */
@IgnoreExtraProperties
data class User(
        var apartment: String?,
        var building_id: String?,
        var name: String?,
        var surname: String?,
        var street_id: String?,
        var building: String?,
        var street: String?
)