package com.octal.examly.presentation.ui.fragment.admin

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.octal.examly.R
import com.octal.examly.databinding.FragmentSelectUsersBinding
import com.octal.examly.domain.model.User
import com.octal.examly.presentation.adapter.UserListAdapter
import com.octal.examly.presentation.state.UiState
import com.octal.examly.presentation.viewmodel.AssignTestViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class SelectUsersFragment : Fragment() {

    private var _binding: FragmentSelectUsersBinding? = null
    private val binding get() = _binding!!

    private val assignTestViewModel: AssignTestViewModel by activityViewModels()
    private lateinit var userListAdapter: UserListAdapter

    private val selectedUsers = mutableListOf<User>()
    private var deadlineTimestamp: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        observeUsers()
        observeAssignmentResult()
        assignTestViewModel.loadUsers()
    }

    private fun setupUI() {
        binding.btnSelectDeadline.setOnClickListener {
            showDatePicker()
        }

        binding.btnAssign.setOnClickListener {
            assignTest()
        }

        binding.btnSelectAll?.setOnClickListener {
            selectAllUsers()
        }

        binding.btnDeselectAll?.setOnClickListener {
            deselectAllUsers()
        }
    }

    private fun setupRecyclerView() {
        userListAdapter = UserListAdapter { selectedSet ->
            selectedUsers.clear()
            selectedUsers.addAll(selectedSet)
            updateSelectedCount()
        }

        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userListAdapter
        }
    }

    private fun observeUsers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                assignTestViewModel.usersState.collect { state ->
                    when (state) {
                        is UiState.Success -> userListAdapter.submitList(state.data)
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
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.success_test_assigned),
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().navigate(R.id.action_selectUsers_to_adminHome)
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

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day, 23, 59, 59)
                deadlineTimestamp = calendar.timeInMillis
                binding.tvDeadline.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(Date(deadlineTimestamp))
                validateInputs()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    private fun selectAllUsers() {
        userListAdapter.selectAll()
    }

    private fun deselectAllUsers() {
        userListAdapter.clearSelections()
        selectedUsers.clear()
        updateSelectedCount()
    }

    private fun updateSelectedCount() {
        binding.tvSelectionCount?.text = getString(R.string.users_selected, selectedUsers.size)
        validateInputs()
    }

    private fun validateInputs() {
        binding.btnAssign.isEnabled = selectedUsers.isNotEmpty() && deadlineTimestamp > 0
    }

    private fun assignTest() {
        if (selectedUsers.isEmpty()) {
            showError(getString(R.string.error_no_users_selected))
            return
        }

        if (deadlineTimestamp == 0L) {
            showError(getString(R.string.error_no_deadline_selected))
            return
        }

        assignTestViewModel.assignTestToUsers(selectedUsers.map { it.id }, deadlineTimestamp)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
