package com.octal.examly.presentation.ui.fragment.common

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
import com.octal.examly.databinding.FragmentHomeBinding
import com.octal.examly.domain.model.UserRole
import com.octal.examly.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    private var currentUserRole: UserRole? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeCurrentUser()
        observeDashboardStats()
        setupClickListeners()
    }

    private fun observeCurrentUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentUser.collect { user ->
                    user?.let {
                        currentUserRole = it.role
                        setupUIForRole(it.role)

                        binding.tvWelcomeMessage.text = getString(
                            R.string.welcome_back,
                            it.username
                        )
                    }
                }
            }
        }
    }

    private fun observeDashboardStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userDashboardStats.collect { stats ->
                    stats?.let {
                        displayUserStats(it)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.adminDashboardStats.collect { stats ->
                    stats?.let {
                        displayAdminStats(it)
                    }
                }
            }
        }
    }

    private fun setupUIForRole(role: UserRole) {
        when (role) {
            UserRole.USER -> setupUserUI()
            UserRole.ADMIN -> setupAdminUI()
        }
    }

    private fun setupUserUI() {
        binding.sectionUserTests?.visibility = View.VISIBLE
        binding.sectionAdminQuickActions?.visibility = View.GONE

        binding.cardAvailableTests.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_testList)
        }

        binding.cardResults.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_results)
        }

        loadUserDashboardData()
    }

    private fun setupAdminUI() {
        binding.sectionUserTests?.visibility = View.GONE
        binding.sectionAdminQuickActions?.visibility = View.VISIBLE

        binding.cardCreateTest?.setOnClickListener {
            findNavController().navigate(R.id.action_adminHome_to_createTest)
        }

        binding.cardAssignTest?.setOnClickListener {
            findNavController().navigate(R.id.action_adminHome_to_assignTest)
        }

        binding.cardCreateUser?.setOnClickListener {
            findNavController().navigate(R.id.action_adminHome_to_createUser)
        }

        binding.cardCreateSubject?.setOnClickListener {
            findNavController().navigate(R.id.action_adminHome_to_createSubject)
        }

        binding.cardViewMetrics?.setOnClickListener {
            findNavController().navigate(R.id.action_adminHome_to_metrics)
        }

        loadAdminDashboardData()
    }

    private fun setupClickListeners() {
        binding.btnSettings?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

        binding.btnRefresh?.setOnClickListener {
            refreshDashboard()
        }
    }

    private fun loadUserDashboardData() {
        viewModel.currentUser.value?.let { user ->
            viewModel.loadUserDashboardStats(user.id)
        }
    }

    private fun displayUserStats(stats: com.octal.examly.domain.usecase.dashboard.UserDashboardStats) {
        binding.tvAssignedTestsCount?.text = stats.assignedTestsCount.toString()
        binding.tvPendingTestsCount?.text = stats.pendingTestsCount.toString()
        binding.tvRecentResultsCount?.text = stats.recentResultsCount.toString()
    }

    private fun loadAdminDashboardData() {
        viewModel.loadAdminDashboardStats()
    }

    private fun displayAdminStats(stats: com.octal.examly.domain.usecase.dashboard.AdminDashboardStats) {
        binding.tvTotalUsersCount?.text = stats.totalUsersCount.toString()
        binding.tvTotalTestsCount?.text = stats.totalTestsCount.toString()
        binding.tvTotalSubjectsCount?.text = stats.totalSubjectsCount.toString()
    }

    private fun refreshDashboard() {
        currentUserRole?.let { role ->
            when (role) {
                UserRole.USER -> loadUserDashboardData()
                UserRole.ADMIN -> loadAdminDashboardData()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
