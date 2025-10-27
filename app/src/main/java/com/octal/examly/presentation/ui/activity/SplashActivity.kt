package com.octal.examly.presentation.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.octal.examly.R
import com.octal.examly.presentation.viewmodel.MainViewModel
import com.octal.examly.util.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    private val viewModel: MainViewModel by viewModels()

    companion object {
        private const val SPLASH_DELAY = 2000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        checkSession()
    }

    private fun checkSession() {
        lifecycleScope.launch {
            delay(SPLASH_DELAY)

            val isLoggedIn = sessionManager.isLoggedIn()

            if (isLoggedIn) {
                val currentUser = sessionManager.getUser()
                if (currentUser != null) {
                    navigateToMain()
                } else {
                    navigateToLogin()
                }
            } else {
                navigateToLogin()
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
