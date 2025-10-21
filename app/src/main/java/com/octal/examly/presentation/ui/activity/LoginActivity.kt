package com.octal.examly.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.octal.examly.R
import com.octal.examly.presentation.state.LoginState
import com.octal.examly.presentation.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    private lateinit var usernameInputLayout: TextInputLayout
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var progressIndicator: CircularProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupToolbar()
        initializeViews()
        setupListeners()
        observeLoginState()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "Iniciar Sesión"
            setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun initializeViews() {
        usernameInputLayout = findViewById(R.id.usernameInputLayout)
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        progressIndicator = findViewById(R.id.progressIndicator)
    }

    private fun setupListeners() {
        // Limpiar errores al escribir
        usernameEditText.doOnTextChanged { _, _, _, _ ->
            usernameInputLayout.error = null
        }

        passwordEditText.doOnTextChanged { _, _, _, _ ->
            passwordInputLayout.error = null
        }

        // Botón de login
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInput(username, password)) {
                viewModel.login(username, password)
            }
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        var isValid = true

        if (username.isBlank()) {
            usernameInputLayout.error = "El usuario no puede estar vacío"
            isValid = false
        }

        if (password.isBlank()) {
            passwordInputLayout.error = "La contraseña no puede estar vacía"
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
                            showLoading(false)
                        }
                        is LoginState.Loading -> {
                            showLoading(true)
                        }
                        is LoginState.Success -> {
                            showLoading(false)
                            navigateToMain()
                        }
                        is LoginState.Error -> {
                            showLoading(false)
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            loginButton.visibility = View.GONE
            progressIndicator.visibility = View.VISIBLE
            usernameEditText.isEnabled = false
            passwordEditText.isEnabled = false
        } else {
            loginButton.visibility = View.VISIBLE
            progressIndicator.visibility = View.GONE
            usernameEditText.isEnabled = true
            passwordEditText.isEnabled = true
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}