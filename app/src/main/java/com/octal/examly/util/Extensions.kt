package com.octal.examly.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Long.toDate(pattern: String = Constants.DATE_FORMAT): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toTime(pattern: String = Constants.TIME_FORMAT): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toDateTime(pattern: String = Constants.DATETIME_FORMAT): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun String.toDate(pattern: String = Constants.DATE_FORMAT): Long? {
    return try {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        sdf.parse(this)?.time
    } catch (e: Exception) {
        null
    }
}

@SuppressLint("DefaultLocale")
fun Long.toTimeString(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60

    return when {
        hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%02d:%02d", minutes, seconds)
    }
}

fun Int.minutesToMillis(): Long = this * 60 * 1000L

fun Long.millisToMinutes(): Int = (this / 1000 / 60).toInt()

fun Long.millisToSeconds(): Int = (this / 1000).toInt()

fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

fun Double.toPercentage(): String {
    return String.format("%.2f%%", this)
}

fun Int.toOrdinal(): String {
    return when (this % 10) {
        1 -> if (this % 100 == 11) "${this}º" else "${this}º"
        2 -> if (this % 100 == 12) "${this}º" else "${this}º"
        3 -> if (this % 100 == 13) "${this}º" else "${this}º"
        else -> "${this}º"
    }
}

fun <T> List<T>.randomOrNull(): T? {
    return if (isEmpty()) null else random()
}

fun <T> List<T>.shuffled(): List<T> {
    return this.shuffled(Random())
}