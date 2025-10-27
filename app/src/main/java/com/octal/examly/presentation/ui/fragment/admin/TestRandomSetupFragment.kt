package com.octal.examly.presentation.ui.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.octal.examly.R
import com.octal.examly.databinding.FragmentTestRandomSetupBinding
import com.octal.examly.domain.model.Subject
import com.octal.examly.presentation.state.UiState
import com.octal.examly.presentation.viewmodel.CreateQuestionViewModel
import com.octal.examly.presentation.viewmodel.CreateTestViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestRandomSetupFragment : Fragment() {

    private var _binding: FragmentTestRandomSetupBinding? = null
    private val binding get() = _binding!!

    private val createTestViewModel: CreateTestViewModel by activityViewModels()
    private val questionViewModel: CreateQuestionViewModel by viewModels()

    private val selectedSubjects = mutableListOf<Subject>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestRandomSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeSubjects()
        observeQuestionCounts()

        questionViewModel.loadSubjects()
    }

    private fun setupUI() {
        binding.btnNext.setOnClickListener {
            proceedToConfiguration()
        }

        binding.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.etQuestionCount.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                validateInputs()
            }
        })
    }

    private fun observeSubjects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                questionViewModel.subjectsState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is UiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            displaySubjectChips(state.data)
                        }

                        is UiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            showError(state.message)
                        }

                        else -> {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun displaySubjectChips(subjects: List<Subject>) {
        binding.chipGroupSubjects.removeAllViews()

        subjects.forEach { subject ->
            val chip = Chip(requireContext()).apply {
                text = subject.name
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedSubjects.add(subject)
                        questionViewModel.loadQuestionCountBySubject(subject.id)
                    } else {
                        selectedSubjects.remove(subject)
                    }
                    validateInputs()
                }
            }
            binding.chipGroupSubjects.addView(chip)
        }
    }

    private fun observeQuestionCounts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                questionViewModel.questionCounts.collect { counts ->
                    displayQuestionCounts(counts)
                }
            }
        }
    }

    private fun displayQuestionCounts(counts: Map<Long, Int>) {
        val totalAvailable = selectedSubjects.sumOf { subject ->
            counts[subject.id] ?: 0
        }

        if (selectedSubjects.isNotEmpty()) {
            binding.tvAvailableQuestions?.text = getString(
                R.string.available_questions_count,
                totalAvailable
            )
            binding.tvAvailableQuestions?.visibility = View.VISIBLE
        } else {
            binding.tvAvailableQuestions?.visibility = View.GONE
        }
    }

    private fun validateInputs() {
        val questionCount = binding.etQuestionCount.text.toString().toIntOrNull() ?: 0
        val hasSubjects = selectedSubjects.isNotEmpty()
        val hasValidCount = questionCount > 0

        binding.btnNext.isEnabled = hasSubjects && hasValidCount

        if (!hasSubjects && binding.chipGroupSubjects.childCount > 0) {
            binding.tvSubjectError?.text = getString(R.string.error_no_subject_selected)
            binding.tvSubjectError?.visibility = View.VISIBLE
        } else {
            binding.tvSubjectError?.visibility = View.GONE
        }

        if (questionCount == 0 && (binding.etQuestionCount.text?.isNotEmpty() == true)) {
            binding.tilQuestionCount.error = getString(R.string.error_invalid_question_count)
        } else {
            binding.tilQuestionCount.error = null
        }
    }

    private fun proceedToConfiguration() {
        if (selectedSubjects.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_no_subject_selected),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val questionCount = binding.etQuestionCount.text.toString().toIntOrNull()
        if (questionCount == null || questionCount <= 0) {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_invalid_question_count),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        createTestViewModel.setRandomConfiguration(
            subjects = selectedSubjects,
            questionCount = questionCount
        )

        findNavController().navigate(R.id.action_randomSetup_to_configuration)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
