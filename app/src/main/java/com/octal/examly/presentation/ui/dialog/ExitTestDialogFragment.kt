package com.octal.examly.presentation.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.octal.examly.R
import com.octal.examly.databinding.DialogExitTestBinding

class ExitTestDialogFragment : DialogFragment() {

    private var _binding: DialogExitTestBinding? = null
    private val binding get() = _binding!!

    private var onActionSelected: ((ExitTestAction) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogExitTestBinding.inflate(layoutInflater)

        val currentQuestion = arguments?.getInt(ARG_CURRENT_QUESTION, 0) ?: 0
        val totalQuestions = arguments?.getInt(ARG_TOTAL_QUESTIONS, 0) ?: 0
        val answeredQuestions = arguments?.getInt(ARG_ANSWERED_QUESTIONS, 0) ?: 0
        val hasTimer = arguments?.getBoolean(ARG_HAS_TIMER, false) ?: false
        val timeRemaining = arguments?.getLong(ARG_TIME_REMAINING, 0) ?: 0

        setupUI(currentQuestion, totalQuestions, answeredQuestions, hasTimer, timeRemaining)

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setCancelable(true)
            .create()
    }

    private fun setupUI(
        currentQuestion: Int,
        totalQuestions: Int,
        answeredQuestions: Int,
        hasTimer: Boolean,
        timeRemaining: Long
    ) {
        binding.ivWarningIcon.setImageResource(R.drawable.ic_warning)
        binding.tvTitle.text = getString(R.string.exit_test)

        binding.tvCurrentProgress.text = getString(
            R.string.current_progress,
            currentQuestion,
            totalQuestions
        )

        binding.tvAnsweredCount.text = getString(
            R.string.answered_questions,
            answeredQuestions,
            totalQuestions
        )

        val unanswered = totalQuestions - answeredQuestions
        binding.tvUnansweredCount.text = getString(
            R.string.unanswered_questions,
            unanswered
        )

        binding.progressBar.max = totalQuestions
        binding.progressBar.progress = answeredQuestions

        if (hasTimer && timeRemaining > 0) {
            binding.layoutTimer.visibility = android.view.View.VISIBLE
            binding.tvTimeRemaining.text = getString(
                R.string.time_remaining,
                formatTime(timeRemaining)
            )
        } else {
            binding.layoutTimer.visibility = android.view.View.GONE
        }

        binding.tvWarningMessage.text = getString(R.string.exit_test_warning_message)

        binding.btnSaveAndExit.setOnClickListener {
            onActionSelected?.invoke(ExitTestAction.SAVE_AND_EXIT)
            dismiss()
        }

        binding.btnExitWithoutSaving.setOnClickListener {
            onActionSelected?.invoke(ExitTestAction.EXIT_WITHOUT_SAVING)
            dismiss()
        }

        binding.btnStay.setOnClickListener {
            onActionSelected?.invoke(ExitTestAction.CANCEL)
            dismiss()
        }
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        onActionSelected = null
    }

    enum class ExitTestAction {
        SAVE_AND_EXIT,
        EXIT_WITHOUT_SAVING,
        CANCEL
    }

    companion object {
        private const val ARG_CURRENT_QUESTION = "current_question"
        private const val ARG_TOTAL_QUESTIONS = "total_questions"
        private const val ARG_ANSWERED_QUESTIONS = "answered_questions"
        private const val ARG_HAS_TIMER = "has_timer"
        private const val ARG_TIME_REMAINING = "time_remaining"

        fun newInstance(
            currentQuestion: Int,
            totalQuestions: Int,
            answeredQuestions: Int,
            hasTimer: Boolean = false,
            timeRemaining: Long = 0,
            onAction: (ExitTestAction) -> Unit
        ): ExitTestDialogFragment {
            return ExitTestDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_CURRENT_QUESTION, currentQuestion)
                    putInt(ARG_TOTAL_QUESTIONS, totalQuestions)
                    putInt(ARG_ANSWERED_QUESTIONS, answeredQuestions)
                    putBoolean(ARG_HAS_TIMER, hasTimer)
                    putLong(ARG_TIME_REMAINING, timeRemaining)
                }
                this.onActionSelected = onAction
            }
        }
    }
}
