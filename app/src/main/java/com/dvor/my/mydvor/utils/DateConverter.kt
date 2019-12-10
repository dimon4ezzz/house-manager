package com.dvor.my.mydvor.utils

import android.icu.text.SimpleDateFormat
import android.text.format.DateUtils
import java.text.ParseException
import java.util.*

/**
 * Small date converter.
 */
object DateConverter {
    @Deprecated("don't use old date format")
    private const val oldFormat = "EEE MMM dd HH:mm:ss zzzz yyyy"
    private const val format = "dd.MM.yy HH:mm:ss"

    /**
     * Converts usual date in string to relative date in string.
     *
     * @param date date in string, please use format: `dd.MM.yy HH:mm:ss`
     * @sample convert("31.12.19 23:59:59") become `tomorrow`, or `today`, or `Dec 31, 19`
     * @throws Exception when it cannot convert date
     */
    fun convert(date: String): String {
        val parsedDate: Date

        parsedDate = try {
            SimpleDateFormat(oldFormat, Locale.ENGLISH)
                    .parse(date)
        } catch (e: ParseException) {
            SimpleDateFormat(format)
                    .parse(date)
        } catch (e: Exception) {
            throw e
        }

        return DateUtils.getRelativeTimeSpanString(parsedDate.time).toString()
    }
}