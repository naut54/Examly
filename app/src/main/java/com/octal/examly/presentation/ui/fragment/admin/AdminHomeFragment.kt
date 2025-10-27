package com.octal.examly.presentation.ui.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.octal.examly.R
import com.octal.examly.databinding.FragmentAdminHomeBinding
import com.octal.examly.presentation.viewmodel.MainViewModel
import com.octal.examly.presentation.viewmodel.MetricsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminHomeFragment : Fragment() {

    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by viewModels()
    private val metricsViewModel: MetricsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupQuickActions()
        observeCurrentUser()
        observeMetrics()
        loadMetrics()
    }

    private fun setupQuickActions() {
        binding.cardCreateTest.setOnClickListener {
            navigateToCreateTest()
        }

        binding.cardAssignTestAction?.setOnClickListener {
            navigateToAssignTest()
        }

        binding.cardAssignTest?.setOnClickListener {
            navigateToAssignTest()
        }

        binding.cardCreateUser.setOnClickListener {
            navigateToCreateUser()
        }

        binding.cardCreateSubject.setOnClickListener {
            navigateToCreateSubject()
        }

        binding.cardCreateQuestion?.setOnClickListener {
            navigateToCreateQuestion()
        }

        binding.cardViewMetrics.setOnClickListener {
            navigateToMetrics()
        }

        binding.cardAllTests?.setOnClickListener {
            navigateToAllTests()
        }

        binding.cardAllResults?.setOnClickListener {
            navigateToAllResults()
        }

        binding.btnRefresh?.setOnClickListener {
            refreshMetrics()
        }
    }

    private fun observeCurrentUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.currentUser.collect { user ->
                    user?.let {
                        binding.tvWelcomeMessage.text = getString(
                            R.string.welcome_admin,
                            it.username
                        )
                    }
                }
            }
        }
    }

    private fun observeMetrics() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                metricsViewModel.platformMetrics.collect { metrics ->
                    metrics?.let {
                        displayMetrics(it)
                    }
                }
            }
        }
    }

    private fun loadMetrics() {
        metricsViewModel.loadPlatformMetrics()
    }

    private fun refreshMetrics() {
        binding.swipeRefresh?.isRefreshing = true
        loadMetrics()
        binding.swipeRefresh?.isRefreshing = false
    }

    private fun displayMetrics(metrics: com.octal.examly.domain.model.PlatformMetrics) {
        binding.tvTotalUsers.text = metrics.totalUsers.toString()

        binding.tvTotalTests.text = metrics.totalTests.toString()

        binding.tvTotalSubjects.text = metrics.totalSubjects.toString()

        binding.tvTotalQuestions?.text = metrics.totalQuestions.toString()

        binding.tvTotalResults?.text = metrics.totalResults.toString()

        binding.tvActiveAssignments?.text = metrics.activeAssignments.toString()

        binding.tvRecentActivity?.text = getString(
            R.string.recent_activity_count,
            metrics.recentActivityCount
        )

        if (metrics.totalAssignments > 0) {
            val completionRate = (metrics.completedAssignments.toFloat() /
                                 metrics.totalAssignments * 100).toInt()
            binding.tvCompletionRate?.text = getString(
                R.string.completion_rate,
                completionRate
            )
            binding.progressCompletionRate?.progress = completionRate
        }

        binding.tvAverageScore?.text = getString(
            R.string.average_score_percentage,
            (metrics.averageScore * 100).toInt()
        )
    }

    private fun navigateToCreateTest() {
        findNavController().navigate(
            R.id.action_adminHome_to_createTest
        )
    }

    private fun navigateToAssignTest() {
        findNavController().navigate(
            R.id.action_adminHome_to_assignTest
        )
    }

    private fun navigateToCreateUser() {
        findNavController().navigate(
            R.id.action_adminHome_to_createUser
        )
    }

    private fun navigateToCreateSubject() {
        findNavController().navigate(
            R.id.action_adminHome_to_createSubject
        )
    }

    private fun navigateToCreateQuestion() {
        findNavController().navigate(
            R.id.action_adminHome_to_createQuestion
        )
    }

    private fun navigateToMetrics() {
        findNavController().navigate(
            R.id.action_adminHome_to_metrics
        )
    }

    private fun navigateToAllTests() {
        findNavController().navigate(
            R.id.testListFragment
        )
    }

    private fun navigateToAllResults() {
        findNavController().navigate(
            R.id.resultsFragment
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
