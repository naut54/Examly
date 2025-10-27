package com.octal.examly.presentation.ui.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.card.MaterialCardView
import com.octal.examly.R
import com.octal.examly.databinding.ViewTestCardBinding
import com.octal.examly.domain.model.Test
import com.octal.examly.domain.model.TestAssignment
import com.octal.examly.domain.model.TestAssignmentStatus
import com.octal.examly.domain.model.TestAttemptStatus
import java.text.SimpleDateFormat
import java.util.*

class TestCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val binding: ViewTestCardBinding

    init {
        binding = ViewTestCardBinding.inflate(LayoutInflater.from(context), this)
        setupCard()
    }

    private fun setupCard() {
        radius = resources.getDimension(R.dimen.corner_radius_large)
        cardElevation = resources.getDimension(R.dimen.elevation_small)
        setCardBackgroundColor(context.getColor(R.color.surface))
        isClickable = true
        isFocusable = true
    }

    fun setTest(test: Test, subjectName: String? = null) {
        binding.apply {
            tvTestTitle.text = test.title

            if (test.description.isNotEmpty()) {
                tvTestDescription.text = test.description
                tvTestDescription.visibility = android.view.View.VISIBLE
            } else {
                tvTestDescription.visibility = android.view.View.GONE
            }

            if (!subjectName.isNullOrEmpty()) {
                tvSubject.text = subjectName
                tvSubject.visibility = android.view.View.VISIBLE
            } else {
                tvSubject.visibility = android.view.View.GONE
            }

            ivTestIcon.setImageResource(R.drawable.ic_test)

            tvQuestionCount.text = context.getString(
                R.string.questions_count,
                test.configuration.numberOfQuestions ?: 0
            )

            if (test.configuration.hasTimer && test.configuration.timeLimit != null) {
                tvTimeLimit.text = context.getString(
                    R.string.time_limit_minutes,
                    test.configuration.timeLimit
                )
                tvTimeLimit.visibility = android.view.View.VISIBLE
                ivTimerIcon.visibility = android.view.View.VISIBLE
            } else {
                tvTimeLimit.visibility = android.view.View.GONE
                ivTimerIcon.visibility = android.view.View.GONE
            }

            if (test.configuration.hasPracticeMode) {
                tvModeBadge.text = context.getString(R.string.practice_and_exam)
                tvModeBadge.setBackgroundResource(R.drawable.bg_badge_mixed)
            } else {
                tvModeBadge.text = context.getString(R.string.exam_only)
                tvModeBadge.setBackgroundResource(R.drawable.bg_badge_exam)
            }
            tvModeBadge.visibility = android.view.View.VISIBLE

            layoutDeadline.visibility = android.view.View.GONE
            tvStatusBadge.visibility = android.view.View.GONE
        }
    }

    fun setTestAssignment(assignment: TestAssignment, test: Test, subjectName: String? = null) {
        setTest(test, subjectName)

        binding.apply {
            when (assignment.status) {
                TestAssignmentStatus.PENDING -> {
                    tvStatusBadge.text = context.getString(R.string.pending)
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending)
                    tvStatusBadge.setTextColor(context.getColor(R.color.on_warning))
                }
                TestAssignmentStatus.IN_PROGRESS -> {
                    tvStatusBadge.text = context.getString(R.string.in_progress)
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_in_progress)
                    tvStatusBadge.setTextColor(context.getColor(R.color.on_info))
                }
                TestAssignmentStatus.COMPLETED -> {
                    tvStatusBadge.text = context.getString(R.string.completed)
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_completed)
                    tvStatusBadge.setTextColor(context.getColor(R.color.on_success))
                }
            }
            tvStatusBadge.visibility = android.view.View.VISIBLE

            if (assignment.deadline != null) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                tvDeadline.text = dateFormat.format(Date(assignment.deadline))

                val now = System.currentTimeMillis()
                when {
                    assignment.deadline < now -> {
                        tvDeadline.setTextColor(context.getColor(R.color.error))
                        ivDeadlineIcon.setColorFilter(context.getColor(R.color.error))
                    }
                    assignment.deadline - now < 24 * 60 * 60 * 1000 -> {
                        tvDeadline.setTextColor(context.getColor(R.color.warning))
                        ivDeadlineIcon.setColorFilter(context.getColor(R.color.warning))
                    }
                    else -> {
                        tvDeadline.setTextColor(context.getColor(R.color.on_surface))
                        ivDeadlineIcon.setColorFilter(context.getColor(R.color.on_surface_variant))
                    }
                }
                layoutDeadline.visibility = android.view.View.VISIBLE
            } else {
                layoutDeadline.visibility = android.view.View.GONE
            }
        }
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        super.setOnClickListener(listener)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = if (enabled) 1.0f else 0.5f
    }
}
