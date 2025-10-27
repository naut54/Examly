package com.octal.examly.presentation.ui.fragment.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.octal.examly.R
import com.octal.examly.databinding.FragmentTestConfigurationBinding
import com.octal.examly.domain.model.Subject
import com.octal.examly.presentation.state.UiState
import com.octal.examly.presentation.viewmodel.CreateTestViewModel
import com.octal.examly.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestConfigurationFragment : Fragment() {

    private var _binding: FragmentTestConfigurationBinding? = null
    private val binding get() = _binding!!

    private val createTestViewModel: CreateTestViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    companion object {
        private const val TAG = "TestConfigFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestConfigurationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        displayConfigurationSummary()
        observeCreationState()
        observeSubjects()
    }

    private fun setupUI() {
        Log.d(TAG, "setupUI: Initializing UI components")

        binding.btnCreateTest.setOnClickListener {
            Log.d(TAG, "btnCreateTest clicked")
            createTest()
        }

        binding.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.switchTimer.setOnCheckedChangeListener { _, isChecked ->
            binding.tilTimeLimit?.visibility = if (isChecked) View.VISIBLE else View.GONE
            val timeLimit = if (isChecked) {
                binding.etTimeLimit.text.toString().toIntOrNull()
            } else {
                null
            }
            createTestViewModel.setTimerConfiguration(isChecked, timeLimit)
        }

        binding.switchPracticeMode.setOnCheckedChangeListener { _, isChecked ->
            createTestViewModel.setPracticeMode(isChecked)
        }

        binding.etTimeLimit.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (binding.switchTimer.isChecked) {
                    val timeLimit = s.toString().toIntOrNull()
                    createTestViewModel.setTimerConfiguration(true, timeLimit)
                }
            }
        })

        binding.etTitle.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                validateInputs()
            }
        })

        binding.actvSubject.setOnItemClickListener { _, _, _, _ ->
            validateInputs()
        }
    }

    private fun observeSubjects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                createTestViewModel.subjects.collect { subjects ->
                    setupSubjectDropdown(subjects)
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

    private fun displayConfigurationSummary() {
        val testMode = createTestViewModel.getTestMode()
        val questionCount = createTestViewModel.getQuestionCount()
        val hasTimer = createTestViewModel.hasTimer()
        val hasPracticeMode = createTestViewModel.hasPracticeMode()

        binding.tvMode?.text = when (testMode) {
            com.octal.examly.domain.model.TestMode.FIXED -> getString(R.string.fixed_mode)
            com.octal.examly.domain.model.TestMode.RANDOM -> getString(R.string.random_mode)
            else -> ""
        }

        binding.tvQuestionCount?.text = getString(R.string.question_count, questionCount)

        if (hasTimer) {
            val timeLimit = createTestViewModel.getTimeLimit()
            binding.tvTimer?.text = getString(R.string.time_limit_minutes, timeLimit)
        } else {
            binding.tvTimer?.text = getString(R.string.no_time_limit)
        }

        binding.tvPracticeMode?.text = if (hasPracticeMode) {
            getString(R.string.practice_mode_available)
        } else {
            getString(R.string.practice_mode_not_available)
        }

        if (testMode == com.octal.examly.domain.model.TestMode.RANDOM) {
            val subjects = createTestViewModel.getSelectedSubjects()
            binding.tvSubjects?.text = subjects.joinToString(", ") { it.name }
            binding.layoutSubjects?.visibility = View.VISIBLE
        } else {
            binding.layoutSubjects?.visibility = View.GONE
        }
    }

    private fun observeCreationState() {
        Log.d(TAG, "observeCreationState: Setting up state observer")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                createTestViewModel.creationState.collect { state ->
                    Log.d(TAG, "observeCreationState: State changed to ${state::class.simpleName}")
                    when (state) {
                        is UiState.Loading -> {
                            Log.d(TAG, "observeCreationState: Loading state")
                            showLoading()
                        }

                        is UiState.Success -> {
                            Log.d(TAG, "observeCreationState: Success state - Test ID=${state.data}")
                            hideLoading()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.success_test_created),
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().navigate(R.id.adminHomeFragment)
                        }

                        is UiState.Error -> {
                            Log.e(TAG, "observeCreationState: Error state - ${state.message}")
                            hideLoading()
                            showError(state.message)
                        }

                        else -> {
                            Log.d(TAG, "observeCreationState: Other state (${state::class.simpleName})")
                            hideLoading()
                        }
                    }
                }
            }
        }
    }

    private fun validateInputs() {
        val title = binding.etTitle.text.toString().trim()
        val selectedSubjectName = binding.actvSubject.text.toString().trim()

        val isTitleValid = title.isNotEmpty()
        val isSubjectValid = selectedSubjectName.isNotEmpty()
        val isValid = isTitleValid && isSubjectValid

        binding.btnCreateTest.isEnabled = isValid

        if (title.isEmpty() && binding.etTitle.hasFocus()) {
            binding.tilTitle.error = getString(R.string.error_empty_title)
        } else {
            binding.tilTitle.error = null
        }

        if (selectedSubjectName.isEmpty() && binding.actvSubject.hasFocus()) {
            binding.tilSubject.error = getString(R.string.error_no_subject_selected)
        } else {
            binding.tilSubject.error = null
        }
    }

    private fun createTest() {
        Log.d(TAG, "createTest: Starting test creation")

        val currentUser = mainViewModel.currentUser.value
        Log.d(TAG, "createTest: currentUser=${currentUser?.id}, username=${currentUser?.username}")

        if (currentUser == null) {
            Log.e(TAG, "createTest: No user session found")
            showError(getString(R.string.error_no_user_session))
            return
        }

        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val selectedSubjectName = binding.actvSubject.text.toString().trim()
        Log.d(TAG, "createTest: title='$title', description='$description', subject='$selectedSubjectName'")

        if (title.isEmpty()) {
            Log.e(TAG, "createTest: Title is empty")
            binding.tilTitle.error = getString(R.string.error_empty_title)
            return
        }

        if (selectedSubjectName.isEmpty()) {
            Log.e(TAG, "createTest: Subject not selected")
            binding.tilSubject.error = getString(R.string.error_no_subject_selected)
            return
        }

        val subject = createTestViewModel.subjects.value.find { it.name == selectedSubjectName }
        if (subject == null) {
            Log.e(TAG, "createTest: Invalid subject selected")
            binding.tilSubject.error = getString(R.string.error_invalid_subject)
            return
        }

        Log.d(TAG, "createTest: Setting test basic info with subjectId=${subject.id}")
        createTestViewModel.setTestBasicInfo(title, description, subject.id)

        val testMode = createTestViewModel.getTestMode()
        val questionCount = createTestViewModel.getQuestionCount()
        Log.d(TAG, "createTest: testMode=$testMode, questionCount=$questionCount")

        Log.d(TAG, "createTest: Calling ViewModel.createTest()")
        createTestViewModel.createTest(createdBy = currentUser.id)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCreateTest.isEnabled = false
        binding.etTitle.isEnabled = false
        binding.etDescription.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnCreateTest.isEnabled = true
        binding.etTitle.isEnabled = true
        binding.etDescription.isEnabled = true
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
