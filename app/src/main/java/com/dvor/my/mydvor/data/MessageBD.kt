package com.dvor.my.mydvor.data

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
@Keep
data class MessageBD(
        var income: Int?,
        var text: String?,
        var date: String?
)