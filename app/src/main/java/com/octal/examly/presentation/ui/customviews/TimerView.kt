package com.octal.examly.presentation.ui.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.octal.examly.R
import com.octal.examly.databinding.ViewTimerBinding

class TimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewTimerBinding
    private var isWarningState = false

    init {
        binding = ViewTimerBinding.inflate(LayoutInflater.from(context), this, true)
        orientation = HORIZONTAL
        setupView()
    }

    private fun setupView() {
        binding.ivTimerIcon.setImageResource(R.drawable.ic_timer)
    }

    fun setTime(timeMillis: Long) {
        val totalSeconds = timeMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        binding.tvTimeRemaining.text = String.format("%02d:%02d", minutes, seconds)

        if (timeMillis < 300000 && !isWarningState) {
            setWarningState(true)
        } else if (timeMillis >= 300000 && isWarningState) {
            setWarningState(false)
        }
    }

    fun setWarningState(warning: Boolean) {
        isWarningState = warning
        if (warning) {
            binding.tvTimeRemaining.setTextColor(context.getColor(R.color.error))
            binding.ivTimerIcon.setColorFilter(context.getColor(R.color.error))
        } else {
            binding.tvTimeRemaining.setTextColor(context.getColor(R.color.on_surface))
            binding.ivTimerIcon.setColorFilter(context.getColor(R.color.on_surface_variant))
        }
    }
}
