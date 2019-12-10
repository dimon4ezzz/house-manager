package com.dvor.my.mydvor.data

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
@Keep
data class NewsBD(
        var header: String?,
        var text: String?,
        var date: String?,
        var img: String?,
        var uid: String?
)