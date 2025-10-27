package com.octal.examly.presentation.ui.fragment.user

import android.os.Bundle
import android.util.Log
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
import com.octal.examly.databinding.FragmentTestListBinding
import com.octal.examly.domain.model.TestAssignment
import com.octal.examly.presentation.adapter.TestAssignmentListAdapter
import com.octal.examly.presentation.state.FilterState
import com.octal.examly.presentation.state.UiState
import com.octal.examly.presentation.viewmodel.MainViewModel
import com.octal.examly.presentation.viewmodel.TestListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestListFragment : Fragment(), MenuProvider {

    companion object {
        private const val TAG = "StartTestFlow"
    }

    private var _binding: FragmentTestListBinding? = null
    private val binding get() = _binding!!

    private val testListViewModel: TestListViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var testListAdapter: TestAssignmentListAdapter
    private var currentUserId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "TestListFragment.onViewCreated")

        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setupRecyclerView()
        setupSwipeRefresh()
        observeCurrentUser()
        observeTests()
        observeFilterState()
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView")
        testListAdapter = TestAssignmentListAdapter(
            onTestClick = { assignment: TestAssignment ->
                Log.d(TAG, "onTestClick: assignmentId=${assignment.id} testId=${assignment.testId}")
                navigateToTestSummary(assignment)
            },
            onResumeClick = { assignment: TestAssignment ->
                Log.d(TAG, "onResumeClick: assignmentId=${assignment.id} testId=${assignment.testId}")
                resumeTest(assignment)
            }
        )

        binding.rvTests.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = testListAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh?.setOnRefreshListener {
            Log.d(TAG, "SwipeRefresh: onRefresh")
            refreshTests()
        }
    }

    private fun observeCurrentUser() {
        Log.d(TAG, "observeCurrentUser: START")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.currentUser.collect { user ->
                    Log.d(TAG, "observeCurrentUser: collected user=$user")
                    user?.let {
                        currentUserId = it.id
                        Log.d(TAG, "observeCurrentUser: currentUserId=$currentUserId → loadTests")
                        loadTests(it.id)
                    }
                }
            }
        }
    }

    private fun loadTests(userId: Long) {
        Log.d(TAG, "loadTests: userId=$userId")
        testListViewModel.loadAssignedTests(userId)
    }

    private fun observeTests() {
        Log.d(TAG, "observeTests: START")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                testListViewModel.testsState.collect { state ->
                    Log.d(TAG, "observeTests: state=$state")
                    when (state) {
                        is UiState.Loading -> {
                            Log.d(TAG, "observeTests: Loading")
                            showLoading()
                        }

                        is UiState.Success -> {
                            Log.d(TAG, "observeTests: Success count=${state.data.size}")
                            hideLoading()
                            showTests(state.data)
                        }

                        is UiState.Error -> {
                            Log.e(TAG, "observeTests: Error=${state.message}")
                            hideLoading()
                            showError(state.message)
                        }

                        is UiState.Empty -> {
                            Log.d(TAG, "observeTests: Empty")
                            hideLoading()
                            showEmptyState()
                        }

                        is UiState.Idle -> {
                            Log.d(TAG, "observeTests: Idle")
                            hideLoading()
                        }

                        else -> {
                            Log.w(TAG, "observeTests: Unknown state=$state")
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
                testListViewModel.filterState.collect { filterState ->
                    updateFilterIndicator(filterState)
                }
            }
        }
    }

    private fun showLoading() {
        Log.d(TAG, "showLoading")
        binding.progressBar.visibility = View.VISIBLE
        binding.rvTests.visibility = View.GONE
        binding.layoutEmpty?.visibility = View.GONE
        binding.swipeRefresh?.isRefreshing = false
    }

    private fun hideLoading() {
        Log.d(TAG, "hideLoading")
        binding.progressBar.visibility = View.GONE
        binding.swipeRefresh?.isRefreshing = false
    }

    private fun showTests(tests: List<TestAssignment>) {
        Log.d(TAG, "showTests: count=${tests.size}")
        if (tests.isEmpty()) {
            showEmptyState()
        } else {
            binding.rvTests.visibility = View.VISIBLE
            binding.layoutEmpty?.visibility = View.GONE
            testListAdapter.submitList(tests)

            updateTestCounts(tests)
        }
    }

    private fun updateTestCounts(tests: List<TestAssignment>) {
        val pending = tests.count { it.status == com.octal.examly.domain.model.TestAssignmentStatus.PENDING }
        val inProgress = tests.count { it.status == com.octal.examly.domain.model.TestAssignmentStatus.IN_PROGRESS }
        val completed = tests.count { it.status == com.octal.examly.domain.model.TestAssignmentStatus.COMPLETED }

        binding.tvTotalTests?.text = getString(R.string.total_tests, tests.size)
        binding.tvPendingTests?.text = getString(R.string.pending_tests, pending)
        binding.tvInProgressTests?.text = getString(R.string.in_progress_tests, inProgress)
        binding.tvCompletedTests?.text = getString(R.string.completed_tests, completed)
    }

    private fun showEmptyState() {
        Log.d(TAG, "showEmptyState")
        binding.rvTests.visibility = View.GONE
        binding.layoutEmpty?.visibility = View.VISIBLE
        binding.tvEmptyMessage?.text = getString(R.string.no_tests_assigned)
        binding.tvEmptySubtitle?.text = getString(R.string.no_tests_assigned_subtitle)
    }

    private fun showError(message: String) {
        binding.rvTests.visibility = View.GONE
        binding.layoutEmpty?.visibility = View.VISIBLE
        binding.tvEmptyMessage?.text = getString(R.string.error_loading_tests)
        binding.tvEmptySubtitle?.text = message
    }

    private fun updateFilterIndicator(filterState: FilterState) {
        Log.d(TAG, "updateFilterIndicator: active=${filterState.hasActiveFilters()} state=$filterState")
        val hasActiveFilters = filterState.hasActiveFilters()
        binding.ivFilterIndicator?.visibility = if (hasActiveFilters) {
            View.VISIBLE
        } else {
            View.GONE
        }

        if (hasActiveFilters) {
            val filterCount = filterState.getActiveFilterCount()
            Log.d(TAG, "updateFilterIndicator: filterCount=$filterCount")
            binding.tvFilterCount?.text = filterCount.toString()
            binding.tvFilterCount?.visibility = View.VISIBLE
        } else {
            binding.tvFilterCount?.visibility = View.GONE
        }
    }

    private fun refreshTests() {
        Log.d(TAG, "refreshTests: currentUserId=$currentUserId")
        currentUserId?.let { userId ->
            loadTests(userId)
        }
    }

    private fun navigateToTestSummary(assignment: TestAssignment) {
        Log.d(TAG, "navigateToTestSummary: assignmentId=${assignment.id} testId=${assignment.testId}")
        val action = TestListFragmentDirections
            .actionTestListFragmentToTestSummaryFragment(assignment.id)
        findNavController().navigate(action)
    }

    private fun resumeTest(assignment: TestAssignment) {
        val action = TestListFragmentDirections
            .actionTestListFragmentToTestSummaryFragment(assignment.id)
        findNavController().navigate(action)
    }

    private fun showFilterDialog() {
    }

    private fun clearFilters() {
        testListViewModel.clearFilters()
        refreshTests()
    }

    private fun sortTests(sortOption: String) {
        testListViewModel.sortTests(sortOption)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_test_list, menu)
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
            R.id.action_sort -> {
                showSortDialog()
                true
            }
            else -> false
        }
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf(
            getString(R.string.sort_by_deadline),
            getString(R.string.sort_by_subject),
            getString(R.string.sort_by_status),
            getString(R.string.sort_by_date_assigned)
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.sort_by)
            .setItems(sortOptions) { _, which ->
                when (which) {
                    0 -> sortTests("deadline")
                    1 -> sortTests("subject")
                    2 -> sortTests("status")
                    3 -> sortTests("assigned_date")
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
