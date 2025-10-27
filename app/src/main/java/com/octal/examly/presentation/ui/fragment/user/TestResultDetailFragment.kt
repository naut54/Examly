package com.octal.examly.presentation.ui.fragment.user

import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.octal.examly.R
import com.octal.examly.databinding.FragmentTestResultDetailBinding
import com.octal.examly.domain.model.TestResult
import com.octal.examly.domain.model.UserAnswer
import com.octal.examly.presentation.adapter.ResultDetailAdapter
import com.octal.examly.presentation.state.UiState
import com.octal.examly.presentation.viewmodel.ResultDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class TestResultDetailFragment : Fragment() {

    private var _binding: FragmentTestResultDetailBinding? = null
    private val binding get() = _binding!!

    private val resultDetailViewModel: ResultDetailViewModel by viewModels()
    private val args: TestResultDetailFragmentArgs by navArgs()

    private lateinit var resultDetailAdapter: ResultDetailAdapter
    private var currentResult: TestResult? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestResultDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        observeResultDetail()

        resultDetailViewModel.loadResultDetail(args.resultId)
    }

    private fun setupUI() {
        binding.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnRetakeTest?.setOnClickListener {
            retakeTest()
        }

        binding.btnShare?.setOnClickListener {
            shareResult()
        }
    }

    private fun setupRecyclerView() {
        resultDetailAdapter = ResultDetailAdapter()

        binding.rvQuestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resultDetailAdapter
            setHasFixedSize(false)
        }
    }

    private fun observeResultDetail() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                resultDetailViewModel.resultDetailState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            showLoading()
                        }

                        is UiState.Success -> {
                            hideLoading()
                            currentResult = state.data
                            displayResultDetail(state.data)
                        }

                        is UiState.Error -> {
                            hideLoading()
                            showError(state.message)
                        }

                        is UiState.Empty -> {
                            hideLoading()
                            showError(getString(R.string.error_result_not_found))
                        }

                        is UiState.Idle -> {
                            hideLoading()
                        }

                        else -> {
                            hideLoading()
                        }
                    }
                }
            }
        }
    }

    private fun displayResultDetail(result: TestResult) {
        binding.tvTestTitle.text = getString(R.string.test_placeholder_title, result.testId)
        binding.tvSubject?.text = getString(R.string.subject_placeholder)
        binding.tvCompletedDate?.text = getString(
            R.string.completed_on,
            formatDate(result.completedAt)
        )

        val percentage = result.score.toInt()
        binding.tvScore.text = getString(R.string.score_percentage, percentage)
        binding.tvScoreDetail?.text = getString(
            R.string.score_detail,
            result.correctAnswers,
            result.totalQuestions
        )

        if (result.isPassed) {
            binding.tvPassBadge?.text = getString(R.string.passed)
            binding.tvPassBadge?.setBackgroundResource(R.drawable.bg_badge_passed)
            binding.tvPassBadge?.setTextColor(
                resources.getColor(R.color.success, null)
            )
        } else {
            binding.tvPassBadge?.text = getString(R.string.failed)
            binding.tvPassBadge?.setBackgroundResource(R.drawable.bg_badge_failed)
            binding.tvPassBadge?.setTextColor(
                resources.getColor(R.color.error, null)
            )
        }
        binding.tvPassBadge?.visibility = View.VISIBLE

        if (result.mode == com.octal.examly.domain.model.TestAttemptMode.PRACTICE) {
            binding.tvModeBadge?.text = getString(R.string.practice_mode)
            binding.tvModeBadge?.setBackgroundResource(R.drawable.bg_badge_practice)
        } else {
            binding.tvModeBadge?.text = getString(R.string.exam_mode)
            binding.tvModeBadge?.setBackgroundResource(R.drawable.bg_badge_exam)
        }
        binding.tvModeBadge?.visibility = View.VISIBLE

        displayStatistics(result)

        displayQuestionsAndAnswers(result)
    }

    private fun displayStatistics(result: TestResult) {
        val total = result.totalQuestions
        val correctClamped = result.correctAnswers.coerceIn(0, total)
        val wrongComputed = if (result.wrongAnswers >= 0) result.wrongAnswers else (total - correctClamped)
        val wrongClamped = wrongComputed.coerceIn(0, total)

        binding.tvCorrectAnswers?.text = correctClamped.toString()

        binding.tvWrongAnswers?.text = wrongClamped.toString()

        binding.tvTimeSpent?.text = formatTimeSpent(result.timeSpent ?: 0L)

        val accuracy = if (total > 0) {
            (correctClamped.toFloat() / total * 100).toInt()
        } else {
            0
        }
        binding.tvAccuracy?.text = getString(R.string.accuracy_percentage, accuracy)

        binding.progressCorrect?.max = total
        binding.progressCorrect?.progress = correctClamped

        binding.progressWrong?.max = total
        binding.progressWrong?.progress = wrongClamped

        setupChart(result)
    }

    private fun setupChart(result: TestResult) {
        binding.chartView?.visibility = View.GONE
    }

    private fun displayQuestionsAndAnswers(result: TestResult) {
        val userAnswers = resultDetailViewModel.getUserAnswers(result)
        resultDetailAdapter.submitList(userAnswers)
    }

    private fun retakeTest() {
        findNavController().navigateUp()
    }

    private fun shareResult() {
        currentResult?.let { result ->
            val shareText = buildString {
                appendLine(getString(R.string.app_name))
                appendLine()
                appendLine("${getString(R.string.test)}: ${getString(R.string.test_placeholder_title, result.testId)}")
                appendLine("${getString(R.string.score)}: ${result.score.toInt()}%")
                appendLine("${getString(R.string.correct)}: ${result.correctAnswers}/${result.totalQuestions}")

                if (result.isPassed) {
                    appendLine(getString(R.string.result_passed))
                } else {
                    appendLine(getString(R.string.result_failed))
                }
            }

            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            }

            startActivity(
                android.content.Intent.createChooser(
                    shareIntent,
                    getString(R.string.share_result)
                )
            )
        }
    }

    private fun formatTimeSpent(timeMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(timeMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis) % 60

        return when {
            hours > 0 -> getString(R.string.time_format_hms, hours, minutes, seconds)
            minutes > 0 -> getString(R.string.time_format_ms, minutes, seconds)
            else -> getString(R.string.time_format_s, seconds)
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
