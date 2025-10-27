package com.octal.examly.presentation.ui.fragment.admin

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
import com.octal.examly.databinding.FragmentSelectTestBinding
import com.octal.examly.domain.model.Test
import com.octal.examly.presentation.adapter.TestListAdapter
import com.octal.examly.presentation.state.UiState
import com.octal.examly.presentation.viewmodel.AssignTestViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SelectTestFragment : Fragment() {

    private var _binding: FragmentSelectTestBinding? = null
    private val binding get() = _binding!!

    private val assignTestViewModel: AssignTestViewModel by activityViewModels()
    private lateinit var testListAdapter: TestListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeTests()
        assignTestViewModel.loadAllTests()
    }

    private fun setupRecyclerView() {
        testListAdapter = TestListAdapter(
            onTestClick = { test ->
                selectTest(test)
            }
        )

        binding.rvTests.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = testListAdapter
        }
    }

    private fun observeTests() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                assignTestViewModel.testsState.collect { state ->
                    when (state) {
                        is UiState.Loading -> binding.progressBar.visibility = View.VISIBLE
                        is UiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            testListAdapter.submitList(state.data)
                        }
                        is UiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            showError(state.message)
                        }
                        else -> binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun selectTest(test: Test) {
        assignTestViewModel.setSelectedTest(test)
        assignTestViewModel.setCurrentStep(2)
        findNavController().navigate(R.id.action_selectTestFragment_to_selectUsersFragment)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
