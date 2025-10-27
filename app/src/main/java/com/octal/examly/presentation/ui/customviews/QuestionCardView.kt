package com.octal.examly.presentation.ui.customviews

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.card.MaterialCardView
import com.octal.examly.R
import com.octal.examly.databinding.ViewQuestionCardBinding
import com.octal.examly.domain.model.Question
import com.octal.examly.domain.model.QuestionType

class QuestionCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val binding: ViewQuestionCardBinding

    init {
        binding = ViewQuestionCardBinding.inflate(LayoutInflater.from(context), this)
        setupCard()
    }

    private fun setupCard() {
        radius = resources.getDimension(R.dimen.corner_radius_medium)
        cardElevation = resources.getDimension(R.dimen.elevation_small)
    }

    fun setQuestion(question: Question, questionNumber: Int? = null) {
        binding.apply {
            if (questionNumber != null) {
                tvQuestionNumber.text = context.getString(R.string.question_number, questionNumber)
                tvQuestionNumber.visibility = android.view.View.VISIBLE
            } else {
                tvQuestionNumber.visibility = android.view.View.GONE
            }

            when (question.type) {
                QuestionType.SINGLE_CHOICE -> {
                    tvTypeIndicator.text = context.getString(R.string.single_choice)
                    tvTypeIndicator.setBackgroundResource(R.drawable.bg_badge_single_choice)
                }
                QuestionType.MULTIPLE_CHOICE -> {
                    tvTypeIndicator.text = context.getString(R.string.multiple_choice)
                    tvTypeIndicator.setBackgroundResource(R.drawable.bg_badge_multiple_choice)
                }
            }

            tvQuestionText.text = question.questionText

            if (question.imageUri != null) {
                ivQuestionImage.setImageURI(Uri.parse(question.imageUri))
                ivQuestionImage.visibility = android.view.View.VISIBLE
            } else {
                ivQuestionImage.visibility = android.view.View.GONE
            }

            tvSubjectBadge.visibility = android.view.View.GONE
        }
    }

    fun setSubjectName(subjectName: String?) {
        binding.tvSubjectBadge.apply {
            if (!subjectName.isNullOrEmpty()) {
                text = subjectName
                visibility = android.view.View.VISIBLE
            } else {
                visibility = android.view.View.GONE
            }
        }
    }
}
