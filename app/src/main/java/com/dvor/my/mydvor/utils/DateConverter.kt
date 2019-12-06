package com.dvor.my.mydvor.utils

import android.icu.text.SimpleDateFormat
import android.text.format.DateUtils
import java.util.*

object DateConverter {
    fun convert(date: String): String {
        val format = SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.ENGLISH)
        val parsedDate = format.parse(date)

        return DateUtils.getRelativeTimeSpanString(parsedDate.time).toString()
    }
}