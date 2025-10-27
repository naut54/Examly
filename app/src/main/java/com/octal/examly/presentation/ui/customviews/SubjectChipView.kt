package com.octal.examly.presentation.ui.customviews

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.octal.examly.R
import com.octal.examly.domain.model.Subject

class SubjectChipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.chipStyle
) : Chip(context, attrs, defStyleAttr) {

    init {
        setupChip()
    }

    private fun setupChip() {
        setChipBackgroundColorResource(R.color.surface_variant)
        setTextColor(ContextCompat.getColor(context, R.color.on_surface))
        chipCornerRadius = resources.getDimension(R.dimen.corner_radius_small)
    }

    fun setSubject(subject: Subject, checkable: Boolean = false) {
        text = subject.name
        isCheckable = checkable

        val colorRes = getSubjectColor(subject.id)
        setChipBackgroundColorResource(colorRes)
    }

    private fun getSubjectColor(subjectId: Long): Int {
        val colors = listOf(
            R.color.subject_color_1,
            R.color.subject_color_2,
            R.color.subject_color_3,
            R.color.subject_color_4,
            R.color.subject_color_5,
            R.color.subject_color_6
        )
        return colors[(subjectId % colors.size).toInt()]
    }

    fun setSubjectColor(colorRes: Int) {
        setChipBackgroundColorResource(colorRes)
    }
}
