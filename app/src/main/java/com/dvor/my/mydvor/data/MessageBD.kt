package com.dvor.my.mydvor.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class MessageBD(
        var income: Int?,
        var text: String?,
        var date: String?
)