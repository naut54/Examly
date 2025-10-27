package com.octal.examly.presentation.ui.fragment.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.octal.examly.R
import com.octal.examly.databinding.FragmentAssignTestBinding
import com.octal.examly.domain.model.Test
import com.octal.examly.domain.model.User
import com.octal.examly.presentation.adapter.TestListAdapter
import com.octal.examly.presentation.adapter.UserListAdapter
import com.octal.examly.presentation.state.UiState
import com.octal.examly.presentation.viewmodel.AssignTestViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AssignTestFragment : Fragment() {

    private var _binding: FragmentAssignTestBinding? = null
    private val binding get() = _binding!!

    private val assignTestViewModel: AssignTestViewModel by activityViewModels()

    private var selectedTest: Test? = null
    private val selectedUsers = mutableListOf<User>()
    private var deadlineTimestamp: Long = 0L

    private var allTests: List<Test> = emptyList()
    private var allUsers: List<User> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssignTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupBackPressedHandler()
        observeTests()
        observeUsers()
        observeAssignmentResult()

        assignTestViewModel.loadAllTests()
        assignTestViewModel.loadUsers()
    }

    private fun setupBackPressedHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showCancelConfirmation()
                }
            }
        )
    }

    private fun setupUI() {
        binding.tvTitle?.text = getString(R.string.assign_test)

        binding.btnCancel?.setOnClickListener { showCancelConfirmation() }

        binding.cardSelectTest.setOnClickListener { showTestPickerDialog() }

        binding.cardSelectUsers.setOnClickListener { showUsersPickerDialog() }

        binding.switchDeadline.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutDeadlineSelector.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                deadlineTimestamp = 0L
                binding.tvSelectedDeadline.visibility = View.GONE
                updateAssignButtonState()
            }
        }
        binding.btnSelectDate.setOnClickListener { showDatePicker() }
        binding.btnSelectTime.setOnClickListener { showTimePicker() }

        binding.btnAssign.isEnabled = false
        binding.btnAssign.setOnClickListener { onAssignClicked() }
    }

    private fun observeTests() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                assignTestViewModel.testsState.collect { state ->
                    when (state) {
                        is UiState.Success -> allTests = state.data
                        is UiState.Error -> showError(state.message)
                        else -> {}
                    }
                }
            }
        }
    }

    private fun observeUsers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                assignTestViewModel.usersState.collect { state ->
                    when (state) {
                        is UiState.Success -> allUsers = state.data
                        is UiState.Error -> showError(state.message)
                        else -> {}
                    }
                }
            }
        }
    }

    private fun observeAssignmentResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                assignTestViewModel.assignmentState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.btnAssign.isEnabled = false
                            binding.progressBar?.visibility = View.VISIBLE
                        }
                        is UiState.Success -> {
                            binding.progressBar?.visibility = View.GONE
                            Toast.makeText(requireContext(), getString(R.string.success_test_assigned), Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                        is UiState.Error -> {
                            binding.progressBar?.visibility = View.GONE
                            binding.btnAssign.isEnabled = true
                            showError(state.message)
                        }
                        else -> {
                            binding.progressBar?.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun showTestPickerDialog() {
        if (allTests.isEmpty()) {
            showError(getString(R.string.select_test_no_tests))
            return
        }

        val recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
        val adapter = TestListAdapter { test ->
            selectedTest = test
            assignTestViewModel.setSelectedTest(test)
            binding.tvSelectedTest.text = test.title
            updateAssignButtonState()
        }
        recyclerView.adapter = adapter
        adapter.submitList(allTests)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.assign_test_select_test)
            .setView(recyclerView)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun showUsersPickerDialog() {
        if (allUsers.isEmpty()) {
            showError(getString(R.string.no_users_available))
            return
        }

        val content = layoutInflater.inflate(R.layout.view_user_selector, FrameLayout(requireContext()), false)
        val rv: RecyclerView = content.findViewById(R.id.rv_users)
        val tvCount: android.widget.TextView = content.findViewById(R.id.tv_selected_count)
        val btnSelectAll: com.google.android.material.button.MaterialButton = content.findViewById(R.id.btn_select_all)
        val btnDeselectAll: com.google.android.material.button.MaterialButton = content.findViewById(R.id.btn_deselect_all)
        val searchView: SearchView = content.findViewById(R.id.search_view)

        val adapter = UserListAdapter { selected ->
            selectedUsers.clear()
            selectedUsers.addAll(selected)
            tvCount.text = getString(R.string.users_selected, selectedUsers.size)
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        adapter.submitList(allUsers)

        btnSelectAll.setOnClickListener {
            adapter.selectAll()
            selectedUsers.clear()
            selectedUsers.addAll(adapter.getSelectedUsers())
            tvCount.text = getString(R.string.users_selected, selectedUsers.size)
        }
        btnDeselectAll.setOnClickListener {
            adapter.clearSelections()
            selectedUsers.clear()
            tvCount.text = getString(R.string.users_selected, 0)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterUsers(adapter, query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsers(adapter, newText)
                return true
            }
        })

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.assign_test_select_users)
            .setView(content)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                binding.tvSelectedUsers.text = getString(R.string.users_selected, selectedUsers.size)
                updateAssignButtonState()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun filterUsers(adapter: UserListAdapter, query: String?) {
        val q = query?.trim()?.lowercase(Locale.getDefault()).orEmpty()
        if (q.isEmpty()) {
            adapter.submitList(allUsers)
        } else {
            adapter.submitList(allUsers.filter { it.username.lowercase(Locale.getDefault()).contains(q) })
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                if (deadlineTimestamp == 0L) {
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                } else {
                    val prev = Calendar.getInstance().apply { timeInMillis = deadlineTimestamp }
                    calendar.set(Calendar.HOUR_OF_DAY, prev.get(Calendar.HOUR_OF_DAY))
                    calendar.set(Calendar.MINUTE, prev.get(Calendar.MINUTE))
                    calendar.set(Calendar.SECOND, prev.get(Calendar.SECOND))
                }
                deadlineTimestamp = calendar.timeInMillis
                showSelectedDeadline()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    private fun showTimePicker() {
        val cal = Calendar.getInstance()
        if (deadlineTimestamp > 0) cal.timeInMillis = deadlineTimestamp
        val initialHour = cal.get(Calendar.HOUR_OF_DAY)
        val initialMinute = cal.get(Calendar.MINUTE)
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val dateCal = Calendar.getInstance()
                if (deadlineTimestamp > 0) {
                    dateCal.timeInMillis = deadlineTimestamp
                }
                dateCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                dateCal.set(Calendar.MINUTE, minute)
                dateCal.set(Calendar.SECOND, 0)
                deadlineTimestamp = dateCal.timeInMillis
                showSelectedDeadline()
            },
            initialHour,
            initialMinute,
            true
        ).show()
    }

    private fun showSelectedDeadline() {
        if (deadlineTimestamp > 0) {
            val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.tvSelectedDeadline.text = fmt.format(Date(deadlineTimestamp))
            binding.tvSelectedDeadline.visibility = View.VISIBLE
        } else {
            binding.tvSelectedDeadline.visibility = View.GONE
        }
        updateAssignButtonState()
    }

    private fun onAssignClicked() {
        val users = selectedUsers.toList()
        if (selectedTest == null) {
            showError(getString(R.string.error_no_test_selected))
            return
        }
        if (users.isEmpty()) {
            showError(getString(R.string.error_no_users_selected))
            return
        }
        val requireDeadline = binding.switchDeadline.isChecked
        val finalDeadline = if (requireDeadline) {
            if (deadlineTimestamp == 0L) {
                showError(getString(R.string.error_no_deadline_selected))
                return
            }
            deadlineTimestamp
        } else 0L
        assignTestViewModel.assignTestToUsers(users.map { it.id }, finalDeadline)
    }

    private fun updateAssignButtonState() {
        val requireDeadline = binding.switchDeadline.isChecked
        binding.btnAssign.isEnabled = selectedTest != null && selectedUsers.isNotEmpty() && (!requireDeadline || deadlineTimestamp > 0)
    }

    private fun showCancelConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.cancel_assignment)
            .setMessage(R.string.cancel_assignment_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
