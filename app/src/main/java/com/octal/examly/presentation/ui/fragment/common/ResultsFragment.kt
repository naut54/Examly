package com.octal.examly.presentation.ui.fragment.common

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.octal.examly.R
import com.octal.examly.databinding.FragmentResultsBinding
import com.octal.examly.domain.model.TestResult
import com.octal.examly.domain.model.UserRole
import com.octal.examly.presentation.adapter.ResultsAdapter
import com.octal.examly.presentation.state.FilterState
import com.octal.examly.presentation.state.UiState
import com.octal.examly.presentation.ui.dialog.FilterDialogFragment
import com.octal.examly.presentation.viewmodel.MainViewModel
import com.octal.examly.presentation.viewmodel.ResultsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResultsFragment : Fragment(), MenuProvider {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    private val resultsViewModel: ResultsViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var resultsAdapter: ResultsAdapter
    private var currentUserRole: UserRole? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setupRecyclerView()
        setupSwipeRefresh()
        observeCurrentUser()
        observeResults()
        observeFilterState()
    }

    private fun setupRecyclerView() {
        resultsAdapter = ResultsAdapter(
            onResultClick = { result ->
                navigateToResultDetail(result)
            }
        )

        binding.rvResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resultsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh?.setOnRefreshListener {
            refreshResults()
        }
    }

    private fun observeCurrentUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.currentUser.collect { user ->
                    user?.let {
                        currentUserRole = it.role
                        loadResults(it.role, it.id)
                    }
                }
            }
        }
    }

    private fun loadResults(role: UserRole, userId: Long) {
        when (role) {
            UserRole.USER -> {
                resultsViewModel.loadUserResults(userId)
            }
            UserRole.ADMIN -> {
                resultsViewModel.loadAllResults()
            }
        }
    }

    private fun observeResults() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                resultsViewModel.resultsState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            showLoading()
                        }

                        is UiState.Success -> {
                            hideLoading()
                            showResults(state.data)
                        }

                        is UiState.Error -> {
                            hideLoading()
                            showError(state.message)
                        }

                        is UiState.Empty -> {
                            hideLoading()
                            showEmptyState()
                        }

                        is UiState.Idle -> {
                            hideLoading()
                        }
                    }
                }
            }
        }
    }

    private fun observeFilterState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                resultsViewModel.filterState.collect { filterState ->
                    updateFilterIndicator(filterState)
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvResults.visibility = View.GONE
        binding.layoutEmpty?.visibility = View.GONE
        binding.swipeRefresh?.isRefreshing = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.swipeRefresh?.isRefreshing = false
    }

    private fun showResults(results: List<TestResult>) {
        if (results.isEmpty()) {
            showEmptyState()
        } else {
            binding.rvResults.visibility = View.VISIBLE
            binding.layoutEmpty?.visibility = View.GONE
            resultsAdapter.submitList(results)

            binding.tvResultsCount?.text = getString(
                R.string.total_results,
                results.size
            )
        }
    }

    private fun showEmptyState() {
        binding.rvResults.visibility = View.GONE
        binding.layoutEmpty?.visibility = View.VISIBLE
        binding.tvEmptyMessage?.text = getString(R.string.no_results_found)
    }

    private fun showError(message: String) {
        binding.rvResults.visibility = View.GONE
        binding.layoutEmpty?.visibility = View.VISIBLE
        binding.tvEmptyMessage?.text = message
    }

    private fun updateFilterIndicator(filterState: FilterState) {
        val hasActiveFilters = filterState.hasActiveFilters()
        binding.ivFilterIndicator?.visibility = if (hasActiveFilters) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun refreshResults() {
        currentUserRole?.let { role ->
            mainViewModel.currentUser.value?.let { user ->
                loadResults(role, user.id)
            }
        }
    }

    private fun navigateToResultDetail(result: TestResult) {
        val action = ResultsFragmentDirections
            .actionResultsFragmentToTestResultDetailFragment(result.id)
        findNavController().navigate(action)
    }

    private fun showFilterDialog() {
        val filterDialog = FilterDialogFragment.newInstance(
            currentFilter = resultsViewModel.filterState.value,
            availableSubjects = emptyList()
        ) { newFilterState ->
            resultsViewModel.applyFilters(newFilterState)
            refreshResults()
        }
        filterDialog.show(childFragmentManager, "FilterDialog")
    }

    private fun clearFilters() {
        resultsViewModel.clearFilters()
        refreshResults()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_results, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_filter -> {
                showFilterDialog()
                true
            }
            R.id.action_clear_filters -> {
                clearFilters()
                true
            }
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
