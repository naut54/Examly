package com.octal.examly.presentation.ui.fragment.admin

import android.os.Bundle
import android.util.Log
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.octal.examly.R
import com.octal.examly.databinding.FragmentTestFixedSetupBinding
import com.octal.examly.domain.model.Question
import com.octal.examly.domain.model.Subject
import com.octal.examly.presentation.adapter.QuestionListAdapter
import com.octal.examly.presentation.state.UiState
import com.octal.examly.presentation.viewmodel.CreateQuestionViewModel
import com.octal.examly.presentation.viewmodel.CreateTestViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestFixedSetupFragment : Fragment() {

    private var _binding: FragmentTestFixedSetupBinding? = null
    private val binding get() = _binding!!

    private val createTestViewModel: CreateTestViewModel by activityViewModels()
    private val questionViewModel: CreateQuestionViewModel by viewModels()

    private lateinit var availableQuestionsAdapter: QuestionListAdapter
    private lateinit var selectedQuestionsAdapter: QuestionListAdapter

    private val selectedQuestions = mutableListOf<Question>()
    private var currentSubjectFilter: Long? = null

    companion object {
        private const val TAG = "TestFixedSetupFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestFixedSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupAvailableQuestionsRecyclerView()
        setupSelectedQuestionsRecyclerView()
        observeSubjects()
        observeQuestions()

        questionViewModel.loadSubjects()
    }

    private fun setupUI() {
        binding.btnNext.setOnClickListener {
            proceedToConfiguration()
        }

        binding.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnClearSelection?.setOnClickListener {
            clearSelection()
        }

        binding.etSearch?.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterQuestions(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterQuestions(newText)
                return true
            }
        })
    }

    private fun setupAvailableQuestionsRecyclerView() {
        availableQuestionsAdapter = QuestionListAdapter(
            onQuestionClick = { question ->
                toggleQuestionSelection(question)
            },
            onDeleteClick = { _ -> },
            onReorder = { _ -> }
        )

        binding.rvAvailableQuestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = availableQuestionsAdapter
        }
    }

    private fun setupSelectedQuestionsRecyclerView() {
        selectedQuestionsAdapter = QuestionListAdapter(
            onQuestionClick = { },
            onDeleteClick = { question ->
                removeQuestionFromSelection(question)
            },
            onReorder = { newOrder ->
                selectedQuestions.clear()
                selectedQuestions.addAll(newOrder)
            }
        )

        binding.rvSelectedQuestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = selectedQuestionsAdapter
        }

        val itemTouchHelper = ItemTouchHelper(QuestionListAdapter.DragCallback(selectedQuestionsAdapter))
        itemTouchHelper.attachToRecyclerView(binding.rvSelectedQuestions)
    }

    private fun observeSubjects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                questionViewModel.subjectsState.collect { state ->
                    when (state) {
                        is UiState.Success -> {
                            displaySubjectFilters(state.data)
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }

    private fun displaySubjectFilters(subjects: List<Subject>) {
        binding.chipGroupSubjects.removeAllViews()

        val allChip = Chip(requireContext()).apply {
            text = getString(R.string.all_subjects)
            isCheckable = true
            isChecked = true
            setOnClickListener {
                currentSubjectFilter = null
                loadQuestionsBySubject(null)
            }
        }
        binding.chipGroupSubjects.addView(allChip)

        subjects.forEach { subject ->
            val chip = Chip(requireContext()).apply {
                text = subject.name
                isCheckable = true
                setOnClickListener {
                    currentSubjectFilter = subject.id
                    loadQuestionsBySubject(subject.id)
                }
            }
            binding.chipGroupSubjects.addView(chip)
        }
    }

    private fun observeQuestions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                questionViewModel.questionsState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is UiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            availableQuestionsAdapter.submitList(state.data)
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

    private fun loadQuestionsBySubject(subjectId: Long?) {
        if (subjectId == null) {
            questionViewModel.loadAllQuestions()
        } else {
            questionViewModel.loadQuestionsBySubject(subjectId)
        }
    }

    private fun filterQuestions(query: String?) {
        questionViewModel.filterQuestions(query)
    }

    private fun toggleQuestionSelection(question: Question) {
        if (selectedQuestions.contains(question)) {
            removeQuestionFromSelection(question)
        } else {
            addQuestionToSelection(question)
        }
    }

    private fun addQuestionToSelection(question: Question) {
        if (!selectedQuestions.contains(question)) {
            Log.d(TAG, "addQuestionToSelection: Adding question ID=${question.id}, text='${question.questionText}'")
            selectedQuestions.add(question)
            updateSelectedQuestions()
        }
    }

    private fun removeQuestionFromSelection(question: Question) {
        Log.d(TAG, "removeQuestionFromSelection: Removing question ID=${question.id}")
        selectedQuestions.remove(question)
        updateSelectedQuestions()
    }

    private fun clearSelection() {
        Log.d(TAG, "clearSelection: Clearing all ${selectedQuestions.size} selected questions")
        selectedQuestions.clear()
        updateSelectedQuestions()
    }

    private fun updateSelectedQuestions() {
        selectedQuestionsAdapter.submitList(selectedQuestions.toList())
        updateSelectionCounter()
    }

    private fun updateSelectionCounter() {
        val count = selectedQuestions.size
        binding.tvSelectedCount?.text = getString(R.string.questions_selected, count)

        binding.btnNext.isEnabled = count > 0
    }

    private fun proceedToConfiguration() {
        Log.d(TAG, "proceedToConfiguration: Attempting to proceed with ${selectedQuestions.size} questions")

        if (selectedQuestions.isEmpty()) {
            Log.e(TAG, "proceedToConfiguration: No questions selected")
            Toast.makeText(
                requireContext(),
                getString(R.string.error_no_questions_selected),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        Log.d(TAG, "proceedToConfiguration: Saving ${selectedQuestions.size} questions to ViewModel")
        selectedQuestions.forEachIndexed { index, question ->
            Log.d(TAG, "  Question $index: ID=${question.id}, text='${question.questionText}'")
        }
        createTestViewModel.setSelectedQuestions(selectedQuestions)

        Log.d(TAG, "proceedToConfiguration: Navigating to configuration screen")
        findNavController().navigate(R.id.action_fixedSetup_to_configuration)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
