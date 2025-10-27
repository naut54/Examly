package com.octal.examly.presentation.ui.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.octal.examly.R
import com.octal.examly.databinding.FragmentAdminMetricsBinding
import com.octal.examly.domain.model.Subject
import com.octal.examly.presentation.adapter.MetricsAdapter
import com.octal.examly.presentation.state.FilterState
import com.octal.examly.presentation.state.UiState
import com.octal.examly.presentation.viewmodel.MetricsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminMetricsFragment : Fragment(), MenuProvider {

    private var _binding: FragmentAdminMetricsBinding? = null
    private val binding get() = _binding!!

    private val metricsViewModel: MetricsViewModel by viewModels()
    private lateinit var metricsAdapter: MetricsAdapter

    private var currentSubjectFilter: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminMetricsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setupUI()
        setupRecyclerView()
        observeMetrics()
        observeSubjects()

        loadMetrics()
    }

    private fun setupUI() {
        binding.swipeRefresh?.setOnRefreshListener {
            refreshMetrics()
        }

        binding.btnClearFilters?.setOnClickListener {
            clearFilters()
        }
    }

    private fun setupRecyclerView() {
        metricsAdapter = MetricsAdapter()

        binding.rvMetricsBreakdown?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = metricsAdapter
        }
    }

    private fun observeMetrics() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                metricsViewModel.metricsState.collect { state ->
                    when (state) {
                        is UiState.Loading -> showLoading()
                        is UiState.Success -> {
                            hideLoading()
                            displayMetrics(state.data)
                        }
                        is UiState.Error -> {
                            hideLoading()
                            showError(state.message)
                        }
                        else -> hideLoading()
                    }
                }
            }
        }
    }

    private fun observeSubjects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                metricsViewModel.subjectsState.collect { state ->
                    when (state) {
                        is UiState.Success -> displaySubjectFilters(state.data)
                        else -> {}
                    }
                }
            }
        }
    }

    private fun displaySubjectFilters(subjects: List<Subject>) {
        binding.chipGroupSubjects?.removeAllViews()

        val allChip = Chip(requireContext()).apply {
            text = getString(R.string.all_subjects)
            isCheckable = true
            isChecked = true
            setOnClickListener {
                currentSubjectFilter = null
                loadMetrics()
            }
        }
        binding.chipGroupSubjects?.addView(allChip)

        subjects.forEach { subject ->
            val chip = Chip(requireContext()).apply {
                text = subject.name
                isCheckable = true
                setOnClickListener {
                    currentSubjectFilter = subject.id
                    loadMetrics()
                }
            }
            binding.chipGroupSubjects?.addView(chip)
        }
    }

    private fun displayMetrics(metrics: com.octal.examly.domain.model.GlobalMetrics) {
        binding.tvTotalTests?.text = metrics.totalTests.toString()
        binding.tvTotalUsers?.text = metrics.totalUsers.toString()
        binding.tvTotalResults?.text = metrics.totalResults.toString()
        binding.tvAverageScore?.text = getString(
            R.string.score_percentage,
            (metrics.averageScore * 100).toInt()
        )

        binding.tvPassRate?.text = getString(
            R.string.pass_rate_formatted,
            (metrics.passRate * 100).toInt()
        )
        binding.tvFailRate?.text = getString(
            R.string.fail_rate_formatted,
            (metrics.failRate * 100).toInt()
        )

        binding.progressPassRate?.progress = (metrics.passRate * 100).toInt()
        binding.progressFailRate?.progress = (metrics.failRate * 100).toInt()

        binding.tvAverageTime?.text = formatTime(metrics.averageTimeSpent)

        binding.tvCompletionRate?.text = getString(
            R.string.completion_rate,
            (metrics.completionRate * 100).toInt()
        )

        setupCharts(metrics)

        metricsAdapter.submitList(metrics.subjectBreakdown)
    }

    private fun setupCharts(metrics: com.octal.examly.domain.model.GlobalMetrics) {
        binding.chartContainer?.visibility = View.GONE
    }

    private fun formatTime(millis: Long): String {
        val minutes = millis / 60000
        val hours = minutes / 60
        val mins = minutes % 60

        return if (hours > 0) {
            getString(R.string.time_format_hours_minutes, hours, mins)
        } else {
            getString(R.string.time_format_minutes, mins.toInt())
        }
    }

    private fun loadMetrics() {
        if (currentSubjectFilter != null) {
            metricsViewModel.loadMetricsBySubject(currentSubjectFilter!!)
        } else {
            metricsViewModel.loadGlobalMetrics()
        }
    }

    private fun refreshMetrics() {
        loadMetrics()
        binding.swipeRefresh?.isRefreshing = false
    }

    private fun clearFilters() {
        currentSubjectFilter = null
        binding.chipGroupSubjects?.check(binding.chipGroupSubjects?.getChildAt(0)?.id ?: 0)
        loadMetrics()
    }

    private fun showFilterDialog() {
    }

    private fun exportMetrics() {
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_metrics, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_filter -> {
                showFilterDialog()
                true
            }
            R.id.action_export -> {
                exportMetrics()
                true
            }
            else -> false
        }
    }

    private fun showLoading() {
        binding.progressBar?.visibility = View.VISIBLE
        binding.layoutContent?.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar?.visibility = View.GONE
        binding.layoutContent?.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
