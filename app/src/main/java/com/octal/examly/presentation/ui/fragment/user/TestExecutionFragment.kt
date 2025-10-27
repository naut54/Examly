package com.octal.examly.presentation.ui.fragment.user

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.octal.examly.R
import com.octal.examly.presentation.ui.dialog.ExitTestDialogFragment
import com.octal.examly.presentation.ui.dialog.SubmitTestDialogFragment
import com.octal.examly.databinding.FragmentTestExecutionBinding
import com.octal.examly.domain.model.Question
import com.octal.examly.domain.model.TestAttempt
import com.octal.examly.presentation.adapter.QuestionPagerAdapter
import com.octal.examly.presentation.state.TestExecutionState
import com.octal.examly.presentation.viewmodel.TestExecutionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestExecutionFragment : Fragment() {

    companion object {
        private const val TAG = "StartTestFlow"
    }

    private var _binding: FragmentTestExecutionBinding? = null
    private val binding get() = _binding!!

    private val testExecutionViewModel: TestExecutionViewModel by viewModels()
    private val args: TestExecutionFragmentArgs by navArgs()

    private lateinit var questionPagerAdapter: QuestionPagerAdapter
    private var currentAttempt: TestAttempt? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestExecutionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "TestExecutionFragment.onViewCreated args: assignmentId=${args.assignmentId} isResume=${args.isResume} attemptId=${args.attemptId} isPracticeMode=${args.isPracticeMode}")

        setupUI()
        setupBackPressedHandler()
        observeExecutionState()
        observeTimer()
        observeProgress()

        if (args.isResume) {
            val attemptId = args.attemptId
            Log.d(TAG, "onViewCreated: isResume=true attemptId=$attemptId")
            if (attemptId <= 0L) {
                Log.e(TAG, "onViewCreated: attemptId invalid, navigating up")
                Toast.makeText(requireContext(), getString(R.string.error_attempt_not_found), Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            } else {
                Log.d(TAG, "resumeTest: attemptId=$attemptId")
                testExecutionViewModel.resumeTest(attemptId)
            }
        } else {
            Log.d(TAG, "startTest: assignmentId=${args.assignmentId} isPracticeMode=${args.isPracticeMode}")
            testExecutionViewModel.startTest(args.assignmentId, args.isPracticeMode)
        }
    }

    private fun setupUI() {
        questionPagerAdapter = QuestionPagerAdapter(
            fragment = this,
            onAnswerSelected = { questionId, answerIds ->
                Log.d(TAG, "onAnswerSelected: questionId=$questionId answers=${answerIds.size}")
                saveAnswer(questionId, answerIds.toList())
            }
        )

        binding.vpQuestions.apply {
            adapter = questionPagerAdapter
            isUserInputEnabled = true

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    Log.d(TAG, "ViewPager.onPageSelected: position=$position")
                    onQuestionChanged(position)
                }
            })
        }

        binding.btnPrevious.setOnClickListener {
            Log.d(TAG, "btnPrevious clicked")
            navigateToPreviousQuestion()
        }

        binding.btnNext.setOnClickListener {
            Log.d(TAG, "btnNext clicked")
            navigateToNextQuestion()
        }

        binding.btnSubmit.setOnClickListener {
            Log.d(TAG, "btnSubmit clicked")
            showSubmitConfirmation()
        }

        binding.btnExit?.setOnClickListener {
            Log.d(TAG, "btnExit clicked")
            showExitConfirmation()
        }
    }

    private fun setupBackPressedHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitConfirmation()
                }
            }
        )
    }

    private fun observeExecutionState() {
        Log.d(TAG, "observeExecutionState: START")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                testExecutionViewModel.executionState.collect { state ->
                    Log.d(TAG, "observeExecutionState: state=$state")
                    when (state) {
                        is TestExecutionState.Loading -> {
                            Log.d(TAG, "state=Loading")
                            showLoading()
                        }

                        is TestExecutionState.InProgress -> {
                            Log.d(TAG, "state=InProgress attemptId=${state.attempt.id} index=${state.attempt.currentQuestionIndex} questions=${state.attempt.questions.size}")
                            hideLoading()
                            currentAttempt = state.attempt
                            setupTest(state.attempt)
                        }

                        is TestExecutionState.Submitting -> {
                            Log.d(TAG, "state=Submitting")
                            showSubmitting()
                        }

                        is TestExecutionState.Completed -> {
                            Log.d(TAG, "state=Completed resultId=${state.resultId}")
                            hideLoading()
                            navigateToResult(state.resultId)
                        }

                        is TestExecutionState.Error -> {
                            Log.e(TAG, "state=Error message=${state.message}")
                            hideLoading()
                            showError(state.message)
                        }

                        else -> {
                            Log.d(TAG, "state=$state (NotStarted/Paused)")
                        }
                    }
                }
            }
        }
    }

    private fun observeTimer() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                testExecutionViewModel.timeRemaining.collect { timeRemaining ->
                    if (timeRemaining != null) {
                        updateTimer(timeRemaining)

                        if (timeRemaining <= 0) {
                            autoSubmitTest()
                        }
                    } else {
                        binding.timerView?.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun observeProgress() {
        Log.d(TAG, "observeProgress: START")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                testExecutionViewModel.currentQuestionIndex.collect { index ->
                    Log.d(TAG, "observeProgress: index=${index}")
                    updateProgressIndicator(index)
                    updateNavigationButtons(index)
                }
            }
        }
    }

    private fun setupTest(attempt: TestAttempt) {
        Log.d(TAG, "setupTest: attemptId=${attempt.id} questions=${attempt.questions.size} index=${attempt.currentQuestionIndex} timeRemaining=${attempt.timeRemaining}")
        questionPagerAdapter.submitList(attempt.questions)

        if (attempt.mode == com.octal.examly.domain.model.TestAttemptMode.PRACTICE) {
            binding.tvModeBadge?.text = getString(R.string.practice_mode)
            binding.tvModeBadge?.setBackgroundResource(R.drawable.bg_badge_practice)
        } else {
            binding.tvModeBadge?.text = getString(R.string.exam_mode)
            binding.tvModeBadge?.setBackgroundResource(R.drawable.bg_badge_exam)
        }
        binding.tvModeBadge?.visibility = View.VISIBLE

        binding.vpQuestions.setCurrentItem(attempt.currentQuestionIndex, false)

        if (attempt.timeRemaining != null) {
            binding.timerView?.visibility = View.VISIBLE
            testExecutionViewModel.startTimer(attempt.timeRemaining.toLong() * 1000L)
        } else {
            binding.timerView?.visibility = View.GONE
        }

        updateProgressIndicator(attempt.currentQuestionIndex)
    }

    private fun updateProgressIndicator(currentIndex: Int) {
        Log.d(TAG, "updateProgressIndicator: index=${currentIndex} total=${questionPagerAdapter.itemCount}")
        val totalQuestions = questionPagerAdapter.itemCount
        binding.progressIndicator?.max = totalQuestions
        binding.progressIndicator?.progress = currentIndex + 1

        binding.tvProgress?.text = getString(
            R.string.question_progress,
            currentIndex + 1,
            totalQuestions
        )
    }

    private fun updateNavigationButtons(currentIndex: Int) {
        val totalQuestions = questionPagerAdapter.itemCount
        Log.d(TAG, "updateNavigationButtons: index=${currentIndex} total=${totalQuestions}")

        binding.btnPrevious.isEnabled = currentIndex > 0

        if (currentIndex == totalQuestions - 1) {
            binding.btnNext.visibility = View.GONE
            binding.btnSubmit.visibility = View.VISIBLE
        } else {
            binding.btnNext.visibility = View.VISIBLE
            binding.btnSubmit.visibility = View.GONE
        }
    }

    private fun updateTimer(timeRemaining: Long) {
        Log.d(TAG, "updateTimer: remainingMs=${timeRemaining}")
        binding.timerView?.setTime(timeRemaining)

        if (timeRemaining < 300000) {
            binding.timerView?.setWarningState(true)
        }
    }

    private fun onQuestionChanged(position: Int) {
        Log.d(TAG, "onQuestionChanged: position=${position}")
        testExecutionViewModel.saveProgress(position)

        updateProgressIndicator(position)
        updateNavigationButtons(position)
    }

    private fun navigateToPreviousQuestion() {
        val currentItem = binding.vpQuestions.currentItem
        Log.d(TAG, "navigateToPreviousQuestion: from=${currentItem}")
        if (currentItem > 0) {
            binding.vpQuestions.currentItem = currentItem - 1
        }
    }

    private fun navigateToNextQuestion() {
        val currentItem = binding.vpQuestions.currentItem
        val totalItems = questionPagerAdapter.itemCount
        Log.d(TAG, "navigateToNextQuestion: from=${currentItem} total=${totalItems}")
        if (currentItem < totalItems - 1) {
            binding.vpQuestions.currentItem = currentItem + 1
        }
    }

    private fun saveAnswer(questionId: Long, answerIds: List<Long>) {
        Log.d(TAG, "saveAnswer: questionId=${questionId} answers=${answerIds}")
        testExecutionViewModel.saveAnswer(questionId, answerIds)
    }

    private fun showSubmitConfirmation() {
        Log.d(TAG, "showSubmitConfirmation")
        val unansweredCount = testExecutionViewModel.getUnansweredQuestionCount()
        Log.d(TAG, "showSubmitConfirmation: unansweredCount=${unansweredCount}")

        currentAttempt?.let { attempt ->
            val totalQuestions = attempt.questions.size
            val answeredQuestions = totalQuestions - unansweredCount
            val timeSpent = System.currentTimeMillis() - attempt.startedAt
            val mode = if (attempt.mode == com.octal.examly.domain.model.TestAttemptMode.PRACTICE) {
                getString(R.string.practice_mode)
            } else {
                getString(R.string.exam_mode)
            }

            val dialog = SubmitTestDialogFragment.newInstance(
                totalQuestions = totalQuestions,
                answeredQuestions = answeredQuestions,
                timeSpent = timeSpent,
                testMode = mode,
                testTitle = ""
            ) { confirmed ->
                if (confirmed) {
                    Log.d(TAG, "Submit dialog: confirmed → submitTest")
                    submitTest()
                } else {
                    Log.d(TAG, "Submit dialog: review → staying in test")
                }
            }
            dialog.show(childFragmentManager, "SubmitTestDialog")
        }
    }

    private fun submitTest() {
        Log.d(TAG, "submitTest: requesting ViewModel.submitTest")
        testExecutionViewModel.submitTest()
    }

    private fun autoSubmitTest() {
        Toast.makeText(
            requireContext(),
            R.string.time_up_auto_submit,
            Toast.LENGTH_LONG
        ).show()

        submitTest()
    }

    private fun showExitConfirmation() {
        Log.d(TAG, "showExitConfirmation")

        currentAttempt?.let { attempt ->
            val currentIndex = attempt.currentQuestionIndex
            val totalQuestions = attempt.questions.size
            val answeredCount = testExecutionViewModel.getAnsweredQuestionCount()
            val hasTimer = attempt.timeRemaining != null
            val timeRemaining = attempt.timeRemaining?.toLong()?.times(1000) ?: 0L

            val dialog = ExitTestDialogFragment.newInstance(
                currentQuestion = currentIndex + 1,
                totalQuestions = totalQuestions,
                answeredQuestions = answeredCount,
                hasTimer = hasTimer,
                timeRemaining = timeRemaining
            ) { action ->
                when (action) {
                    ExitTestDialogFragment.ExitTestAction.SAVE_AND_EXIT -> {
                        Log.d(TAG, "Exit dialog: SAVE_AND_EXIT → exitTest")
                        exitTest()
                    }
                    ExitTestDialogFragment.ExitTestAction.EXIT_WITHOUT_SAVING -> {
                        Log.d(TAG, "Exit dialog: EXIT_WITHOUT_SAVING → exitTest")
                        exitTest()
                    }
                    ExitTestDialogFragment.ExitTestAction.CANCEL -> {
                        Log.d(TAG, "Exit dialog: CANCEL → staying in test")
                    }
                }
            }
            dialog.show(childFragmentManager, "ExitTestDialog")
        }
    }

    private fun exitTest() {
        Log.d(TAG, "exitTest: navigateUp")
        findNavController().navigateUp()
    }

    private fun navigateToResult(resultId: Long) {
        val action = TestExecutionFragmentDirections
            .actionTestExecutionToResultDetail(resultId)
        findNavController().navigate(action)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutContent?.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.layoutContent?.visibility = View.VISIBLE
    }

    private fun showSubmitting() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnPrevious.isEnabled = false
        binding.btnNext.isEnabled = false
        binding.btnSubmit.isEnabled = false
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onPause() {
        super.onPause()
        testExecutionViewModel.pauseTimer()
    }

    override fun onResume() {
        super.onResume()
        testExecutionViewModel.resumeTimer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
