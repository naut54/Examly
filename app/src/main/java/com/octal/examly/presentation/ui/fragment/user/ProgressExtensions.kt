package com.octal.examly.presentation.ui.fragment.user

import com.google.android.material.progressindicator.LinearProgressIndicator

fun LinearProgressIndicator.setProgress(current: Int, total: Int) {
    val percent = if (total > 0) (current * 100 / total) else 0
    this.max = 100
    this.progress = percent
}