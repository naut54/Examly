package com.octal.examly.utils

import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateTimeFormatter @Inject constructor() {

    private val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())
    private val timeFormat = SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat(Constants.DATETIME_FORMAT, Locale.getDefault())

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    fun formatDuration(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return when {
            hours > 0 -> {
                val remainingMinutes = minutes % 60
                if (remainingMinutes > 0) {
                    "$hours h $remainingMinutes min"
                } else {
                    "$hours h"
                }
            }
            minutes > 0 -> {
                val remainingSeconds = seconds % 60
                if (remainingSeconds > 0) {
                    "$minutes min $remainingSeconds seg"
                } else {
                    "$minutes min"
                }
            }
            else -> "$seconds seg"
        }
    }

    fun formatCountdown(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Hace un momento"
            minutes < 60 -> "Hace $minutes min"
            hours < 24 -> "Hace $hours h"
            days == 1L -> "Ayer"
            days < 7 -> "Hace $days días"
            else -> formatDate(timestamp)
        }
    }

    fun parseDate(dateString: String, pattern: String = Constants.DATE_FORMAT): Long? {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.parse(dateString)?.time
        } catch (e: Exception) {
            null
        }
    }

    fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)

        calendar.timeInMillis = timestamp
        val dateToCheck = calendar.get(Calendar.DAY_OF_YEAR)

        return today == dateToCheck
    }

    fun isPast(timestamp: Long): Boolean {
        return timestamp < System.currentTimeMillis()
    }

    fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}
