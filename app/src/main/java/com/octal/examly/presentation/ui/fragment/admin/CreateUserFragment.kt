package com.octal.examly.presentation.ui.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.octal.examly.R
import com.octal.examly.databinding.FragmentCreateUserBinding
import com.octal.examly.domain.model.UserRole
import com.octal.examly.presentation.state.UiState
import com.octal.examly.presentation.viewmodel.CreateUserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateUserFragment : Fragment() {

    private var _binding: FragmentCreateUserBinding? = null
    private val binding get() = _binding!!

    private val createUserViewModel: CreateUserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeCreationState()
    }

    private fun setupUI() {
        val roles = arrayOf(getString(R.string.user), getString(R.string.admin))
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = adapter

        binding.btnCreateUser.setOnClickListener {
            createUser()
        }

        binding.btnCancel?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.etUsername.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                validateInputs()
            }
        })

        binding.etPassword.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                validateInputs()
            }
        })

        binding.etConfirmPassword.addTextChangedListener(object : android.text.TextWatcher {
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
                createUserViewModel.creationState.collect { state ->
                    when (state) {
                        is UiState.Loading -> showLoading()
                        is UiState.Success -> {
                            hideLoading()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.success_user_created),
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
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        var isValid = true

        if (username.isEmpty()) {
            binding.tilUsername.error = getString(R.string.error_empty_username)
            isValid = false
        } else {
            binding.tilUsername.error = null
        }

        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_password_too_short)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.error_passwords_not_match)
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        binding.btnCreateUser.isEnabled = isValid
    }

    private fun createUser() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val rolePosition = binding.spinnerRole.selectedItemPosition
        val role = if (rolePosition == 0) UserRole.USER else UserRole.ADMIN

        if (username.isEmpty()) {
            binding.tilUsername.error = getString(R.string.error_empty_username)
            return
        }

        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_password_too_short)
            return
        }

        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.error_passwords_not_match)
            return
        }

        createUserViewModel.createUser(username, password, role)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCreateUser.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnCreateUser.isEnabled = true
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
