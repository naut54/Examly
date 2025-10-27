package com.octal.examly.presentation.ui.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import com.google.android.material.card.MaterialCardView
import com.octal.examly.R
import com.octal.examly.databinding.ViewMetricCardBinding

class MetricCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val binding: ViewMetricCardBinding =
        ViewMetricCardBinding.inflate(LayoutInflater.from(context), this)

    init {
        setupCard()
    }

    private fun setupCard() {
        radius = resources.getDimension(R.dimen.corner_radius_large)
        cardElevation = resources.getDimension(R.dimen.elevation_small)
    }

    fun setMetric(
        @DrawableRes iconRes: Int,
        title: String,
        value: String,
        subtitle: String? = null,
        progress: Int? = null,
        breakdown: String? = null
    ) {
        binding.apply {
            ivIcon.setImageResource(iconRes)

            tvTitle.text = title

            tvValue.text = value

            if (subtitle != null) {
                tvSubtitle.text = subtitle
                tvSubtitle.visibility = android.view.View.VISIBLE
            } else {
                tvSubtitle.visibility = android.view.View.GONE
            }

            if (progress != null) {
                progressBar.progress = progress
                progressBar.visibility = android.view.View.VISIBLE
            } else {
                progressBar.visibility = android.view.View.GONE
            }

            if (breakdown != null) {
                tvBreakdown.text = breakdown
                tvBreakdown.visibility = android.view.View.VISIBLE
            } else {
                tvBreakdown.visibility = android.view.View.GONE
            }
        }
    }

    fun setIconColor(color: Int) {
        binding.ivIcon.setColorFilter(context.getColor(color))
    }
}
