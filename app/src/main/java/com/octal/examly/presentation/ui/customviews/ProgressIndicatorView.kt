package com.octal.examly.presentation.ui.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.octal.examly.R
import com.octal.examly.databinding.ViewProgressIndicatorBinding

class ProgressIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewProgressIndicatorBinding

    init {
        binding = ViewProgressIndicatorBinding.inflate(LayoutInflater.from(context), this, true)
        orientation = VERTICAL
    }

    fun setProgress(current: Int, total: Int) {
        binding.apply {
            tvProgressText.text = context.getString(
                R.string.question_progress,
                current,
                total
            )

            progressBar.max = total
            progressBar.progress = current

            val percentage = if (total > 0) {
                (current.toFloat() / total * 100).toInt()
            } else {
                0
            }
            tvProgressPercentage.text = context.getString(
                R.string.progress_percentage,
                percentage
            )
        }
    }
}
