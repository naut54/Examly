package com.octal.examly.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.TextInputEditText
import com.octal.examly.R
import com.octal.examly.databinding.ActivityLoginBinding
import com.octal.examly.presentation.state.LoginState
import com.octal.examly.presentation.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeLoginState()
    }

    private fun setupUI() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            binding.tilUsername.error = null
            binding.tilPassword.error = null

            if (validateInputs(username, password)) {
                viewModel.login(username, password)
            }
        }

        binding.etUsername.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.tilUsername.error = null
            }
        }

        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.tilPassword.error = null
            }
        }
    }

    private fun validateInputs(username: String, password: String): Boolean {
        var isValid = true

        if (username.isEmpty()) {
            binding.tilUsername.error = getString(R.string.error_empty_fields)
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_empty_fields)
            isValid = false
        }

        return isValid
    }

    private fun observeLoginState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        is LoginState.Idle -> {
                            hideLoading()
                        }

                        is LoginState.Loading -> {
                            showLoading()
                        }

                        is LoginState.Success -> {
                            hideLoading()
                            Toast.makeText(
                                this@LoginActivity,
                                getString(R.string.success_login),
                                Toast.LENGTH_SHORT
                            ).show()
                            navigateToMain()
                        }

                        is LoginState.Error -> {
                            hideLoading()
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
        binding.etUsername.isEnabled = false
        binding.etPassword.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnLogin.isEnabled = true
        binding.etUsername.isEnabled = true
        binding.etPassword.isEnabled = true
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        if (message.contains("credentials", ignoreCase = true) ||
            message.contains("username", ignoreCase = true) ||
            message.contains("password", ignoreCase = true)) {
            binding.tilPassword.error = getString(R.string.error_invalid_credentials)
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}
