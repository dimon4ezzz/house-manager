package com.dvor.my.mydvor.utils

import android.icu.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DataConverter {
    fun convert(date: String): String {
        val convertedDate = ""
        val currentDate = Date()
        var postDate = Date()
        val format = SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.ENGLISH)
        try {
            postDate = format.parse(date)
        } catch (ignored: Exception) {
        }

        val minutes = postDate.minutes
        val minutesStr: String
        minutesStr = if (minutes < 10)
            "0$minutes"
        else
            minutes.toLong().toString()


        //если сообщение отправлено сегодня. Возвращаем время
        if (currentDate.year == postDate.year && currentDate.month == postDate.month
                && currentDate.day == postDate.day)

            return postDate.hours.toString() + ":" + minutesStr

        // вчера
        if (currentDate.year == postDate.year && currentDate.month == postDate.month
                && currentDate.day == postDate.day + 1)
            return "Вчера " + postDate.hours + ":" + minutesStr

        // раньше
        val cal = Calendar.getInstance()
        cal.time = postDate
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val year = cal.get(Calendar.YEAR)
        return "$day.$month.$year"
    }
}
