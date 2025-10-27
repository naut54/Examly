package com.octal.examly.presentation.ui.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.octal.examly.R
import com.octal.examly.databinding.FragmentCreateSubjectBinding
import com.octal.examly.presentation.state.UiState
import com.octal.examly.presentation.viewmodel.CreateSubjectViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateSubjectFragment : Fragment() {

    private var _binding: FragmentCreateSubjectBinding? = null
    private val binding get() = _binding!!

    private val createSubjectViewModel: CreateSubjectViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeCreationState()
    }

    private fun setupUI() {
        binding.btnCreateSubject.setOnClickListener {
            createSubject()
        }

        binding.btnCancel?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.etName.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                validateInputs()
            }
        })
    }

    private fun observeCreationState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                createSubjectViewModel.creationState.collect { state ->
                    when (state) {
                        is UiState.Loading -> showLoading()
                        is UiState.Success -> {
                            hideLoading()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.success_subject_created),
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

    private fun validateInputs() {
        val name = binding.etName.text.toString().trim()
        val isValid = name.isNotEmpty()

        if (name.isEmpty() && binding.etName.hasFocus()) {
            binding.tilName.error = getString(R.string.error_empty_name)
        } else {
            binding.tilName.error = null
        }

        binding.btnCreateSubject.isEnabled = isValid
    }

    private fun createSubject() {
        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.error_empty_name)
            return
        }

        createSubjectViewModel.createSubject(name, description)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCreateSubject.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnCreateSubject.isEnabled = true
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
