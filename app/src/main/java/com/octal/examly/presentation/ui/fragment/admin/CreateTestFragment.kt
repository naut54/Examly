package com.octal.examly.presentation.ui.fragment.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.octal.examly.R
import com.octal.examly.databinding.FragmentCreateTestBinding
import com.octal.examly.domain.model.Question
import com.octal.examly.domain.model.Subject
import com.octal.examly.domain.model.TestMode
import com.octal.examly.presentation.adapter.QuestionListAdapter
import com.octal.examly.presentation.state.UiState
import com.octal.examly.presentation.viewmodel.CreateQuestionViewModel
import com.octal.examly.presentation.viewmodel.CreateTestViewModel
import com.octal.examly.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateTestFragment : Fragment() {

    private var _binding: FragmentCreateTestBinding? = null
    private val binding get() = _binding!!

    private val createTestViewModel: CreateTestViewModel by activityViewModels()
    private val questionViewModel: CreateQuestionViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var availableQuestionsAdapter: QuestionListAdapter
    private lateinit var selectedQuestionsAdapter: QuestionListAdapter

    private val selectedQuestions = mutableListOf<Question>()
    private val selectedRandomSubjects = mutableListOf<Subject>()
    private var currentSubjectFilter: Long? = null

    companion object {
        private const val TAG = "CreateTestFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupBackPressedHandler()
        setupQuestionRecyclerViews()
        observeSubjects()
        observeQuestions()
        observeCreationState()

        questionViewModel.loadSubjects()
        questionViewModel.loadAllQuestions()
    }

    private fun setupUI() {
        binding.btnCancel.setOnClickListener {
            showCancelConfirmation()
        }

        binding.btnCreateTest.setOnClickListener {
            createTest()
        }

        binding.radioGroupMode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbFixedMode -> showFixedModeUI()
                R.id.rbRandomMode -> showRandomModeUI()
            }
        }

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                questionViewModel.filterQuestions(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                questionViewModel.filterQuestions(newText)
                return true
            }
        })

        binding.switchTimer.setOnCheckedChangeListener { _, isChecked ->
            binding.tilTimeLimit.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.etTestName.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                validateInputs()
            }
        })

        binding.actvSubject.setOnItemClickListener { _, _, _, _ ->
            validateInputs()
        }

        showFixedModeUI()
    }

    private fun setupQuestionRecyclerViews() {
        availableQuestionsAdapter = QuestionListAdapter(
            onQuestionClick = { question ->
                addQuestionToSelection(question)
            },
            onDeleteClick = { _ -> },
            onReorder = { _ -> }
        )

        binding.rvAvailableQuestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = availableQuestionsAdapter
        }

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

    private fun showFixedModeUI() {
        binding.cardFixedMode.visibility = View.VISIBLE
        binding.cardRandomMode.visibility = View.GONE
    }

    private fun showRandomModeUI() {
        binding.cardFixedMode.visibility = View.GONE
        binding.cardRandomMode.visibility = View.VISIBLE
    }

    private fun observeSubjects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                createTestViewModel.subjects.collect { subjects ->
                    setupSubjectDropdown(subjects)
                    setupSubjectFilterChips(subjects)
                    setupRandomSubjectChips(subjects)
                }
            }
        }
    }

    private fun setupSubjectDropdown(subjects: List<Subject>) {
        val subjectNames = subjects.map { it.name }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            subjectNames
        )
        binding.actvSubject.setAdapter(adapter)
    }

    private fun setupSubjectFilterChips(subjects: List<Subject>) {
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

    private fun setupRandomSubjectChips(subjects: List<Subject>) {
        binding.chipGroupRandomSubjects.removeAllViews()

        subjects.forEach { subject ->
            val chip = Chip(requireContext()).apply {
                text = subject.name
                isCheckable = true
                isChecked = false
                setOnClickListener {
                    if (isChecked) {
                        selectedRandomSubjects.add(subject)
                    } else {
                        selectedRandomSubjects.remove(subject)
                    }
                    validateInputs()
                }
            }
            binding.chipGroupRandomSubjects.addView(chip)
        }
    }

    private fun observeQuestions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                questionViewModel.questionsState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                        }
                        is UiState.Success -> {
                            availableQuestionsAdapter.submitList(state.data)
                        }
                        is UiState.Error -> {
                            showError(state.message)
                        }
                        else -> {}
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

    private fun addQuestionToSelection(question: Question) {
        if (!selectedQuestions.contains(question)) {
            Log.d(TAG, "Adding question ID=${question.id}")
            selectedQuestions.add(question)
            updateSelectedQuestions()
        }
    }

    private fun removeQuestionFromSelection(question: Question) {
        Log.d(TAG, "Removing question ID=${question.id}")
        selectedQuestions.remove(question)
        updateSelectedQuestions()
    }

    private fun updateSelectedQuestions() {
        selectedQuestionsAdapter.submitList(selectedQuestions.toList())
        binding.tvSelectedCount.text = getString(R.string.questions_selected, selectedQuestions.size)
        validateInputs()
    }

    private fun observeCreationState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                createTestViewModel.creationState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            showLoading()
                        }
                        is UiState.Success -> {
                            hideLoading()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.success_test_created),
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().navigateUp()
                        }
                        is UiState.Error -> {
                            hideLoading()
                            showError(state.message)
                        }
                        else -> {
                            hideLoading()
                        }
                    }
                }
            }
        }
    }

    private fun validateInputs() {
        val title = binding.etTestName.text.toString().trim()
        val selectedSubjectName = binding.actvSubject.text.toString().trim()
        val selectedMode = getSelectedMode()

        var isValid = title.isNotEmpty() && selectedSubjectName.isNotEmpty()

        when (selectedMode) {
            TestMode.FIXED -> {
                isValid = isValid && selectedQuestions.isNotEmpty()
            }
            TestMode.RANDOM -> {
                val questionCount = binding.etQuestionCount.text.toString().toIntOrNull() ?: 0
                isValid = isValid && selectedRandomSubjects.isNotEmpty() && questionCount > 0
            }
        }

        binding.btnCreateTest.isEnabled = isValid

        if (title.isEmpty() && binding.etTestName.hasFocus()) {
            binding.tilTestName.error = getString(R.string.error_empty_title)
        } else {
            binding.tilTestName.error = null
        }

        if (selectedSubjectName.isEmpty() && binding.actvSubject.hasFocus()) {
            binding.tilSubject.error = getString(R.string.error_no_subject_selected)
        } else {
            binding.tilSubject.error = null
        }
    }

    private fun getSelectedMode(): TestMode {
        return when (binding.radioGroupMode.checkedRadioButtonId) {
            R.id.rbFixedMode -> TestMode.FIXED
            R.id.rbRandomMode -> TestMode.RANDOM
            else -> TestMode.FIXED
        }
    }

    private fun createTest() {
        Log.d(TAG, "createTest: Starting")

        val currentUser = mainViewModel.currentUser.value
        if (currentUser == null) {
            showError(getString(R.string.error_no_user_session))
            return
        }

        val title = binding.etTestName.text.toString().trim()
        val description = binding.etTestDescription.text.toString().trim()
        val selectedSubjectName = binding.actvSubject.text.toString().trim()

        if (title.isEmpty()) {
            binding.tilTestName.error = getString(R.string.error_empty_title)
            return
        }

        if (selectedSubjectName.isEmpty()) {
            binding.tilSubject.error = getString(R.string.error_no_subject_selected)
            return
        }

        val subject = createTestViewModel.subjects.value.find { it.name == selectedSubjectName }
        if (subject == null) {
            binding.tilSubject.error = getString(R.string.error_invalid_subject)
            return
        }

        val mode = getSelectedMode()
        Log.d(TAG, "createTest: mode=$mode")

        val hasTimer = binding.switchTimer.isChecked
        val timeLimit = if (hasTimer) {
            binding.etTimeLimit.text.toString().toIntOrNull()
        } else {
            null
        }

        val hasPracticeMode = binding.switchPracticeMode.isChecked

        createTestViewModel.setTestBasicInfo(title, description, subject.id)
        createTestViewModel.setTestMode(mode)
        createTestViewModel.setTimerConfiguration(hasTimer, timeLimit)
        createTestViewModel.setPracticeMode(hasPracticeMode)

        when (mode) {
            TestMode.FIXED -> {
                if (selectedQuestions.isEmpty()) {
                    showError(getString(R.string.error_no_questions_selected))
                    return
                }
                Log.d(TAG, "createTest: FIXED mode - ${selectedQuestions.size} questions")
                selectedQuestions.forEachIndexed { index, question ->
                    Log.d(TAG, "  Question $index: ID=${question.id}")
                }
                createTestViewModel.setSelectedQuestions(selectedQuestions)
            }
            TestMode.RANDOM -> {
                val questionCount = binding.etQuestionCount.text.toString().toIntOrNull() ?: 0
                if (selectedRandomSubjects.isEmpty() || questionCount <= 0) {
                    showError("Please select subjects and enter question count")
                    return
                }
                Log.d(TAG, "createTest: RANDOM mode - $questionCount questions from ${selectedRandomSubjects.size} subjects")
                createTestViewModel.setRandomConfiguration(selectedRandomSubjects, questionCount)
            }
        }

        Log.d(TAG, "createTest: Calling ViewModel.createTest()")
        createTestViewModel.createTest(createdBy = currentUser.id)
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

    private fun showCancelConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.cancel_test_creation)
            .setMessage(R.string.cancel_test_creation_message)
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton(R.string.yes) { _, _ ->
                cancelCreation()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun cancelCreation() {
        createTestViewModel.resetTestCreation()
        findNavController().navigateUp()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutContent.isEnabled = false
        binding.btnCreateTest.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.layoutContent.isEnabled = true
        validateInputs()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
