package com.octal.examly.presentation.ui.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CompoundButton
import com.google.android.material.card.MaterialCardView
import com.octal.examly.R
import com.octal.examly.databinding.ViewAnswerOptionBinding
import com.octal.examly.domain.model.Answer
import com.octal.examly.domain.model.QuestionType
import androidx.core.view.isVisible

class AnswerOptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val binding: ViewAnswerOptionBinding
    private var onSelectionChanged: ((Boolean) -> Unit)? = null

    enum class AnswerState {
        NORMAL,
        SELECTED,
        CORRECT,
        INCORRECT,
        CORRECT_SELECTED,
        INCORRECT_SELECTED
    }

    init {
        binding = ViewAnswerOptionBinding.inflate(LayoutInflater.from(context), this)
        setupCard()
        setupListeners()
    }

    private fun setupCard() {
        radius = resources.getDimension(R.dimen.corner_radius_medium)
        cardElevation = resources.getDimension(R.dimen.elevation_small)
        isClickable = true
        isFocusable = true
    }

    private fun setupListeners() {
        setOnClickListener {
            toggleSelection()
        }

        binding.rbOption.setOnCheckedChangeListener { _, isChecked ->
            onSelectionChanged?.invoke(isChecked)
        }

        binding.cbOption.setOnCheckedChangeListener { _, isChecked ->
            onSelectionChanged?.invoke(isChecked)
        }
    }

    fun setAnswer(
        answer: Answer,
        questionType: QuestionType,
        state: AnswerState = AnswerState.NORMAL
    ) {
        binding.apply {
            tvAnswerText.text = answer.answerText

            when (questionType) {
                QuestionType.SINGLE_CHOICE -> {
                    rbOption.visibility = android.view.View.VISIBLE
                    cbOption.visibility = android.view.View.GONE
                }
                QuestionType.MULTIPLE_CHOICE -> {
                    rbOption.visibility = android.view.View.GONE
                    cbOption.visibility = android.view.View.VISIBLE
                }
            }

            setState(state)
        }
    }

    fun setState(state: AnswerState) {
        binding.apply {
            when (state) {
                AnswerState.NORMAL -> {
                    setCardBackgroundColor(context.getColor(R.color.surface))
                    ivStatusIcon.visibility = android.view.View.GONE
                    strokeWidth = 0
                }
                AnswerState.SELECTED -> {
                    setCardBackgroundColor(context.getColor(R.color.surface_variant))
                    ivStatusIcon.visibility = android.view.View.GONE
                    strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_width_medium)
                    strokeColor = context.getColor(R.color.primary)
                }
                AnswerState.CORRECT -> {
                    setCardBackgroundColor(context.getColor(R.color.success_container))
                    ivStatusIcon.setImageResource(R.drawable.ic_check)
                    ivStatusIcon.setColorFilter(context.getColor(R.color.success))
                    ivStatusIcon.visibility = android.view.View.VISIBLE
                    strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_width_medium)
                    strokeColor = context.getColor(R.color.success)
                }
                AnswerState.INCORRECT -> {
                    setCardBackgroundColor(context.getColor(R.color.error_container))
                    ivStatusIcon.setImageResource(R.drawable.ic_close)
                    ivStatusIcon.setColorFilter(context.getColor(R.color.error))
                    ivStatusIcon.visibility = android.view.View.VISIBLE
                    strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_width_medium)
                    strokeColor = context.getColor(R.color.error)
                }
                AnswerState.CORRECT_SELECTED -> {
                    setCardBackgroundColor(context.getColor(R.color.success_container))
                    ivStatusIcon.setImageResource(R.drawable.ic_check)
                    ivStatusIcon.setColorFilter(context.getColor(R.color.success))
                    ivStatusIcon.visibility = android.view.View.VISIBLE
                    strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_width_large)
                    strokeColor = context.getColor(R.color.success)
                }
                AnswerState.INCORRECT_SELECTED -> {
                    setCardBackgroundColor(context.getColor(R.color.error_container))
                    ivStatusIcon.setImageResource(R.drawable.ic_close)
                    ivStatusIcon.setColorFilter(context.getColor(R.color.error))
                    ivStatusIcon.visibility = android.view.View.VISIBLE
                    strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_width_large)
                    strokeColor = context.getColor(R.color.error)
                }
            }
        }
    }

    private fun toggleSelection() {
        when {
            binding.rbOption.isVisible -> {
                binding.rbOption.isChecked = !binding.rbOption.isChecked
            }
            binding.cbOption.isVisible -> {
                binding.cbOption.isChecked = !binding.cbOption.isChecked
            }
        }
    }

    fun isAnswerSelected(): Boolean {
        return binding.rbOption.isChecked || binding.cbOption.isChecked
    }

    fun setAnswerSelected(selected: Boolean) {
        binding.rbOption.isChecked = selected
        binding.cbOption.isChecked = selected
    }

    fun setOnSelectionChangedListener(listener: (Boolean) -> Unit) {
        onSelectionChanged = listener
    }
}
