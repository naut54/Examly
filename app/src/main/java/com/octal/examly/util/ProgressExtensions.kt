package com.octal.examly.utils

import com.google.android.material.progressindicator.LinearProgressIndicator

fun LinearProgressIndicator.setProgress(current: Int, total: Int) {
    this.max = total
    this.progress = current
}
