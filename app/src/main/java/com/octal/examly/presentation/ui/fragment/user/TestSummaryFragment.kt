package com.octal.examly.presentation.ui.fragment.user

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.octal.examly.R
import com.octal.examly.databinding.FragmentTestSummaryBinding
import com.octal.examly.domain.model.TestAssignment
import com.octal.examly.presentation.state.UiState
import com.octal.examly.presentation.viewmodel.TestSummaryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class TestSummaryFragment : Fragment() {

    companion object {
        private const val TAG = "StartTestFlow"
    }

    private var _binding: FragmentTestSummaryBinding? = null
    private val binding get() = _binding!!

    private val testSummaryViewModel: TestSummaryViewModel by viewModels()
    private val args: TestSummaryFragmentArgs by navArgs()

    private var currentAssignment: TestAssignment? = null
    private var currentTest: com.octal.examly.domain.model.Test? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "TestSummaryFragment.onViewCreated args.assignmentId=${args.assignmentId}")

        setupUI()
        observeAssignment()
        observeTestDetails()
        observePendingAttempt()

        Log.d(TAG, "loadAssignment: id=${args.assignmentId}")
        testSummaryViewModel.loadAssignment(args.assignmentId)
    }

    private fun setupUI() {
        // Use findViewById for btnStart to avoid binding property mismatch during refactor
        binding.root.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnStart)?.setOnClickListener {
            Log.d(TAG, "btnStart clicked")
            val isPracticeSelected = binding.rbPracticeMode?.isChecked == true
            if (isPracticeSelected) {
                startTest(isPracticeMode = true)
            } else {
                // Exam mode requires confirmation
                showExamModeConfirmation()
            }
        }

        binding.btnContinueTest?.setOnClickListener {
            Log.d(TAG, "btnContinueTest clicked")
            continueTest()
        }

        binding.btnBack?.setOnClickListener {
            Log.d(TAG, "btnBack clicked → navigateUp")
            findNavController().navigateUp()
        }
    }

    private fun observeAssignment() {
        Log.d(TAG, "observeAssignment: START")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                testSummaryViewModel.assignmentState.collect { state ->
                    Log.d(TAG, "observeAssignment: state=$state")
                    when (state) {
                        is UiState.Loading -> {
                            Log.d(TAG, "observeAssignment: Loading")
                            showLoading()
                        }

                        is UiState.Success -> {
                            Log.d(TAG, "observeAssignment: Success assignmentId=${state.data.id} testId=${state.data.testId}")
                            hideLoading()
                            currentAssignment = state.data
                            displayAssignmentInfo(state.data)
                        }

                        is UiState.Error -> {
                            Log.e(TAG, "observeAssignment: Error=${state.message}")
                            hideLoading()
                            showError(state.message)
                        }

                        is UiState.Empty -> {
                            Log.w(TAG, "observeAssignment: Empty")
                            hideLoading()
                            showError(getString(R.string.error_assignment_not_found))
                        }

                        is UiState.Idle -> {
                            Log.d(TAG, "observeAssignment: Idle")
                            hideLoading()
                        }

                        else -> {
                            Log.w(TAG, "observeAssignment: Unknown state=$state")
                            hideLoading()
                        }
                    }
                }
            }
        }
    }

    private fun observeTestDetails() {
        Log.d(TAG, "observeTestDetails: START")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                testSummaryViewModel.testState.collect { state ->
                    Log.d(TAG, "observeTestDetails: state=$state")
                    when (state) {
                        is UiState.Success -> {
                            Log.d(TAG, "observeTestDetails: Success testId=${state.data.id}")
                            currentTest = state.data
                            currentAssignment?.let { assignment ->
                                displayAssignmentInfo(assignment)
                            }
                        }
                        is UiState.Error -> {
                            Log.e(TAG, "observeTestDetails: Error=${state.message}")
                        }
                        else -> {
                            Log.d(TAG, "observeTestDetails: state=$state")
                        }
                    }
                }
            }
        }
    }

    private fun observePendingAttempt() {
        Log.d(TAG, "observePendingAttempt: START")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                testSummaryViewModel.pendingAttempt.collect { attempt ->
                    Log.d(TAG, "observePendingAttempt: attempt=${attempt?.id} index=${attempt?.currentQuestionIndex}")
                    if (attempt != null) {
                        binding.btnContinueTest?.visibility = View.VISIBLE
                        binding.layoutContinueInfo?.visibility = View.VISIBLE

                        val progress = "${attempt.currentQuestionIndex + 1}/${attempt.questions.size}"
                        binding.tvContinueProgress?.text = getString(
                            R.string.continue_test_progress,
                            progress
                        )
                    } else {
                        binding.btnContinueTest?.visibility = View.GONE
                        binding.layoutContinueInfo?.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun displayAssignmentInfo(assignment: TestAssignment) {
        val test = currentTest
        if (test != null) {
            binding.tvTestTitle.text = test.title
            binding.tvTestDescription?.text = test.description.ifBlank {
                getString(R.string.no_description)
            }
            binding.tvSubject.text = getString(R.string.subject_placeholder)

            val questionCount = test.configuration.numberOfQuestions
            if (questionCount != null) {
                binding.tvQuestionCount.text = getString(R.string.question_count, questionCount)
            } else {
                binding.tvQuestionCount.text = getString(R.string.questions_unknown)
            }

            val hasTimer = test.configuration.hasTimer
            val minutes = test.configuration.timeLimit
            if (hasTimer && minutes != null) {
                binding.layoutTimeLimit?.visibility = View.VISIBLE
                binding.tvTimeLimit?.text = formatTimeLimit(minutes)
            } else {
                binding.layoutTimeLimit?.visibility = View.GONE
            }
        } else {
            binding.tvTestTitle.text = getString(R.string.test_placeholder_title, assignment.testId)
            binding.tvTestDescription?.text = getString(R.string.no_description)
            binding.tvSubject.text = getString(R.string.subject_placeholder)
            binding.tvQuestionCount.text = getString(R.string.questions_unknown)
            binding.layoutTimeLimit?.visibility = View.GONE
        }

        binding.tvAssignedDate?.text = getString(
            R.string.assigned_date,
            formatDate(assignment.assignedAt)
        )

        assignment.deadline?.let { d ->
            binding.tvDeadline?.text = formatDate(d)
            binding.layoutDeadline?.visibility = View.VISIBLE
        } ?: run {
            binding.layoutDeadline?.visibility = View.GONE
        }

        checkDeadline(assignment.deadline)

        setupModeButtons(assignment)

    }

    private fun setupModeButtons(assignment: TestAssignment) {
        // Show the single Start button
        binding.root.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnStart)?.visibility = View.VISIBLE

        // Control mode selector visibility based on the test configuration
        val hasPractice = currentTest?.configuration?.hasPracticeMode ?: false
        if (hasPractice) {
            binding.cardModeSelection?.visibility = View.VISIBLE
            binding.rbExamMode?.isChecked = true
        } else {
            binding.cardModeSelection?.visibility = View.GONE
        }
    }

    private fun displayInstructions(assignment: TestAssignment) {
    }

    private fun checkDeadline(deadline: Long?) {
        if (deadline == null) {
            binding.tvDeadlineWarning?.visibility = View.GONE
            return
        }
        val now = System.currentTimeMillis()
        val timeRemaining = deadline - now

        if (timeRemaining < 0) {
            binding.tvDeadlineWarning?.text = getString(R.string.deadline_passed)
            binding.tvDeadlineWarning?.setTextColor(
                resources.getColor(R.color.error, null)
            )
            binding.tvDeadlineWarning?.visibility = View.VISIBLE
        } else if (timeRemaining < TimeUnit.HOURS.toMillis(24)) {
            binding.tvDeadlineWarning?.text = getString(R.string.deadline_approaching)
            binding.tvDeadlineWarning?.setTextColor(
                resources.getColor(R.color.warning, null)
            )
            binding.tvDeadlineWarning?.visibility = View.VISIBLE
        } else {
            binding.tvDeadlineWarning?.visibility = View.GONE
        }
    }

    private fun startTest(isPracticeMode: Boolean) {
        currentAssignment?.let { assignment ->
            Log.d(TAG, "startTest: assignmentId=${assignment.id} testId=${assignment.testId} isPracticeMode=$isPracticeMode")
            val action = TestSummaryFragmentDirections
                .actionTestSummaryToTestExecution(
                    assignmentId = assignment.id,
                    isPracticeMode = isPracticeMode,
                    isResume = false
                )
            findNavController().navigate(action)
        } ?: run {
            Log.e(TAG, "startTest: currentAssignment is null")
        }
    }

    private fun continueTest() {
        currentAssignment?.let { assignment ->
            val attempt = testSummaryViewModel.pendingAttempt.value
            val action = TestSummaryFragmentDirections
                .actionTestSummaryToTestExecution(
                    assignmentId = assignment.id,
                    attemptId = attempt?.id ?: 0L,
                    isResume = true,
                    isPracticeMode = false
                )
            findNavController().navigate(action)
        }
    }

    private fun showExamModeConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.start_exam_mode)
            .setMessage(R.string.exam_mode_confirmation)
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton(R.string.start) { _, _ ->
                startTest(isPracticeMode = false)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun formatTimeLimit(minutes: Int): String {
        return if (minutes >= 60) {
            val hours = minutes / 60
            val mins = minutes % 60
            if (mins > 0) {
                getString(R.string.time_format_hours_minutes, hours, mins)
            } else {
                getString(R.string.time_format_hours, hours)
            }
        } else {
            getString(R.string.time_format_minutes, minutes)
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutContent?.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.layoutContent?.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
