package com.octal.examly.presentation.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.octal.examly.R
import com.octal.examly.databinding.DialogSubmitTestBinding
import java.util.concurrent.TimeUnit

class SubmitTestDialogFragment : DialogFragment() {

    private var _binding: DialogSubmitTestBinding? = null
    private val binding get() = _binding!!

    private var onSubmitConfirmed: ((Boolean) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogSubmitTestBinding.inflate(layoutInflater)

        val totalQuestions = arguments?.getInt(ARG_TOTAL_QUESTIONS, 0) ?: 0
        val answeredQuestions = arguments?.getInt(ARG_ANSWERED_QUESTIONS, 0) ?: 0
        val timeSpent = arguments?.getLong(ARG_TIME_SPENT, 0) ?: 0
        val testMode = arguments?.getString(ARG_TEST_MODE) ?: ""
        val testTitle = arguments?.getString(ARG_TEST_TITLE) ?: ""

        setupUI(totalQuestions, answeredQuestions, timeSpent, testMode, testTitle)

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setCancelable(true)
            .create()
    }

    private fun setupUI(
        totalQuestions: Int,
        answeredQuestions: Int,
        timeSpent: Long,
        testMode: String,
        testTitle: String
    ) {
        binding.tvTitle.text = getString(R.string.submit_test)

        if (testTitle.isNotEmpty()) {
            binding.tvTestTitle.text = testTitle
            binding.tvTestTitle.visibility = android.view.View.VISIBLE
        } else {
            binding.tvTestTitle.visibility = android.view.View.GONE
        }

        if (testMode.isNotEmpty()) {
            binding.tvModeBadge.text = testMode
            binding.tvModeBadge.visibility = android.view.View.VISIBLE
        } else {
            binding.tvModeBadge.visibility = android.view.View.GONE
        }

        val unanswered = totalQuestions - answeredQuestions

        binding.tvTotalQuestions.text = getString(
            R.string.total_questions_count,
            totalQuestions
        )

        binding.tvAnsweredCount.text = getString(
            R.string.answered_count,
            answeredQuestions
        )

        binding.tvUnansweredCount.text = getString(
            R.string.unanswered_count,
            unanswered
        )

        binding.progressBar.max = totalQuestions
        binding.progressBar.progress = answeredQuestions

        val percentage = if (totalQuestions > 0) {
            (answeredQuestions.toFloat() / totalQuestions * 100).toInt()
        } else {
            0
        }
        binding.tvProgressPercentage.text = getString(
            R.string.progress_percentage,
            percentage
        )

        binding.tvTimeSpent.text = getString(
            R.string.time_spent_label,
            formatTime(timeSpent)
        )

        if (unanswered > 0) {
            binding.layoutWarning.visibility = android.view.View.VISIBLE
            binding.tvWarningMessage.text = getString(
                R.string.unanswered_questions_warning,
                unanswered
            )
            binding.ivWarningIcon.setImageResource(R.drawable.ic_warning)
        } else {
            binding.layoutWarning.visibility = android.view.View.GONE
        }

        binding.tvInfoMessage.text = getString(R.string.submit_test_info_message)

        binding.btnSubmit.setOnClickListener {
            onSubmitConfirmed?.invoke(true)
            dismiss()
        }

        binding.btnReview.setOnClickListener {
            onSubmitConfirmed?.invoke(false)
            dismiss()
        }

        if (unanswered > 0) {
            binding.btnSubmit.text = getString(R.string.submit_anyway)
        } else {
            binding.btnSubmit.text = getString(R.string.submit)
        }
    }

    private fun formatTime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

        return when {
            hours > 0 -> String.format("%dh %02dm %02ds", hours, minutes, seconds)
            minutes > 0 -> String.format("%dm %02ds", minutes, seconds)
            else -> String.format("%ds", seconds)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        onSubmitConfirmed = null
    }

    companion object {
        private const val ARG_TOTAL_QUESTIONS = "total_questions"
        private const val ARG_ANSWERED_QUESTIONS = "answered_questions"
        private const val ARG_TIME_SPENT = "time_spent"
        private const val ARG_TEST_MODE = "test_mode"
        private const val ARG_TEST_TITLE = "test_title"

        fun newInstance(
            totalQuestions: Int,
            answeredQuestions: Int,
            timeSpent: Long,
            testMode: String = "",
            testTitle: String = "",
            onConfirm: (Boolean) -> Unit
        ): SubmitTestDialogFragment {
            return SubmitTestDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TOTAL_QUESTIONS, totalQuestions)
                    putInt(ARG_ANSWERED_QUESTIONS, answeredQuestions)
                    putLong(ARG_TIME_SPENT, timeSpent)
                    putString(ARG_TEST_MODE, testMode)
                    putString(ARG_TEST_TITLE, testTitle)
                }
                this.onSubmitConfirmed = onConfirm
            }
        }
    }
}
