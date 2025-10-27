package com.octal.examly.presentation.ui.fragment.admin

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.octal.examly.R
import com.octal.examly.databinding.FragmentCreateQuestionBinding
import com.octal.examly.domain.model.QuestionType
import com.octal.examly.domain.model.Subject
import com.octal.examly.presentation.adapter.EditableAnswerOptionsAdapter
import com.octal.examly.presentation.state.UiState
import com.octal.examly.presentation.viewmodel.CreateQuestionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateQuestionFragment : Fragment() {

    private var _binding: FragmentCreateQuestionBinding? = null
    private val binding get() = _binding!!

    private val createQuestionViewModel: CreateQuestionViewModel by viewModels()
    private lateinit var answerOptionsAdapter: EditableAnswerOptionsAdapter

    private val answers = mutableListOf<Pair<String, Boolean>>()
    private var selectedSubjectId: Long? = null
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivQuestionImage?.setImageURI(it)
            binding.ivQuestionImage?.visibility = View.VISIBLE
            binding.btnRemoveImage?.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupAnswersRecyclerView()
        observeSubjects()
        observeCreationState()

        createQuestionViewModel.loadSubjects()
    }

    private fun setupUI() {
        binding.btnAddImage?.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnRemoveImage?.setOnClickListener {
            selectedImageUri = null
            binding.ivQuestionImage?.visibility = View.GONE
            binding.btnRemoveImage?.visibility = View.GONE
        }

        binding.chipSingleChoice.isChecked = true

        binding.btnAddAnswer.setOnClickListener {
            addAnswer()
        }

        binding.btnCreateQuestion.setOnClickListener {
            createQuestion()
        }

        binding.btnCancel?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupAnswersRecyclerView() {
        answerOptionsAdapter = EditableAnswerOptionsAdapter(
            editable = true,
            onAnswerTextChanged = { position, text ->
                if (position < answers.size) {
                    answers[position] = answers[position].copy(first = text)
                }
            },
            onCorrectChanged = { position, isCorrect ->
                if (position < answers.size) {
                    answers[position] = answers[position].copy(second = isCorrect)
                }
            },
            onRemoveClick = { position ->
                if (position < answers.size) {
                    answers.removeAt(position)
                    updateAnswers()
                }
            }
        )

        binding.rvAnswers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = answerOptionsAdapter
        }
    }

    private fun observeSubjects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                createQuestionViewModel.subjectsState.collect { state ->
                    when (state) {
                        is UiState.Success -> displaySubjects(state.data)
                        is UiState.Error -> showError(state.message)
                        else -> {}
                    }
                }
            }
        }
    }

    private fun displaySubjects(subjects: List<Subject>) {
        binding.chipGroupSubjects.removeAllViews()

        subjects.forEach { subject ->
            val chip = Chip(requireContext()).apply {
                text = subject.name
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedSubjectId = subject.id
                    } else {
                        if (selectedSubjectId == subject.id) {
                            selectedSubjectId = null
                        }
                    }
                }
            }
            binding.chipGroupSubjects.addView(chip)
        }
    }

    private fun observeCreationState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                createQuestionViewModel.creationState.collect { state ->
                    when (state) {
                        is UiState.Loading -> showLoading()
                        is UiState.Success -> {
                            hideLoading()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.success_question_created),
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().navigateUp()
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

    private fun addAnswer() {
        answers.add(Pair("", false))
        updateAnswers()
    }

    private fun updateAnswers() {
        answerOptionsAdapter.submitList(answers.toList())
        binding.tvAnswerCount?.text = getString(R.string.answers_count, answers.size)
    }

    private fun createQuestion() {
        val questionText = binding.etQuestionText.text.toString().trim()
        val explanation = binding.etExplanation.text.toString().trim()
        val type = if (binding.chipSingleChoice.isChecked) {
            QuestionType.SINGLE_CHOICE
        } else {
            QuestionType.MULTIPLE_CHOICE
        }

        if (selectedSubjectId == null) {
            showError(getString(R.string.error_no_subject_selected))
            return
        }

        if (questionText.isEmpty()) {
            binding.tilQuestionText.error = getString(R.string.error_empty_question)
            return
        }

        if (answers.size < 2) {
            showError(getString(R.string.error_minimum_answers))
            return
        }

        if (answers.none { it.second }) {
            showError(getString(R.string.error_no_correct_answer))
            return
        }

        val validAnswers = answers.filter { it.first.isNotBlank() }
        if (validAnswers.size < 2) {
            showError(getString(R.string.error_minimum_answers))
            return
        }

        createQuestionViewModel.createQuestion(
            subjectId = selectedSubjectId!!,
            questionText = questionText,
            imageUri = selectedImageUri?.toString(),
            type = type,
            answers = validAnswers,
            explanation = explanation.ifEmpty { null }
        )
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCreateQuestion.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnCreateQuestion.isEnabled = true
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
