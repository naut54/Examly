package com.octal.examly.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.octal.examly.R
import com.octal.examly.databinding.ActivityMainBinding
import com.octal.examly.domain.model.UserRole
import com.octal.examly.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels()

    private var currentUserRole: UserRole? = null
    private var hasNavigatedToInitialDestination = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        observeCurrentUser()
        setupBackPressedHandler()
    }

    private fun setupNavigation() {
        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.testListFragment,
                R.id.resultsFragment,
                R.id.settingsFragment,
                R.id.adminHomeFragment
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.bottomNavigation?.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = destination.label
        }
    }

    private fun observeCurrentUser() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentUser.collect { user ->
                    user?.let {
                        currentUserRole = it.role
                        setupNavigationForRole(it.role)
                    }
                }
            }
        }
    }

    private fun setupNavigationForRole(role: UserRole) {
        binding.bottomNavigation.menu.clear()

        when (role) {
            UserRole.USER -> {
                binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_user)

                if (!hasNavigatedToInitialDestination) {
                    navigateToInitialDestination(R.id.homeFragment)
                }
            }

            UserRole.ADMIN -> {
                binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_admin)

                if (!hasNavigatedToInitialDestination) {
                    navigateToInitialDestination(R.id.adminHomeFragment)
                }
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (navController.currentDestination?.id != item.itemId) {
                try {
                    navController.popBackStack(item.itemId, false)
                    if (navController.currentDestination?.id != item.itemId) {
                        navController.navigate(item.itemId)
                    }
                } catch (e: IllegalArgumentException) {
                    val homeDestination = when (currentUserRole) {
                        UserRole.ADMIN -> R.id.adminHomeFragment
                        else -> R.id.homeFragment
                    }
                    navController.popBackStack(homeDestination, false)
                    if (item.itemId != homeDestination) {
                        navController.navigate(item.itemId)
                    }
                }
            }
            true
        }
    }

    private fun navigateToInitialDestination(destinationId: Int) {
        if (navController.currentDestination?.id == R.id.homeFragment && destinationId != R.id.homeFragment) {
            navController.navigate(destinationId)
        }
        hasNavigatedToInitialDestination = true
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val topLevelDestinations = setOf(
                    R.id.homeFragment,
                    R.id.adminHomeFragment
                )

                if (navController.currentDestination?.id in topLevelDestinations) {
                    showExitConfirmationDialog()
                } else {
                    if (!navController.popBackStack()) {
                        finish()
                    }
                }
            }
        })
    }

    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.exit)
            .setMessage(getString(R.string.confirm_action))
            .setPositiveButton(R.string.exit) { _, _ ->
                finishAffinity()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutConfirmationDialog()
                true
            }
            R.id.action_settings -> {
                navController.navigate(R.id.settingsFragment)
                true
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.logout)
            .setMessage(getString(R.string.confirm_logout))
            .setPositiveButton(R.string.logout) { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun performLogout() {
        lifecycleScope.launch {
            viewModel.logout()

            Toast.makeText(
                this@MainActivity,
                getString(R.string.success_logout),
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
